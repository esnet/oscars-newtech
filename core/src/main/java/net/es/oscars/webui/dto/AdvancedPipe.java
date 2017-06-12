package net.es.oscars.webui.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvancedPipe {
    private String azbw;
    private String zabw;
    private String a;
    private String z;
    private List<String> azERO;
    private List<String> zaERO;
    private List<String> blacklist;
    private Boolean palindromicPath;
    private String survivabilityType;
    private String numPaths;
}