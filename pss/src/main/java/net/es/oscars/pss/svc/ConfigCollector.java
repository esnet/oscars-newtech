package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.DeviceConfigRequest;
import net.es.oscars.dto.pss.cmd.DeviceConfigResponse;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.prop.CollectorProps;
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
public class ConfigCollector {
    private RancidRunner rancidRunner;
    private RouterConfigBuilder rcb;
    private CollectorProps verifierProps;
    private PssProps pssProps;
    private Map<String, ConfigCacheEntry> cache = new HashMap<>();

    @Autowired
    public ConfigCollector(RancidRunner rancidRunner, PssProps pssProps, CollectorProps verifierProps, RouterConfigBuilder rcb) {
        this.rancidRunner = rancidRunner;
        this.verifierProps = verifierProps;
        this.rcb = rcb;
        this.pssProps = pssProps;
    }

    public DeviceConfigResponse getConfig(DeviceConfigRequest req) throws VerifyException {

        Duration lifetime = Duration.of(verifierProps.getCacheLifetime(), SECONDS);
        Instant now = Instant.now();

        PssProfile pssProfile = PssProfile.find(pssProps, req.getProfile());
        RancidProps props = pssProfile.getRancid();

        if (!props.getPerform()) {
            log.info("configured to not actually run rancid");
            return DeviceConfigResponse.builder()
                    .lastUpdated(now)
                    .model(req.getModel())
                    .device(req.getDevice())
                    .asJson("{}")
                    .build();
        }

        Instant lastUpdated;
        String config;

        boolean mustUpdate = true;
        if (cache.keySet().contains(req.getDevice())) {
            lastUpdated = cache.get(req.getDevice()).getLastUpdated();
            Instant expiration = lastUpdated.plus(lifetime);
            if (now.isBefore(expiration)) {
                log.info("device config current in cache for "+req.getDevice());
                mustUpdate = false;
            }
            log.info("device config stale in cache for "+req.getDevice());
        } else {
            log.info("missing device from cache for "+req.getDevice());
        }

        if (mustUpdate) {
            config = this.collectConfig(req.getDevice(), req.getModel(), req.getProfile());
            lastUpdated = Instant.now();

            ConfigCacheEntry entry = ConfigCacheEntry.builder()
                    .config(config)
                    .device(req.getDevice())
                    .lastUpdated(lastUpdated)
                    .build();
            cache.put(req.getDevice(), entry);


        } else {
            config = cache.get(req.getDevice()).getConfig();
            lastUpdated = cache.get(req.getDevice()).getLastUpdated();
        }

        return DeviceConfigResponse.builder()
                .lastUpdated(lastUpdated)
                .model(req.getModel())
                .device(req.getDevice())
                .asJson(config)
                .build();

    }


    public String collectConfig(String deviceUrn, DeviceModel model, String profile) throws VerifyException {
        String config = null;
        try {
            RancidArguments args = rcb.getConfig(deviceUrn, model, profile);
            RancidResult res = rancidRunner.runRancid(args, profile);
            String output = res.getOutput();
            String[] lines = output.split("\\r?\\n");
            switch (model) {
                case ALCATEL_SR7750:
                    config = "{\"configuration\":\"none\"}";
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
                                            JSONObject obj = XML.toJSONObject(configStr);
                                            JSONObject rpc = (JSONObject) obj.get("rpc-reply");
                                            JSONObject cfg = (JSONObject) rpc.get("configuration");
                                            config = cfg.toString(2);
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
