package net.es.oscars.pss.beans;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.topo.DeviceModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEntry {
    String device;
    DeviceModel model;
}
