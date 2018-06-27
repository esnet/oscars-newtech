package net.es.oscars.soap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.Error;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;

import javax.xml.ws.Holder;

@Slf4j
public class NsiProvider implements ConnectionProviderPort {

    @Override
    public GenericAcknowledgmentType provision(GenericRequestType provision, Holder<CommonHeaderType> header) throws ServiceException {
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
    public GenericAcknowledgmentType reserveCommit(GenericRequestType reserveCommit, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public GenericAcknowledgmentType queryNotification(QueryNotificationType queryNotification, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public GenericAcknowledgmentType terminate(GenericRequestType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public ReserveResponseType reserve(ReserveType reserve, Holder<CommonHeaderType> header) throws ServiceException {
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(reserve);
            log.debug(pretty);
             pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(header);
            log.debug(pretty);

        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public QueryResultConfirmedType queryResultSync(QueryResultType queryResultSync, Holder<CommonHeaderType> header) throws Error {
        return null;
    }

    @Override
    public GenericAcknowledgmentType release(GenericRequestType release, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public GenericAcknowledgmentType reserveAbort(GenericRequestType reserveAbort, Holder<CommonHeaderType> header) throws ServiceException {
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
