package net.es.oscars.nsi.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.types.ServiceExceptionType;
import net.es.nsi.lib.soap.gen.nsi_2_0.services.point2point.P2PServiceBaseType;
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
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.soap.ClientUtil;
import net.es.oscars.web.beans.Interval;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private ConnService connSvc;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ClientUtil clientUtil;

    @Autowired
    private PSSAdapter pssAdapter;


    /* async operations */

    public void reserve(CommonHeaderType header, ReserveType rt, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting reserve task");
            try {
                nsiStateEngine.reserve(NsiEvent.RESV_START, mapping);
                NsiHoldResult result = this.submitHold(rt, mapping, header);
                if (result.getSuccess()) {
                    this.okCallback(NsiEvent.RESV_CF, mapping, header);
                    nsiStateEngine.reserve(NsiEvent.RESV_CF, mapping);

                } else {
                    this.errCallback(NsiEvent.RESV_FL, mapping,
                            result.getErrorMessage(),
                            result.getErrorCode().toString(), header.getCorrelationId());
                    nsiStateEngine.reserve(NsiEvent.RESV_FL, mapping);

                }

            } catch (NsiException ex) {
                log.error("internal error", ex);

            } catch (ServiceException ex) {
                log.error("resv callback failed", ex);

            }
            log.info("ending reserve");
        });
    }

    public void commit(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting commit task");
            try {
                nsiStateEngine.commit(NsiEvent.COMMIT_START, mapping);
                Connection c = this.getOscarsConnection(mapping);
                if (!c.getPhase().equals(Phase.HELD)) {
                    throw new NsiException("Invalid connection phase");
                }
                connSvc.commit(c);
                nsiStateEngine.commit(NsiEvent.COMMIT_CF, mapping);
                log.info("completed commit");
                this.okCallback(NsiEvent.COMMIT_CF, mapping, header);
            } catch (PSSException | PCEException | NsiException  ex) {
                log.error("failed commit");
                log.error(ex.getMessage(), ex);
                try {
                    nsiStateEngine.commit(NsiEvent.COMMIT_FL, mapping);
                    this.errCallback(NsiEvent.COMMIT_FL, mapping,
                            ex.getMessage(), NsiErrors.NRM_ERROR.toString(),
                            header.getCorrelationId());
                } catch (NsiException nex) {
                    log.error("commit failed: then internal error", nex);

                } catch (ServiceException cex) {
                    log.error("commit failed: then callback failed", cex);
                }
            } catch (ServiceException ex) {
                log.error("commit confirm: then callback failed", ex);
            }
            log.info("ending commit");
            return null;
        });
    }

    public void abort(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting abort task");
            try {
                nsiStateEngine.abort(NsiEvent.ABORT_START, mapping);
                Connection c = this.getOscarsConnection(mapping);

                // TODO: verify connection state
                connSvc.cancel(c);
                log.info("completed abort");
                nsiStateEngine.abort(NsiEvent.ABORT_CF, mapping);
                this.okCallback(NsiEvent.ABORT_CF, mapping, header);
            } catch (NsiException ex) {
                log.error("internal error", ex);
            } catch (ServiceException ex) {
                log.error("abort confirm callback failed", ex);
            }
            return null;
        });
    }

    public void provision(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting provision task");
            try {
                try {
                    Connection c = this.getOscarsConnection(mapping);

                    nsiStateEngine.provision(NsiEvent.PROV_START, mapping);
                    try {
                        c.setState(pssAdapter.build(c));
                    } catch (PSSException ex) {
                        // TODO: trigger a forcedEnd?
                        c.setState(State.FAILED);
                        log.error(ex.getMessage(), ex);
                    }
                    connRepo.save(c);

                    nsiStateEngine.provision(NsiEvent.PROV_CF, mapping);
                    log.info("completed provision");

                } catch (NsiException ex) {
                    log.error("provision internal error", ex);
                }
                this.okCallback(NsiEvent.PROV_CF, mapping, header);
                log.info("completed provision confirm callback");
            } catch (ServiceException ex) {
                log.error("provision confirm callback failed", ex);
            }
            return null;
        });
    }
    public void release(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting release task");
            try {
                try {
                    Connection c = this.getOscarsConnection(mapping);

                    nsiStateEngine.release(NsiEvent.REL_START, mapping);
                    try {
                        c.setState(pssAdapter.dismantle(c));
                    } catch (PSSException ex) {
                        // TODO: trigger a forcedEnd?
                        c.setState(State.FAILED);
                        log.error(ex.getMessage(), ex);
                    }
                    connRepo.save(c);

                    nsiStateEngine.release(NsiEvent.REL_CF, mapping);
                    log.info("completed release");

                } catch (NsiException ex) {
                    log.error("release internal error", ex);
                }
                this.okCallback(NsiEvent.REL_CF, mapping, header);
                log.info("completed release confirm callback");
            } catch (ServiceException ex) {
                log.error("release confirm callback failed", ex);
            }
            return null;
        });
    }

    public void terminate(CommonHeaderType header, NsiMapping mapping)
            throws InterruptedException {

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
                log.info("sent term cf callback");
                this.okCallback(NsiEvent.TERM_CF, mapping, header);
            } catch (NsiException ex) {
                log.error("failed terminate, internal error");
                log.error(ex.getMessage(), ex);
            } catch (ServiceException ex) {
                log.error("term confirm callback failed", ex);
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
            } catch (ServiceException ex) {
                log.error("term confirm callback failed", ex);
            }

            return null;
        });

    }

    /* triggered events from TransitionStates periodic tasks */

    public void resvTimedOut(NsiMapping mapping) {
        Executors.newCachedThreadPool().submit(() -> {
            log.info("starting timeout task");
            try {
                nsiStateEngine.resvTimedOut(mapping);
                this.errCallback(NsiEvent.RESV_TIMEOUT, mapping,
                        "reservation timeout", "",this.newCorrelationId());
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
        throw new NsiException("not implemented yet");
    }
    /* SOAP calls to the client */


    public void errorNotify(NsiEvent event, NsiMapping mapping, CommonHeaderType inHeader) throws ServiceException {
        throw new ServiceException("not implemented");

    }

    public void okCallback(NsiEvent event, NsiMapping mapping, CommonHeaderType inHeader) throws ServiceException {
        throw new ServiceException("not implemented");

    }

    public void errCallback(NsiEvent event, NsiMapping mapping, String error, String errNum, String corrId)
            throws NsiException, ServiceException {
        String nsaId = mapping.getNsaId();
        NsiRequesterNSA requesterNSA = this.getRequesterNsa(nsaId).orElseThrow(NsiException::new);

        ConnectionRequesterPort port = clientUtil.createRequesterClient(requesterNSA.getCallbackUrl());
        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        Holder<CommonHeaderType> outHeader = this.makeClientHeader(nsaId, corrId);
        Connection c = this.getOscarsConnection(mapping);
        GenericFailedType gft = new GenericFailedType();
        ConnectionStatesType cst = this.makeConnectionStates(mapping, c);

        gft.setConnectionId(mapping.getNsiConnectionId());
        gft.setServiceException(this.makeSET(error, errNum, mapping));
        gft.setConnectionStates(cst);

        if (event.equals(NsiEvent.RESV_FL)) {
            port.reserveFailed(gft,  outHeader);

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
        if (c.getState().equals(State.ACTIVE)) {
            dst.setActive(true);
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
        String prefix = topoId+":";
        return prefix + internalUrn.replace("/", "_");

    }
    private String internalUrnFromNsi(String nsiUrn) {
        String prefix = topoId+":";
        return nsiUrn.replace(prefix, "").replace("_", "/");

    }

    public Interval convertSchedule(ScheduleType schedule) {
        XMLGregorianCalendar xst = schedule.getStartTime().getValue();
        Instant ist = xst.toGregorianCalendar().toInstant();
        XMLGregorianCalendar xet = schedule.getEndTime().getValue();
        Instant iet = xet.toGregorianCalendar().toInstant();
        return Interval.builder()
                .beginning(ist)
                .ending(iet)
                .build();
    }


    public void getAttrs(ReserveType reserve, Holder<CommonHeaderType> header) {
        header.value.getCorrelationId();
        header.value.getReplyTo();
        header.value.getProviderNSA();

        String connId = reserve.getConnectionId();
        String corrId = header.value.getCorrelationId();

    }
    public P2PServiceBaseType getP2PService(ReserveType rt) throws NsiException {
        ReservationRequestCriteriaType crit = rt.getCriteria();
        P2PServiceBaseType p2pt = null;
        for (Object o : crit.getAny()) {
            if (o instanceof P2PServiceBaseType ) {
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
            throw new NsiException("Missing P2PServiceBaseType element!");
        }
        return p2pt;
    }


    /* db funcs */

    public Connection getOscarsConnection(NsiMapping mapping) throws NsiException {
        Optional<Connection> c = connRepo.findByConnectionId(mapping.getOscarsConnectionId());
        if (!c.isPresent()) {
            throw new NsiException("OSCARS connection not found");
        } else {
            return c.get();
        }
    }

    public NsiMapping getMapping(String nsiConnectionId) throws NsiException {
        if (nsiConnectionId == null || nsiConnectionId.equals("")) {
            throw new NsiException("null or blank connection id! "+nsiConnectionId);
        }
        List<NsiMapping> mappings = nsiRepo.findByNsiConnectionId(nsiConnectionId);
        if (mappings.isEmpty()) {
            throw new NsiException("unknown connection id "+nsiConnectionId);
        } else if (mappings.size() > 1) {
            throw new NsiException("internal error: multiple mappings for connection id "+nsiConnectionId);

        } else {
            return mappings.get(0);
        }
    }

    public Optional<NsiMapping> getMappingForOscarsId(String oscarsConnectionId) throws NsiException{
        List<NsiMapping> mappings = nsiRepo.findByOscarsConnectionId(oscarsConnectionId);
        if (mappings.isEmpty()) {
            return Optional.empty();
        } else if (mappings.size() > 1) {
            throw new NsiException("Multiple NSI mappings for oscars id "+oscarsConnectionId);
        } else {
            return Optional.of(mappings.get(0));
        }

    }

    public Optional<NsiRequesterNSA> getRequesterNsa(String nsaId) throws NsiException {
        List<NsiRequesterNSA> requesters = requesterNsaRepo.findByNsaId(nsaId);
        if (requesters.size() == 0 ) {
            return Optional.empty();

        } else if (requesters.size() >= 2) {
            throw new NsiException("multiple requester entries for "+nsaId);

        } else {
            return Optional.of(requesters.get(0));

        }
    }

    /* header processing */

    public void processHeader(CommonHeaderType inHeader) throws NsiException {
        String error = "";
        boolean hasError = false;
        if (inHeader.getProviderNSA().equals(providerNsa)) {
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
            throw new NsiException(error);
        }
        this.updateRequester(inHeader.getReplyTo(), inHeader.getRequesterNSA());
    }

    public void updateRequester(String replyTo, String nsaId) throws NsiException {
        Optional<NsiRequesterNSA> maybeRequester = this.getRequesterNsa(nsaId);
        if (maybeRequester.isPresent()) {
            NsiRequesterNSA requesterNSA = maybeRequester.get();
            if (!requesterNSA.getCallbackUrl().equals(replyTo)) {
                log.info("updating callbackUrl for "+nsaId);
                requesterNSA.setCallbackUrl(replyTo);
                requesterNsaRepo.save(requesterNSA);
            }

        } else {
            log.info("saving new requester nsa: "+nsaId);
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
