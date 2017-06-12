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
public class MinimalPipe {
    private String bw;
    private String a;
    private String z;
    private List<String> azERO;
    private List<String> zaERO;
}
