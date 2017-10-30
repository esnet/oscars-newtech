package net.es.oscars.soap.beans;


import lombok.Data;
import net.es.nsi.lib.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;


@Data
public abstract class GenericRequest {
    protected CommonHeaderType inHeader;
    protected CommonHeaderType outHeader;
}
