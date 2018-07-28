package net.es.oscars.nsi.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;
import net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.P2PServiceBaseType;
import net.es.nsi.lib.soap.gen.nsi_2_0.services.types.DirectionalityType;
import net.es.nsi.lib.soap.gen.nsi_2_0.services.types.OrderedStpType;
import net.es.nsi.lib.soap.gen.nsi_2_0.services.types.StpListType;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.nsi.beans.NsiErrors;
import net.es.oscars.nsi.beans.NsiEvent;
import net.es.oscars.nsi.beans.NsiHoldResult;
import net.es.oscars.nsi.db.NsiMappingRepository;
import net.es.oscars.nsi.db.NsiRequesterNSARepository;
import net.es.oscars.nsi.ent.NsiMapping;
import net.es.oscars.nsi.ent.NsiRequesterNSA;
import net.es.oscars.pce.PceService;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.soap.ClientUtil;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.PortBwVlan;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import net.es.oscars.web.beans.PceMode;
import net.es.oscars.web.beans.PceRequest;
import net.es.oscars.web.beans.PceResponse;
import net.es.oscars.web.simple.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class NsiService {
    @Value("${nml.topo-id}")
    private String topoId;

    @Value("${nml.topo-name}")
    private String topoName;

    @Value("${resv.timeout}")
    private Integer resvTimeout;

    @Value("${nsi.provider-nsa}")
    private String providerNsa;

    @Value("#{'${nsi.allowed-requesters}'.split(',')}")
    private List<String> allowedRequesters;

    final public static String SERVICE_TYPE = "http://services.ogf.org/nsi/2013/12/descriptions/EVTS.A-GOLE";
    final public static String DEFAULT_PROTOCOL_VERSION = "application/vdn.ogf.nsi.cs.v2.provider+soap";


    @Autowired
    private NsiMappingRepository nsiRepo;

    @Autowired
    private NsiRequesterNSARepository requesterNsaRepo;


    @Autowired
    private NsiStateEngine nsiStateEngine;

    @Autowired
    private TopoService topoService;

    @Autowired
    private ResvService resvService;

    @Autowired
    private PceService pceService;

    @Autowired
    private ConnService connSvc;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ClientUtil clientUtil;

    @Autowired
    private PSSAdapter pssAdapter;


    /* async operations */
    public void reserve(CommonHeaderType header, ReserveType rt, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting reserve task");
            try {
                log.info("transitioning state");
                nsiStateEngine.reserve(NsiEvent.RESV_START, mapping);
                log.info("submitting hold");
                NsiHoldResult result = this.submitHold(rt, mapping, header);
                if (result.getSuccess()) {
                    log.info("successful reserve, sending confirm");
                    nsiStateEngine.reserve(NsiEvent.RESV_CF, mapping);
                    try {
                        this.reserveConfirmCallback(mapping, header);
                    } catch (WebServiceException | ServiceException cex) {
                        log.error("reserve succeeded: then callback failed", cex);
                    }

                } else {
                    log.error("error reserving");
                    nsiStateEngine.reserve(NsiEvent.RESV_FL, mapping);
                    try {
                        this.errCallback(NsiEvent.RESV_FL, mapping,
                                result.getErrorMessage(),
                                result.getErrorCode().toString(), header.getCorrelationId());
                    } catch (WebServiceException | ServiceException cex) {
                        log.error("reserve failed: then callback failed", cex);
                    }
                }
            } catch (RuntimeException ex) {
                log.error("serious error", ex);

            } catch (NsiException ex) {
                log.error("internal error", ex);

            }
            log.info("ending reserve");
        });
    }

    public void commit(CommonHeaderType header, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting commit task");
            try {
                nsiStateEngine.commit(NsiEvent.COMMIT_START, mapping);
                Connection c = this.getOscarsConnection(mapping);
                if (!c.getPhase().equals(Phase.HELD)) {
                    throw new NsiException("Invalid connection phase", NsiErrors.TRANS_ERROR);
                }
                connSvc.commit(c);
                nsiStateEngine.commit(NsiEvent.COMMIT_CF, mapping);

                log.info("completed commit");
                try {
                    this.okCallback(NsiEvent.COMMIT_CF, mapping, header);
                    log.info("sent commit confirmed callback");
                } catch (WebServiceException | ServiceException ex) {
                    log.error("commit confirmed callback failed", ex);
                }
            } catch (PSSException | PCEException | NsiException ex) {
                log.error("failed commit");
                log.error(ex.getMessage(), ex);
                try {
                    nsiStateEngine.commit(NsiEvent.COMMIT_FL, mapping);
                    this.errCallback(NsiEvent.COMMIT_FL, mapping,
                            ex.getMessage(), NsiErrors.NRM_ERROR.toString(),
                            header.getCorrelationId());
                } catch (NsiException nex) {
                    log.error("commit failed: then internal error", nex);

                } catch (WebServiceException | ServiceException cex) {
                    log.error("commit failed: then callback failed", cex);
                }
            } catch (RuntimeException ex) {
                log.error("serious error", ex);
            }
            log.info("ending commit");
            return null;
        });
    }

    public void abort(CommonHeaderType header, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting abort task");
            try {
                Connection c = this.getOscarsConnection(mapping);
                nsiStateEngine.abort(NsiEvent.ABORT_START, mapping);
                if (!c.getPhase().equals(Phase.HELD)) {
                    throw new NsiException("invalid reservation phase, cannot abort", NsiErrors.TRANS_ERROR);
                }
                connSvc.cancel(c);
                log.info("completed abort");
                nsiStateEngine.abort(NsiEvent.ABORT_CF, mapping);
                try {
                    this.okCallback(NsiEvent.ABORT_CF, mapping, header);
                } catch (WebServiceException | ServiceException ex) {
                    log.error("abort confirmed callback failed", ex);
                }

            } catch (NsiException ex) {
                log.error("internal error", ex);
            } catch (RuntimeException ex) {
                log.error("serious error", ex);
            }
            return null;
        });
    }

    public void provision(CommonHeaderType header, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting provision task");
            try {
                Connection c = this.getOscarsConnection(mapping);
                if (!c.getPhase().equals(Phase.RESERVED)) {
                    log.error("cannot provision unless RESERVED");
                    return null;
                }

                nsiStateEngine.provision(NsiEvent.PROV_START, mapping);


                c.setMode(BuildMode.AUTOMATIC);
                connRepo.save(c);

                nsiStateEngine.provision(NsiEvent.PROV_CF, mapping);

                try {
                    this.okCallback(NsiEvent.PROV_CF, mapping, header);
                    log.info("completed provision confirm callback");
                } catch (WebServiceException | ServiceException ex) {
                    log.error("provision confirmed callback failed", ex);
                }

                log.info("completed provision");
            } catch (RuntimeException ex) {
                log.error("serious error", ex);

            } catch (NsiException ex) {
                log.error("provision internal error", ex);
            }
            return null;
        });
    }

    public void release(CommonHeaderType header, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting release task");
            try {
                Connection c = this.getOscarsConnection(mapping);
                if (!c.getPhase().equals(Phase.RESERVED)) {
                    log.error("cannot provision unless RESERVED");
                    return null;
                }

                nsiStateEngine.release(NsiEvent.REL_START, mapping);

                c.setMode(BuildMode.MANUAL);
                // if we are after start time, we will need to tear down
                if (Instant.now().isAfter(c.getReserved().getSchedule().getBeginning())) {
                    if (c.getState().equals(State.ACTIVE)) {
                        try {
                            c.setState(pssAdapter.dismantle(c));
                        } catch (PSSException ex) {
                            // TODO: trigger a forcedEnd?
                            c.setState(State.FAILED);
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }
                connRepo.save(c);

                nsiStateEngine.release(NsiEvent.REL_CF, mapping);

                try {
                    this.okCallback(NsiEvent.REL_CF, mapping, header);
                } catch (WebServiceException | ServiceException ex) {
                    log.error("release confirmed callback failed", ex);
                }
                log.info("completed release");


            } catch (RuntimeException ex) {
                log.error("serious error", ex);

            } catch (NsiException ex) {
                log.error("release internal error", ex);
            }
            return null;
        });
    }

    public void terminate(CommonHeaderType header, NsiMapping mapping) {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting terminate task");
            try {
                Connection c = this.getOscarsConnection(mapping);
                // the cancel only needs to happen if we are not in FORCED_END or PASSED_END_TIME
                if (mapping.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                    connSvc.cancel(c);
                }
                nsiStateEngine.termStart(mapping);
                log.info("completed terminate");
                nsiStateEngine.termConfirm(mapping);
                try {
                    this.okCallback(NsiEvent.TERM_CF, mapping, header);
                    log.info("sent term cf callback");
                } catch (WebServiceException | ServiceException ex) {
                    log.error("term confirmed callback failed", ex);
                }
            } catch (RuntimeException ex) {
                log.error("serious error", ex);

            } catch (NsiException ex) {
                log.error("failed terminate, internal error");
                log.error(ex.getMessage(), ex);
            }

            return null;
        });

    }


    // currently unused
    // TODO: trigger this when REST API terminates connection
    // (& possibly other errors)
    public void forcedEnd(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting forcedEnd task");
            try {
                Connection c = this.getOscarsConnection(mapping);
                nsiStateEngine.forcedEnd(mapping);
                this.errorNotify(NsiEvent.FORCED_END, mapping, header);
            } catch (NsiException ex) {
                log.error("failed terminate, internal error");
                log.error(ex.getMessage(), ex);
            } catch (RuntimeException ex) {
                log.error("serious error", ex);

            } catch (ServiceException ex) {
                log.error("term confirm callback failed", ex);
            }

            return null;
        });

    }

    public void queryAsync(CommonHeaderType header, QueryType query) {
        Executors.newCachedThreadPool().submit(() -> {
            try {
                log.info("starting async query task");
                String nsaId = header.getRequesterNSA();
                String corrId = header.getCorrelationId();
                if (!this.getRequesterNsa(nsaId).isPresent()) {
                    throw new NsiException("Unknown requester nsa id " + nsaId, NsiErrors.SEC_ERROR);
                }

                Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);
                NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).get();

                ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA);
                QuerySummaryConfirmedType qsct = this.query(query);
                try {
                    port.querySummaryConfirmed(qsct, outHeader);

                } catch (ServiceException | WebServiceException ex) {
                    log.error("could not perform query callback");
                    log.error(ex.getMessage(), ex);
                }
            } catch (RuntimeException ex) {
                log.error(ex.getMessage(), ex);
            }

            return null;
        });
    }

    @Transactional
    public QuerySummaryConfirmedType query(QueryType query) throws NsiException {
        QuerySummaryConfirmedType qsct = new QuerySummaryConfirmedType();
        try {
            ZonedDateTime zd = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
            GregorianCalendar c = GregorianCalendar.from(zd);
            XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            qsct.setLastModified(xgc);
        } catch (DatatypeConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new NsiException(ex.getMessage(), NsiErrors.NRM_ERROR);
        }
        if (query.getIfModifiedSince() != null ) {
            throw new NsiException("IMS not supported yet", NsiErrors.UNIMPLEMENTED);
        }

        Set<NsiMapping> mappings = new HashSet<>();
        for (String connId : query.getConnectionId()) {
            mappings.addAll(nsiRepo.findByNsiConnectionId(connId));
        }
        for (String gri : query.getGlobalReservationId()) {
            mappings.addAll(nsiRepo.findByNsiGri(gri));
        }
        Long resultId = 0L;
        for (NsiMapping mapping : mappings) {
            QuerySummaryResultType qsrt = this.toQSRT(mapping);
            qsrt.setResultId(resultId);
            qsct.getReservation().add(qsrt);
            resultId++;
        }
        return qsct;
    }

    public QuerySummaryResultType toQSRT(NsiMapping mapping) throws NsiException {
        Connection c = this.getOscarsConnection(mapping);
        QuerySummaryResultType qsrt = new QuerySummaryResultType();
        qsrt.setConnectionId(mapping.getNsiConnectionId());

        QuerySummaryResultCriteriaType qsrct = new QuerySummaryResultCriteriaType();
        qsrct.setServiceType(SERVICE_TYPE);
        qsrct.setVersion(0);

        P2PServiceBaseType p2p = makeP2P(c);

        net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory p2pof
                = new net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory();

        qsrct.getAny().add(p2pof.createP2Ps(p2p));
        qsrt.getCriteria().add(qsrct);

        qsrt.setDescription(c.getDescription());
        qsrt.setGlobalReservationId(mapping.getNsiGri());
        qsrt.setRequesterNSA(mapping.getNsaId());
        ConnectionStatesType cst = this.makeConnectionStates(mapping, c);
        qsrt.setConnectionStates(cst);
        qsrt.setNotificationId(0L);
        return qsrt;
    }

    /* triggered events from TransitionStates periodic tasks */

    public void resvTimedOut(NsiMapping mapping) {
        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting timeout task");
            try {
                nsiStateEngine.resvTimedOut(mapping);
                this.errCallback(NsiEvent.RESV_TIMEOUT, mapping,
                        "reservation timeout", "", this.newCorrelationId());
            } catch (ServiceException ex) {
                log.error("timeout callback failed", ex);
            } catch (NsiException ex) {
                log.error("internal error", ex);
            }

            return null;
        });
    }


    public void pastEndTime(NsiMapping mapping) {
        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting past end time task");
            try {
                nsiStateEngine.pastEndTime(mapping);
            } catch (NsiException ex) {
                log.error("internal error", ex);
            }
            log.info("finished past end time task");
            return null;
        });
    }

    /* submit hold */
    public NsiHoldResult submitHold(ReserveType rt, NsiMapping mapping, CommonHeaderType inHeader) throws NsiException {
        log.info("preparing connection");

        P2PServiceBaseType p2p = this.getP2PService(rt);
        log.info("got p2p");

        ReservationRequestCriteriaType crit = rt.getCriteria();

        Long mbpsLong = p2p.getCapacity() / 1000000L;
        Integer mbps = mbpsLong.intValue();

        Interval interval = this.nsiToOscarsSchedule(crit.getSchedule());
        Long begin = interval.getBeginning().getEpochSecond();
        Long end = interval.getEnding().getEpochSecond();

        Instant exp = Instant.now().plus(resvTimeout, ChronoUnit.SECONDS);
        Long expSecs = exp.toEpochMilli() / 1000L;
        log.info("got schedule and bw");

        List<SimpleTag> tags = new ArrayList<>();
        tags.add(SimpleTag.builder().category("nsi").contents("").build());

        List<String> include = new ArrayList<>();


        if (p2p.getEro() != null) {
            for (OrderedStpType stp : p2p.getEro().getOrderedSTP()) {
                String urn = this.internalUrnFromStp(stp.getStp());
                include.add(urn);
            }
        }

        try {
            log.info("making fixtures and junctions");
            Pair<List<Fixture>, List<Junction>> fixturesAndJunctions = this.fixturesAndJunctionsFor(p2p, interval);
            log.info("making pipes");
            List<Pipe> pipes = this.pipesFor(interval, mbps, fixturesAndJunctions.getRight(), include);

            SimpleConnection simpleConnection = SimpleConnection.builder()
                    .connectionId(rt.getConnectionId())
                    .description(rt.getDescription())
                    .heldUntil(expSecs.intValue())
                    .phase(Phase.HELD)
                    .state(State.WAITING)
                    .mode(BuildMode.MANUAL)
                    .begin(begin.intValue())
                    .end(end.intValue())
                    .fixtures(fixturesAndJunctions.getLeft())
                    .junctions(fixturesAndJunctions.getRight())
                    .pipes(pipes)
                    .tags(tags)
                    .username("nsi")
                    .build();
            try {
                String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(simpleConnection);
                log.debug("simple conn: \n" + pretty);

            } catch (JsonProcessingException ex) {
                log.error(ex.getMessage(), ex);
            }

            Connection c = connSvc.toNewConnection(simpleConnection);
            try {
                String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
                log.debug("full conn: \n" + pretty);

            } catch (JsonProcessingException ex) {
                log.error(ex.getMessage(), ex);
            }

            log.info("saving new connection");
            connRepo.save(c);
            mapping.setOscarsConnectionId(c.getConnectionId());
            return NsiHoldResult.builder()
                    .errorCode(NsiErrors.OK)
                    .success(true)
                    .errorMessage("")
                    .build();

        } catch (NsiException ex) {

            log.error(ex.getMessage(), ex);
            return NsiHoldResult.builder()
                    .errorCode(ex.getError())
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build();
        } catch (RuntimeException ex) {
            log.error(ex.getMessage(), ex);
            return NsiHoldResult.builder()
                    .errorCode(NsiErrors.RESV_ERROR)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build();
        }
    }

    public List<Pipe> pipesFor(Interval interval, Integer mbps,
                               List<Junction> junctions, List<String> include)
            throws NsiException {
        if (junctions.size() == 0) {
            throw new NsiException("no junctions - at least one required", NsiErrors.PCE_ERROR);
        } else if (junctions.size() == 1) {
            // no pipes required
            return new ArrayList<>();
        } else if (junctions.size() > 2) {
            throw new NsiException("Too many junctions for NSI reserve", NsiErrors.PCE_ERROR);
        }
        try {
            PceRequest request = PceRequest.builder()
                    .a(junctions.get(0).getDevice())
                    .z(junctions.get(1).getDevice())
                    .interval(interval)
                    .azBw(mbps)
                    .zaBw(mbps)
                    .include(include)
                    .build();
            PceResponse response = pceService.calculatePaths(request);
            if (response.getFits() != null) {
                List<String> ero = new ArrayList<>();
                for (EroHop hop : response.getFits().getAzEro()) {
                    ero.add(hop.getUrn());
                }
                Pipe p = Pipe.builder()
                        .mbps(mbps)
                        .ero(ero)
                        .a(junctions.get(0).getDevice())
                        .z(junctions.get(1).getDevice())
                        .pceMode(PceMode.BEST)
                        .build();
                List<Pipe> result = new ArrayList<>();
                result.add(p);
                return result;
            } else {
                throw new NsiException("Path not found", NsiErrors.PCE_ERROR);

            }

        } catch (PCEException ex) {
            throw new NsiException(ex.getMessage(), NsiErrors.PCE_ERROR);

        }

    }

    public Pair<List<Fixture>, List<Junction>> fixturesAndJunctionsFor(P2PServiceBaseType p2p, Interval interval)
            throws NsiException {
        String src = p2p.getSourceSTP();
        String dst = p2p.getDestSTP();
        String in_a = this.internalUrnFromStp(src);
        String in_z = this.internalUrnFromStp(dst);
        Long mbps = p2p.getCapacity() / 1000000L;

        TopoUrn a_urn = topoService.getTopoUrnMap().get(in_a);
        if (a_urn == null) {
            log.error("could not find stp " + src + ", converted to " + in_a);
            throw new NsiException("src STP not found in topology " + src, NsiErrors.LOOKUP_ERROR);
        } else if (!a_urn.getUrnType().equals(UrnType.PORT)) {
            throw new NsiException("src STPs is not a port " + src, NsiErrors.LOOKUP_ERROR);
        }

        TopoUrn z_urn = topoService.getTopoUrnMap().get(in_z);
        if (z_urn == null) {
            log.error("could not find stp " + src + ", converted to " + in_z);
            throw new NsiException("dst STP not found in topology " + dst, NsiErrors.LOOKUP_ERROR);
        } else if (!z_urn.getUrnType().equals(UrnType.PORT)) {
            throw new NsiException("dst STP is not a port " + dst, NsiErrors.LOOKUP_ERROR);
        }
        List<Junction> junctions = new ArrayList<>();
        Junction aJ = Junction.builder().device(a_urn.getDevice().getUrn()).build();
        junctions.add(aJ);
        Junction zJ = aJ;
        if (!a_urn.getDevice().getUrn().equals(z_urn.getDevice().getUrn())) {
            zJ = Junction.builder()
                    .device(z_urn.getDevice().getUrn())
                    .build();
            junctions.add(zJ);
        }

        Set<IntRange> aVlansSet = new HashSet<>();
        aVlansSet.add(this.getVlanRange(src));

        Set<IntRange> zVlansSet = new HashSet<>();
        zVlansSet.add(this.getVlanRange(dst));

        Map<String, PortBwVlan> available = resvService.available(interval, null);
        PortBwVlan aAvail = available.get(a_urn.getUrn());
        PortBwVlan zAvail = available.get(z_urn.getUrn());

        if (aAvail.getIngressBandwidth() < mbps) {
            throw new NsiException("bandwidth unavailable for " + src, NsiErrors.UNAVAIL_ERROR);
        } else if (aAvail.getEgressBandwidth() < mbps) {
            throw new NsiException("bandwidth unavailable for " + src, NsiErrors.UNAVAIL_ERROR);

        } else if (zAvail.getIngressBandwidth() < mbps) {
            throw new NsiException("bandwidth unavailable for " + dst, NsiErrors.UNAVAIL_ERROR);
        } else if (zAvail.getEgressBandwidth() < mbps) {
            throw new NsiException("bandwidth unavailable for " + dst, NsiErrors.UNAVAIL_ERROR);
        }

        Map<String, Set<IntRange>> requestedVlans = new HashMap<>();
        requestedVlans.put(a_urn.getPort().getUrn() + "#A", aVlansSet);
        requestedVlans.put(z_urn.getPort().getUrn() + "#Z", zVlansSet);

        Map<String, Set<IntRange>> availVlans = new HashMap<>();
        availVlans.put(a_urn.getPort().getUrn(), aAvail.getVlanRanges());
        availVlans.put(z_urn.getPort().getUrn(), zAvail.getVlanRanges());

        Map<String, Integer> vlans = ResvLibrary.decideIdentifier(requestedVlans, availVlans);
        Integer aVlanId = vlans.get(a_urn.getPort().getUrn() + "#A");
        Integer zVlanId = vlans.get(z_urn.getPort().getUrn() + "#Z");

        if (aVlanId == null) {
            throw new NsiException("vlan(s) unavailable for " + src, NsiErrors.UNAVAIL_ERROR);

        } else if (zVlanId == null) {
            throw new NsiException("vlan(s) unavailable for " + dst, NsiErrors.UNAVAIL_ERROR);
        }

        Fixture aF = Fixture.builder()
                .junction(aJ.getDevice())
                .port(a_urn.getPort().getUrn())
                .mbps(mbps.intValue())
                .vlan(aVlanId)
                .build();
        Fixture zF = Fixture.builder()
                .junction(zJ.getDevice())
                .port(z_urn.getPort().getUrn())
                .mbps(mbps.intValue())
                .vlan(zVlanId)
                .build();

        List<Fixture> fixtures = new ArrayList<>();
        fixtures.add(aF);
        fixtures.add(zF);

        return new ImmutablePair<>(fixtures, junctions);
    }


    public IntRange getVlanRange(String stp) throws NsiException {
        checkStp(stp);
        String[] stpParts = StringUtils.split(stp, "\\?");
        if (stpParts.length < 2) {
            throw new NsiException("no labels (VLAN or otherwise) for stp " + stp, NsiErrors.LOOKUP_ERROR);
        }
        IntRange vlanRange = IntRange.builder().floor(-1).ceiling(-1).build();

        for (int i = 1; i < stpParts.length; i++) {
            String labelAndValue = stpParts[i];
            String[] lvParts = StringUtils.split(labelAndValue, "=");
            if (lvParts == null || lvParts.length == 0) {
                log.info("empty label-value part");
            } else if (lvParts.length == 1) {
                log.info("just a label, ignoring: " + lvParts[0]);
            } else if (lvParts.length > 2) {
                log.info("label-value parse error: [" + labelAndValue + "]");
            } else {
                // lvParts.length == 2
                String label = lvParts[0];
                String value = lvParts[1];
                if (label.equals("vlan")) {
                    String[] parts = value.split("-");

                    if (parts.length == 1) {
                        Integer vlan = Integer.valueOf(parts[0]);
                        vlanRange.setFloor(vlan);
                        vlanRange.setCeiling(vlan);
                        log.info("vlan range for " + stp + " : " + vlan);
                        return vlanRange;
                    } else if (parts.length == 2) {
                        Integer f = Integer.valueOf(parts[0]);
                        Integer c = Integer.valueOf(parts[1]);
                        vlanRange.setFloor(f);
                        vlanRange.setCeiling(c);
                        log.info("vlan range for " + stp + " : " + f + " - " + c);
                        return vlanRange;

                    }
                } else {
                    log.info("label-value: " + lvParts[0] + " = " + lvParts[1]);
                }
            }
        }
        throw new NsiException("could not locate VLAN range for STP " + stp, NsiErrors.LOOKUP_ERROR);
    }

    public static void checkStp(String stp) throws NsiException {
        if (stp == null) {
            log.error("null STP");
            throw new NsiException("null STP", NsiErrors.LOOKUP_ERROR);
        } else if (stp.length() == 0) {
            log.error("empty STP");
            throw new NsiException("empty STP string", NsiErrors.LOOKUP_ERROR);

        }
        if (!stp.startsWith("urn:ogf:network:")) {
            throw new NsiException("STP does not start with 'urn:ogf:network:' :" + stp, NsiErrors.LOOKUP_ERROR);
        }

    }

    /* SOAP calls to the client */


    public void errorNotify(NsiEvent event, NsiMapping mapping, CommonHeaderType inHeader) throws ServiceException {
        log.info("error notify (only used by forcedEnd)");
        // TODO
        throw new ServiceException("not implemented");

    }

    @Transactional
    public void reserveConfirmCallback(NsiMapping mapping, CommonHeaderType inHeader)
            throws NsiException, ServiceException {
        String nsaId = mapping.getNsaId();
        if (!this.getRequesterNsa(nsaId).isPresent()) {
            throw new NsiException("Unknown requester nsa id " + nsaId, NsiErrors.SEC_ERROR);
        }
        NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).get();

        ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA);

        GenericConfirmedType gct = new GenericConfirmedType();
        gct.setConnectionId(mapping.getNsiConnectionId());

        String corrId = inHeader.getCorrelationId();

        Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);
        Connection c = this.getOscarsConnection(mapping);

        ReserveConfirmedType rct = new ReserveConfirmedType();

        rct.setConnectionId(mapping.getNsiConnectionId());
        rct.setGlobalReservationId(mapping.getNsiGri());
        rct.setDescription(c.getDescription());


        ReservationConfirmCriteriaType rcct = new ReservationConfirmCriteriaType();
        ScheduleType st = this.oscarsToNsiSchedule(c.getHeld().getSchedule());
        rcct.setSchedule(st);
        rct.setCriteria(rcct);
        rcct.setServiceType(SERVICE_TYPE);
        rcct.setVersion(0);

        P2PServiceBaseType p2p = makeP2P(c);
        net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory p2pof
                = new net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.ObjectFactory();
        rcct.getAny().add(p2pof.createP2Ps(p2p));

        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rct);
            log.debug("rct: \n" + pretty);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage(), ex);
        }
        port.reserveConfirmed(rct, outHeader);
    }

    public void dataplaneCallback(NsiMapping mapping, State st) throws NsiException, ServiceException {
        log.info("OK callback");
        String nsaId = mapping.getNsaId();
        if (!this.getRequesterNsa(nsaId).isPresent()) {
            throw new NsiException("Unknown requester nsa id " + nsaId, NsiErrors.SEC_ERROR);
        }
        NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).get();

        ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA);
        net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ObjectFactory of =
                new net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ObjectFactory();

        DataPlaneStateChangeRequestType dsrt = of.createDataPlaneStateChangeRequestType();
        DataPlaneStatusType dst = new DataPlaneStatusType();
        dsrt.setConnectionId(mapping.getNsiConnectionId());

        try {
            ZonedDateTime zd = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
            GregorianCalendar c = GregorianCalendar.from(zd);
            XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            dsrt.setTimeStamp(xgc);

        } catch (DatatypeConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new NsiException(ex.getMessage(), NsiErrors.NRM_ERROR);
        }

        dst.setActive(false);

        if (st.equals(State.ACTIVE)) {
            dst.setActive(true);
        }
        dst.setVersion(mapping.getDataplaneVersion());
        dst.setVersionConsistent(true);
        dsrt.setDataPlaneStatus(dst);

        String corrId = this.newCorrelationId();
        Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);
        port.dataPlaneStateChange(dsrt, outHeader);

    }

    @Transactional
    public void okCallback(NsiEvent event, NsiMapping mapping, CommonHeaderType inHeader)
            throws NsiException, ServiceException {
        log.info("OK callback");
        String nsaId = mapping.getNsaId();
        if (!this.getRequesterNsa(nsaId).isPresent()) {
            throw new NsiException("Unknown requester nsa id " + nsaId, NsiErrors.SEC_ERROR);
        }
        NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).get();

        ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA);

        GenericConfirmedType gct = new GenericConfirmedType();
        gct.setConnectionId(mapping.getNsiConnectionId());

        String corrId = inHeader.getCorrelationId();

        Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);
        if (event.equals(NsiEvent.ABORT_CF)) {
            port.reserveAbortConfirmed(gct, outHeader);

        } else if (event.equals(NsiEvent.COMMIT_CF)) {
            port.reserveCommitConfirmed(gct, outHeader);
        } else if (event.equals(NsiEvent.TERM_CF)) {
            port.terminateConfirmed(gct, outHeader);
        } else if (event.equals(NsiEvent.PROV_CF)) {
            port.provisionConfirmed(gct, outHeader);
        } else if (event.equals(NsiEvent.REL_CF)) {
            port.releaseConfirmed(gct, outHeader);
        }

    }


    public P2PServiceBaseType makeP2P(Connection c) {

        P2PServiceBaseType p2p = new P2PServiceBaseType();

        Components cmp = c.getHeld().getCmp();
        VlanFixture a = cmp.getFixtures().get(0);
        VlanFixture z = cmp.getFixtures().get(1);
        String srcStp = this.nsiUrnFromInternal(a.getPortUrn()) + "?vlan=" + a.getVlan().getVlanId();
        String dstStp = this.nsiUrnFromInternal(z.getPortUrn()) + "?vlan=" + z.getVlan().getVlanId();
        List<String> strEro = new ArrayList<>();
        if (cmp.getPipes() == null || cmp.getPipes().isEmpty()) {
            strEro.add(srcStp);
            strEro.add(dstStp);
        } else {
            VlanPipe p = cmp.getPipes().get(0);
            strEro.add(srcStp);
            for (int i = 0; i < p.getAzERO().size(); i++) {
                // skip devices in NSI ERO
                if (i % 3 != 0) {
                    strEro.add(this.nsiUrnFromInternal(p.getAzERO().get(i).getUrn()));
                }
            }
            strEro.add(dstStp);
        }

        StpListType ero = new StpListType();
        for (int i = 0; i < strEro.size(); i++) {
            OrderedStpType ostp = new OrderedStpType();
            ostp.setStp(strEro.get(i));
            ostp.setOrder(i);

            ero.getOrderedSTP().add(ostp);

        }

        p2p.setSourceSTP(srcStp);
        p2p.setDestSTP(dstStp);
        p2p.setCapacity(a.getIngressBandwidth() * 1000000L);
        p2p.setEro(ero);
        p2p.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        p2p.setSymmetricPath(true);
        return p2p;
    }

    public void errCallback(NsiEvent event, NsiMapping mapping, String error, String errNum, String corrId)
            throws NsiException, ServiceException {
        String nsaId = mapping.getNsaId();
        if (!this.getRequesterNsa(nsaId).isPresent()) {
            throw new NsiException("Unknown requester nsa id " + nsaId, NsiErrors.SEC_ERROR);
        }
        NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).get();

        ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA);

        Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);


        GenericFailedType gft = new GenericFailedType();
        gft.setConnectionId(mapping.getNsiConnectionId());
        gft.setServiceException(this.makeSET(error, errNum, mapping));

        if (!event.equals(NsiEvent.RESV_FL)) {
            Connection c = this.getOscarsConnection(mapping);
            ConnectionStatesType cst = this.makeConnectionStates(mapping, c);

            gft.setConnectionStates(cst);

        } else {
            // if it's a RESV_FL there won't be a connection to find, so set from null
            gft.setConnectionStates(this.makeConnectionStates(mapping, null));

        }


        if (event.equals(NsiEvent.RESV_FL)) {
            port.reserveFailed(gft, outHeader);

        } else if (event.equals(NsiEvent.COMMIT_FL)) {
            port.reserveCommitFailed(gft, outHeader);

        } else if (event.equals(NsiEvent.RESV_TIMEOUT)) {
            ReserveTimeoutRequestType rrt = new ReserveTimeoutRequestType();
            rrt.setOriginatingConnectionId(mapping.getNsiConnectionId());
            rrt.setOriginatingNSA(providerNsa);
            rrt.setTimeoutValue(resvTimeout);
            port.reserveTimeout(rrt, outHeader);
        }
    }


    /* utility / shared funcs */

    public ServiceExceptionType makeSET(String error, String errNum, NsiMapping mapping) {
        ServiceExceptionType exceptionType = new ServiceExceptionType();
        exceptionType.setConnectionId(mapping.getNsiConnectionId());
        exceptionType.setNsaId(providerNsa);
        exceptionType.setServiceType(SERVICE_TYPE);
        exceptionType.setText(error);
        exceptionType.setErrorId(errNum);

        return exceptionType;
    }


    public ConnectionStatesType makeConnectionStates(NsiMapping mapping, Connection c) {
        DataPlaneStatusType dst = new DataPlaneStatusType();
        dst.setActive(false);
        if (c != null) {
            if (c.getState().equals(State.ACTIVE)) {
                dst.setActive(true);
            }
        }
        dst.setVersion(mapping.getDataplaneVersion());
        dst.setVersionConsistent(true);

        ConnectionStatesType cst = new ConnectionStatesType();
        cst.setDataPlaneStatus(dst);
        cst.setLifecycleState(mapping.getLifecycleState());
        cst.setProvisionState(mapping.getProvisionState());
        cst.setReservationState(mapping.getReservationState());
        return cst;
    }


    public String nsiUrnFromInternal(String internalUrn) {
        String prefix = topoId + ":";
        return prefix + internalUrn.replace("/", "_") + ":+";

    }

    private String internalUrnFromNsi(String nsiUrn) {
        String prefix = topoId + ":";
        return nsiUrn.replace(prefix, "")
                .replace("_", "/")
                .replace(":+", "");

    }

    private String internalUrnFromStp(String stp) {
        String[] stpParts = StringUtils.split(stp, "\\?");
        return internalUrnFromNsi(stpParts[0]);
    }

    public Interval nsiToOscarsSchedule(ScheduleType schedule) {
        XMLGregorianCalendar xst = schedule.getStartTime().getValue();
        Instant ist = xst.toGregorianCalendar().toInstant();
        XMLGregorianCalendar xet = schedule.getEndTime().getValue();
        Instant iet = xet.toGregorianCalendar().toInstant();
        return Interval.builder()
                .beginning(ist)
                .ending(iet)
                .build();
    }

    public ScheduleType oscarsToNsiSchedule(Schedule sch) throws NsiException {

        Instant b = sch.getBeginning();

        ZonedDateTime zdb = ZonedDateTime.ofInstant(b, ZoneId.systemDefault());
        GregorianCalendar cb = GregorianCalendar.from(zdb);


        Instant e = sch.getEnding();
        ZonedDateTime zde = ZonedDateTime.ofInstant(e, ZoneId.systemDefault());
        GregorianCalendar ce = GregorianCalendar.from(zde);

        try {
            XMLGregorianCalendar xgb = DatatypeFactory.newInstance().newXMLGregorianCalendar(cb);

            XMLGregorianCalendar xge = DatatypeFactory.newInstance().newXMLGregorianCalendar(ce);

            net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ObjectFactory of =
                    new net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.ObjectFactory();

            ScheduleType st = of.createScheduleType();

            st.setStartTime(of.createScheduleTypeStartTime(xgb));
            st.setEndTime(of.createScheduleTypeEndTime(xge));
            return st;
        } catch (DatatypeConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new NsiException("internal error", NsiErrors.NRM_ERROR);
        }

    }


    public P2PServiceBaseType getP2PService(ReserveType rt) throws NsiException {
        ReservationRequestCriteriaType crit = rt.getCriteria();
        P2PServiceBaseType p2pt = null;
        for (Object o : crit.getAny()) {
            if (o instanceof P2PServiceBaseType) {
                p2pt = (P2PServiceBaseType) o;
            } else {
                try {

                    JAXBElement<P2PServiceBaseType> payload = (JAXBElement<P2PServiceBaseType>) o;
                    p2pt = payload.getValue();
                } catch (ClassCastException ex) {
                    log.error(ex.getMessage(), ex);
                    p2pt = null;
                }
            }
        }

        if (p2pt == null) {
            throw new NsiException("Missing P2PServiceBaseType element!", NsiErrors.MISSING_PARAM_ERROR);
        }
        return p2pt;
    }


    /* db funcs */

    @Transactional
    public Connection getOscarsConnection(NsiMapping mapping) throws NsiException {
        Optional<Connection> c = connRepo.findByConnectionId(mapping.getOscarsConnectionId());
        if (!c.isPresent()) {
            throw new NsiException("OSCARS connection not found", NsiErrors.NO_SCH_ERROR);
        } else {
            return c.get();
        }
    }

    public NsiMapping getMapping(String nsiConnectionId) throws NsiException {
        if (nsiConnectionId == null || nsiConnectionId.equals("")) {
            throw new NsiException("null or blank connection id! " + nsiConnectionId, NsiErrors.MISSING_PARAM_ERROR);
        }
        List<NsiMapping> mappings = nsiRepo.findByNsiConnectionId(nsiConnectionId);
        if (mappings.isEmpty()) {
            throw new NsiException("unknown connection id " + nsiConnectionId, NsiErrors.NO_SCH_ERROR);
        } else if (mappings.size() > 1) {
            throw new NsiException("internal error: multiple mappings for connection id " + nsiConnectionId, NsiErrors.NRM_ERROR);

        } else {
            return mappings.get(0);
        }
    }

    public Optional<NsiMapping> getMappingForOscarsId(String oscarsConnectionId) throws NsiException {
        List<NsiMapping> mappings = nsiRepo.findByOscarsConnectionId(oscarsConnectionId);
        if (mappings.isEmpty()) {
            return Optional.empty();
        } else if (mappings.size() > 1) {
            throw new NsiException("Multiple NSI mappings for oscars id " + oscarsConnectionId, NsiErrors.NRM_ERROR);
        } else {
            return Optional.of(mappings.get(0));
        }

    }

    public Optional<NsiRequesterNSA> getRequesterNsa(String nsaId) throws NsiException {
        List<NsiRequesterNSA> requesters = requesterNsaRepo.findByNsaId(nsaId);
        if (requesters.size() == 0) {
            return Optional.empty();

        } else if (requesters.size() >= 2) {
            throw new NsiException("multiple requester entries for " + nsaId, NsiErrors.NRM_ERROR);

        } else {
            return Optional.of(requesters.get(0));

        }
    }

    /* header processing */

    public void processHeader(CommonHeaderType inHeader) throws NsiException {
        String error = "";
        boolean hasError = false;
        if (!inHeader.getProviderNSA().equals(providerNsa)) {
            hasError = true;
            error += "provider nsa does not match\n";
        }
        boolean isAllowed = false;
        for (String allowed : allowedRequesters) {
            if (allowed.equals(inHeader.getRequesterNSA())) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            hasError = true;
            error += "requester nsa not in allowed list\n";
        }
        if (hasError) {
            throw new NsiException(error, NsiErrors.SEC_ERROR);
        }
        this.updateRequester(inHeader.getReplyTo(), inHeader.getRequesterNSA());
    }

    public void updateRequester(String replyTo, String nsaId) throws NsiException {
        Optional<NsiRequesterNSA> maybeRequester = this.getRequesterNsa(nsaId);
        if (maybeRequester.isPresent()) {
            NsiRequesterNSA requesterNSA = maybeRequester.get();
            if (!requesterNSA.getCallbackUrl().equals(replyTo)) {
                log.info("updating callbackUrl for " + nsaId);
                requesterNSA.setCallbackUrl(replyTo);
                requesterNsaRepo.save(requesterNSA);
            }

        } else {
            log.info("saving new requester nsa: " + nsaId);
            NsiRequesterNSA requesterNSA = NsiRequesterNSA.builder()
                    .callbackUrl(replyTo)
                    .nsaId(nsaId)
                    .build();
            requesterNsaRepo.save(requesterNSA);

        }
    }


    public void makeResponseHeader(CommonHeaderType inHeader) {
        inHeader.getAny().clear();
        inHeader.setReplyTo("");
    }

    public Holder<CommonHeaderType> makeClientHeader(String requesterNsaId, String correlationId) {
        CommonHeaderType hd = new CommonHeaderType();
        hd.setRequesterNSA(requesterNsaId);
        hd.setProviderNSA(providerNsa);
        hd.setProtocolVersion(DEFAULT_PROTOCOL_VERSION);
        hd.setCorrelationId(correlationId);
        Holder<CommonHeaderType> header = new Holder<>();
        header.value = hd;

        return header;
    }

    public String newCorrelationId() {
        return "urn:uuid:" + UUID.randomUUID().toString();
    }

}
