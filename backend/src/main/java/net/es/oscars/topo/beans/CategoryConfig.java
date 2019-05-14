package net.es.oscars.topo.beans;

import lombok.Data;
import net.es.oscars.topo.enums.TagInput;

import java.util.List;

@Data
public class CategoryConfig {
    private String category;

    private String description;
    private TagInput input;
    private boolean mandatory;
    private boolean multivalue;
    private List<String> options;


}
