import { observable, action } from "mobx";

class CommonStore {
    @observable nav = {
        active: ""
    };

    @observable version = {
        frontend: __VERSION__,
        backend: "unknown"
    };

    @observable alerts = [];

    @action setActiveNav(a) {
        this.nav.active = a;
    }

    @action setVersion(key, val) {
        this.version[key] = val;
    }

    @action removeAlert(alert) {
        let idxToRemove = -1;
        this.alerts.map((entry, index) => {
            if (entry.id === alert.id) {
                idxToRemove = index;
            }
        });
        if (idxToRemove > -1) {
            this.alerts.splice(idxToRemove, 1);
        }
    }

    @action addAlert(alert) {
        this.alerts.push(alert);
    }
}
export default new CommonStore();
