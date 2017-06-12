package net.es.oscars.webui.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MinimalBwAvailRequest
{
    private String startTime;
    private String endTime;
    private List<String> azERO;
    private List<String> zaERO;
    private Integer azBandwidth;
    private Integer zaBandwidth;
}
