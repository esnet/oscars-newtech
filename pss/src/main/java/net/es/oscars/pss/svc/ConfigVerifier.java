package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.VerifyRequest;
import net.es.oscars.dto.pss.cmd.VerifyResponse;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.prop.VerifierProps;
import net.es.oscars.pss.rancid.RancidArguments;
import net.es.oscars.pss.rancid.RancidResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@Slf4j
public class ConfigVerifier {
    private RancidRunner rancidRunner;
    private RouterConfigBuilder rcb;
    private VerifierProps verifierProps;
    private Map<String, ConfigCacheEntry> cache = new HashMap<>();

    @Autowired
    public ConfigVerifier(RancidRunner rancidRunner, VerifierProps verifierProps, RouterConfigBuilder rcb) {
        this.rancidRunner = rancidRunner;
        this.verifierProps = verifierProps;
        this.rcb = rcb;
    }

    public VerifyResponse verify(VerifyRequest req) throws VerifyException {

        Duration lifetime = Duration.of(verifierProps.getCacheLifetime(), SECONDS);
        Instant now = Instant.now();

        boolean mustUpdate = true;
        if (cache.keySet().contains(req.getDevice())) {
            Instant lastUpdated = cache.get(req.getDevice()).getLastUpdated();
            Instant expiration = lastUpdated.plus(lifetime);
            if (now.isBefore(expiration)) {
                mustUpdate = false;
            }
        }

        String config = "";
        JSONObject parsed;

        if (mustUpdate) {
            parsed = this.collectConfig(req.getDevice(), req.getModel());

        } else {
            config = cache.get(req.getDevice()).getConfig();
            parsed = cache.get(req.getDevice()).getParsed();
        }
        String error = "";


        VerifyResponse response = VerifyResponse.builder()
                .config(config)
                .device(req.getDevice())
                .model(req.getModel())
                .error(error)
                .build();
        return response;
    }

    public JSONObject collectConfig(String deviceUrn, DeviceModel model) throws VerifyException {
        JSONObject config = null;
        try {
            RancidArguments args = rcb.getConfig(deviceUrn, model);
            RancidResult res = rancidRunner.runRancid(args, deviceUrn);
            String output = res.getOutput();
            String[] lines = output.split("\\r?\\n");
            switch (model) {
                case ALCATEL_SR7750:
                    break;
                case JUNIPER_MX:
                case JUNIPER_EX:
                    String configStr = "";
                    boolean got_last_prompt = false;
                    boolean got_last_xml = false;
                    int i = 0;
                    for (String line : lines) {
                        if (line.length() > 0) {
                            if (!got_last_xml) {
                                if (!got_last_prompt) {
                                    // not great!
                                    if (line.contains("show configuration")) {
                                        got_last_prompt = true;
                                        log.info("last prompt at line:" + i);
                                    }
                                } else {
                                    // ALSO NOT GREAT
                                    configStr = configStr + line + "\n";
                                    if (line.contains("</rpc-reply>")) {
                                        try {
                                            config = XML.toJSONObject(configStr);
                                            got_last_xml = true;
                                            log.info("got last xml");
                                        } catch (JSONException ex) {
                                            log.error(ex.getMessage(), ex);
                                            throw new VerifyException("could not parse XML");
                                        }
                                    }
                                }
                            }
                        }
                        i++;
                    }
                    if (!got_last_prompt) {
                        throw new VerifyException("could not locate prompt");
                    }
                    if (!got_last_xml) {
                        throw new VerifyException("could not parse XML");
                    }
                    if (config == null) {
                        throw new VerifyException("null config!");
                    }

                    break;
                default:
                    throw new VerifyException("unknown model");
            }

        } catch (ConfigException | UrnMappingException | IOException |
                InterruptedException | ControlPlaneException |
                TimeoutException ex) {
            log.error(ex.getMessage(), ex);
            throw new VerifyException(ex.getMessage());
        }

        return config;
    }


}
