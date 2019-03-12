import { observable, action } from "mobx";

class HeldStore {
    @observable held = {
        current: []
    };

    @action setCurrent(current) {
        this.held.current = current;
    }
}

export default new HeldStore();
