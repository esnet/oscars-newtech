package net.es.oscars.snp.beans;

import lombok.Builder;
import lombok.Data;
import net.es.oscars.app.beans.Delta;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;

import java.util.HashMap;

@Data
@Builder
public class CmpDelta {
    private Delta<VlanJunction> junctionDelta;
    private Delta<VlanFixture> fixtureDelta;
    private Delta<VlanPipe> pipeDelta;

    public static CmpDelta newEmptyDelta() {
        Delta<VlanJunction> junctionDelta = Delta.<VlanJunction>builder()
                .added(new HashMap<>())
                .removed(new HashMap<>())
                .modified(new HashMap<>())
                .unchanged(new HashMap<>())
                .build();

        Delta<VlanFixture> fixtureDelta = Delta.<VlanFixture>builder()
                .added(new HashMap<>())
                .removed(new HashMap<>())
                .modified(new HashMap<>())
                .unchanged(new HashMap<>())
                .build();

        Delta<VlanPipe> pipeDelta = Delta.<VlanPipe>builder()
                .added(new HashMap<>())
                .removed(new HashMap<>())
                .modified(new HashMap<>())
                .unchanged(new HashMap<>())
                .build();

        CmpDelta result = CmpDelta.builder()
                .fixtureDelta(fixtureDelta)
                .pipeDelta(pipeDelta)
                .junctionDelta(junctionDelta)
                .build();

        return result;
    }
}
