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
public class TemplateVersionReport {
    private String templateFilename;
    private Boolean hasVersion;
    private String templateVersion;

}
