package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCacheEntry {
    private String device;
    private String config;
    private Instant lastUpdated;

}
