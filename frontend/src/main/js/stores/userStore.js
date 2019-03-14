import { observable, action } from "mobx";

class UserStore {
    @observable
    editUser = {
        allUsers: [],
        user: {
            permissions: {
                adminAllowed: false
            }
        },
        password: "",
        passwordAgain: "",
        oldPassword: "",
        passwordOk: false,
        passwordValidationState: "error",
        passwordHelpText: "Password too short",
        status: "",
        changingPwd: false
    };

    @action
    toggleAdmin() {
        this.editUser.user.permissions.adminAllowed = !this.editUser.user.permissions.adminAllowed;
    }

    @action
    setParamsForEditUser(params) {
        Object.assign(this.editUser, params);
    }

    @action
    setParamsForOneUser(params) {
        Object.assign(this.editUser.user, params);
    }

    @action setPassword(value) {
        this.editUser.password = value;
    }

    @action setPasswordAgain(value) {
        this.editUser.passwordAgain = value;
    }

    @action setOldPassword(value) {
        this.editUser.oldPassword = value;
    }
}

export default new UserStore();
