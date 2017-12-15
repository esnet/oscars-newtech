package net.es.oscars.soap;

import lombok.extern.slf4j.Slf4j;
import net.es.nsi.lib.soap.gen.nsi_2_0.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.soap.beans.SimpleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestProcessor {
    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private ConnService connSvc;
    @Autowired
    private PSSAdapter pssAdapter;


    public void processSimple(SimpleRequest request) throws ServiceException {
        String connectionId = request.getConnectionId();

        Connection c = connRepo.findByConnectionId(connectionId).orElseThrow(() ->
                new ServiceException("Connection not found!")
        );

        try {
            switch (request.getRequestType()) {
                case RESERVE_ABORT:
                    connSvc.cancel(c);
                    this.callback(request);
                    break;
                case RESERVE_COMMIT:
                    connSvc.commit(c);
                    this.callback(request);
                    break;
                case PROVISION:
                    pssAdapter.build(c);
                    this.callback(request);
                    break;
                case RELEASE:
                    pssAdapter.dismantle(c);
                    this.callback(request);
                    break;
                case TERMINATE:
                    connSvc.cancel(c);
                    this.callback(request);
                    break;
            }
        } catch (PSSException ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
            throw new ServiceException("PSS exception!");
        }


        CommonHeaderType inHeader = request.getInHeader();
        CommonHeaderType outHeader = this.makeOutHeader(inHeader);
        request.setOutHeader(outHeader);
    }

    private void callback(SimpleRequest req) {
        String replyTo = req.getInHeader().getReplyTo();
        // TODO
    }

    private CommonHeaderType makeOutHeader(CommonHeaderType inHeader) {
        CommonHeaderType outHeader = new CommonHeaderType();
        outHeader.setCorrelationId(inHeader.getCorrelationId());
        outHeader.setProtocolVersion(inHeader.getProtocolVersion());
        outHeader.setProviderNSA(inHeader.getProviderNSA());
        outHeader.setRequesterNSA(inHeader.getRequesterNSA());
        return outHeader;
    }

}
