import { observable, action } from "mobx";

class ModalStore {
    @observable
    modals = observable.map(
        {
            editEnding: false,
            editDescription: false,
            editFixture: false,
            editJunction: false,
            editPipe: false,
            designHelp: false,
            addFixture: false,
            designErrors: false,
            connectionErrors: false,
            connection: false,
            disconnected: false,
            userAdmin: false
        },
        { name: "modalNames" }
    );

    @action
    openModal(type) {
        this.closeModals();
        this.modals.set(type, true);
    }

    @action
    closeModal(type) {
        this.modals.set(type, false);
    }

    @action
    closeModals() {
        this.modals.forEach((value, key) => {
            this.modals.set(key, false);
        });
    }
}

export default new ModalStore();
