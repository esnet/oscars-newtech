export default class VisUtils {
    static visFromERO(ero) {
        let triplets = Math.floor(ero.length / 3);
        let edges = [];
        let nodes = [];

        if (triplets === 0) {
            edges.push(ero[0] + " -- " + ero[1]);
        } else {
            for (let i = 0; i < triplets; i++) {
                let offset = 3 * i;
                nodes.push(ero[offset + 2]);
                edges.push(ero[offset] + " -- " + ero[offset + 1]);
            }
        }
        return {
            edges: edges,
            nodes: nodes
        };
    }

    static mergeItems(incoming, datasource) {
        let itemsToRemove = [];
        let itemsToAdd = [];
        let itemsToUpdate = [];

        datasource.getIds().map(d_id => {
            let found = false;
            incoming.map(item => {
                if (d_id === item.id) {
                    itemsToUpdate.push(item);
                    found = true;
                }
            });
            if (!found) {
                itemsToRemove.push(d_id);
            }
        });

        incoming.map(item => {
            let found = false;
            datasource.getIds().map(d_id => {
                if (d_id === item.id) {
                    found = true;
                }
            });
            if (!found) {
                itemsToAdd.push(item);
            }
        });
        datasource.remove(itemsToRemove);
        datasource.add(itemsToAdd);
        datasource.update(itemsToUpdate);
    }
}
