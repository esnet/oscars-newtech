package net.es.oscars.topo.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class IntRange {
    private Integer floor;
    private Integer ceiling;

    public boolean contains(Integer i) {
        return (floor <= i && ceiling >= i);
    }

    public Set<IntRange> subtract(Integer i) throws NoSuchElementException {
        HashSet<IntRange> result = new HashSet<>();
        if (!this.contains(i)) {
            throw new NoSuchElementException("range " + this.toString() + " does not contain " + i);
        }
        // remove last one: return an empty set
        if (this.getFloor().equals(this.getCeiling())) {
            return result;
        }

        // remove ceiling or floor: return a single range
        if (this.getCeiling().equals(i)) {
            IntRange r = IntRange.builder().ceiling(i - 1).floor(this.getFloor()).build();
            result.add(r);
        } else if (this.getFloor().equals(i)) {
            IntRange r = IntRange.builder().ceiling(this.getCeiling()).floor(i + 1).build();
            result.add(r);
        } else {
            // split into two
            IntRange top = IntRange.builder().floor(this.getFloor()).ceiling(i - 1).build();
            IntRange bottom = IntRange.builder().floor(i + 1).ceiling(this.getCeiling()).build();
            result.add(top);
            result.add(bottom);
        }
        return result;
    }
}
