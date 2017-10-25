package net.es.oscars.soap;

import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.requester.ConnectionRequesterPort;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;

import javax.xml.ws.Holder;

public class NsiRequester implements ConnectionRequesterPort {
    @Override
    public GenericAcknowledgmentType reserveFailed(GenericFailedType reserveFailed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType querySummaryConfirmed(QuerySummaryConfirmedType querySummaryConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType provisionConfirmed(GenericConfirmedType provisionConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType error(GenericErrorType error, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType terminateConfirmed(GenericConfirmedType parameters, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType releaseConfirmed(GenericConfirmedType releaseConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType errorEvent(ErrorEventType errorEvent, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType dataPlaneStateChange(DataPlaneStateChangeRequestType dataPlaneStateChange, Holder<CommonHeaderType> header) throws ServiceException {
        return null;
    }

    @Override
    public GenericAcknowledgmentType reserveAbortConfirmed(GenericConfirmedType reserveAbortConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType messageDeliveryTimeout(MessageDeliveryTimeoutRequestType messageDeliveryTimeout, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveCommitFailed(GenericFailedType reserveCommitFailed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType queryNotificationConfirmed(QueryNotificationConfirmedType queryNotificationConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType queryResultConfirmed(QueryResultConfirmedType queryResultConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveCommitConfirmed(GenericConfirmedType reserveCommitConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveTimeout(ReserveTimeoutRequestType reserveTimeout, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType reserveConfirmed(ReserveConfirmedType reserveConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType queryRecursiveConfirmed(QueryRecursiveConfirmedType queryRecursiveConfirmed, Holder<CommonHeaderType> header) throws ServiceException {
        return new GenericAcknowledgmentType();
    }
}
