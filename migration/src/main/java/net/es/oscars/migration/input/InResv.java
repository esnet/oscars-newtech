package net.es.oscars.migration.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InResv {
    protected InSchedule schedule;
    protected InCmp cmp;
    protected InMisc misc;
    protected String gri;
    protected Integer mbps;
    protected InPss pss;

}
