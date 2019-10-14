package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateOutput {
    private String unprocessed;
    private String processed;
    private List<String> unprocessedLines;
    private List<String> processedLines;
    private Boolean hasVersion;
    private String templateVersion;

}
