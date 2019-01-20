package net.es.oscars.snp.ent;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.snp.beans.ConfigNodeModifyType;

import java.util.Set;

@Slf4j
@Data
@Builder
public class Modify {
    ConfigNodeModifyType type;
    DeviceConfigNode node;
    Set<String> upstreamNodeIds;

    String mutate;
    ConfigStatus configState;
}