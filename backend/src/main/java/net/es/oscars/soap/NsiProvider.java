package net.es.oscars.soap;

import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.Error;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.nsi.ent.NsiMapping;
import net.es.oscars.nsi.svc.NsiService;
import net.es.oscars.nsi.svc.NsiStateEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.ws.Holder;

@Slf4j
@Component
public class NsiProvider implements ConnectionProviderPort {


    @Autowired
    private NsiService nsiService;
    @Autowired
    private NsiStateEngine stateEngine;

    @Override
    public GenericAcknowledgmentType provision(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
            NsiMapping mapping = nsiService.getMapping(parameters.getConnectionId());
            nsiService.provision(header.value, mapping);
        } catch (NsiException | InterruptedException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }

        nsiService.makeResponseHeader(header.value);
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveCommit(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
            NsiMapping mapping = nsiService.getMapping(parameters.getConnectionId());
            nsiService.commit(header.value, mapping);
        } catch (NsiException | InterruptedException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }

        nsiService.makeResponseHeader(header.value);
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType terminate(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
            NsiMapping mapping = nsiService.getMapping(parameters.getConnectionId());
            nsiService.terminate(header.value, mapping);
        } catch (NsiException | InterruptedException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }


        nsiService.makeResponseHeader(header.value);
        return new GenericAcknowledgmentType();
    }

    @Override
    public ReserveResponseType reserve(ReserveType reserve, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
        } catch (NsiException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }

        NsiMapping mapping = stateEngine.newMapping(
                reserve.getConnectionId(),
                reserve.getGlobalReservationId(),
                header.value.getProtocolVersion());
        try {
            log.info("triggering async reserve");
            nsiService.reserve(header.value, reserve, mapping);
            log.info("returning reserve ack");
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }

        ReserveResponseType rrt = new ReserveResponseType();
        rrt.setConnectionId(mapping.getNsiConnectionId());

        nsiService.makeResponseHeader(header.value);
        return rrt;
    }

    /*
    try {
        String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(reserve);
        log.debug(pretty);
         pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(header);
        log.debug(pretty);

    } catch (JsonProcessingException ex) {
        ex.printStackTrace();
    }
    */

    @Override
    public GenericAcknowledgmentType release(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
            NsiMapping mapping = nsiService.getMapping(parameters.getConnectionId());
            nsiService.release(header.value, mapping);
        } catch (NsiException | InterruptedException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }

        nsiService.makeResponseHeader(header.value);
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveAbort(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            nsiService.processHeader(header.value);
            NsiMapping mapping = nsiService.getMapping(parameters.getConnectionId());
            nsiService.abort(header.value, mapping);
        } catch (NsiException | InterruptedException ex) {
            log.error(ex.getMessage(),ex);
            throw new ServiceException(ex.getMessage());
        }

        nsiService.makeResponseHeader(header.value);
        return new GenericAcknowledgmentType();
    }

    /* queries */

    @Override
    public GenericAcknowledgmentType queryNotification(QueryNotificationType queryNotification, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public QueryResultConfirmedType queryResultSync(QueryResultType queryResultSync, Holder<CommonHeaderType> header) throws Error {
        return null;
    }


    @Override
    public QuerySummaryConfirmedType querySummarySync(QueryType querySummarySync, Holder<CommonHeaderType> header) throws Error {
        return null;
    }

    @Override
    public GenericAcknowledgmentType queryRecursive(QueryType queryRecursive, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }


    @Override
    public GenericAcknowledgmentType querySummary(QueryType querySummary, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public GenericAcknowledgmentType queryResult(QueryResultType queryResult, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public QueryNotificationConfirmedType queryNotificationSync(QueryNotificationType queryNotificationSync, Holder<CommonHeaderType> header) throws Error {
        return null;
    }

}
