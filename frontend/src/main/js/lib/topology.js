class Topology {
    adjacent(a, b, adjacencies) {
        for (let adjcy of adjacencies) {
            if (a === adjcy.a && b === adjcy.b) {
                return "A_TO_B";
            }
            if (a === adjcy.b && b === adjcy.y) {
                return "B_TO_Y";
            }
            if (a === adjcy.y && b === adjcy.z) {
                return "Y_TO_Z";
            }
        }
        return "NONE";
    }

    urnType(urn, adjacencies) {
        for (let adjcy of adjacencies) {
            if (urn === adjcy.a || urn === adjcy.z) {
                return "DEVICE";
            }
            if (urn === adjcy.b || urn === adjcy.y) {
                return "PORT";
            }
        }
        return "NONE";
    }
}

export default new Topology();
