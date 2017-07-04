package net.es.oscars.helpers;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.dto.IntRange;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class IntRangeParsingTest {

    @Test
    @Category(QuickTests.class)
    public void testSetToRangeList() {
        Set<Integer> ints;
        List<IntRange> ranges;
        ints = new HashSet<>();
        ints.add(1);
        ints.add(2);

        ranges = IntRangeParsing.intRangesFromIntegers(ints);
        assert ranges.size() == 1;
        assert ranges.get(0).getFloor().equals(1);
        assert ranges.get(0).getCeiling().equals(2);
        ints.clear();
        ints.add(1);
        ints.add(3);


        ranges = IntRangeParsing.intRangesFromIntegers(ints);
        assert ranges.size() == 2;
        assert ranges.get(0).getFloor().equals(1);
        assert ranges.get(1).getCeiling().equals(3);

        ints.clear();
        ints.add(1);
        ints.add(2);
        ints.add(4);
        ints.add(5);
        ints.add(8);
        ints.add(9);
        ranges = IntRangeParsing.intRangesFromIntegers(ints);

        assert ranges.size() == 3;
        assert ranges.get(0).getFloor().equals(1);
        assert ranges.get(2).getCeiling().equals(9);

    }
}
