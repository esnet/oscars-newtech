import { observable, action } from "mobx";

import myClient from "../agents/client";

class MapStore {
    @observable positions = observable.map({});

    @action loadPositions() {
        myClient.loadJSON({ method: "GET", url: "/api/positions" }).then(
            action(response => {
                this.positions = JSON.parse(response);
            })
        );
    }

    @observable network = {
        nodes: [],
        edges: [],
        initialized: false
    };

    @action setNetwork(nodes, edges) {
        this.network.initialized = true;
        this.network.nodes = nodes;
        this.network.edges = edges;
    }
}

export default new MapStore();
