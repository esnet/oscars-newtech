import {UserContext} from "../index";
import { useContext} from "react";

export default function LoggedIn() {
    const userCtx = useContext(UserContext);

    if (localStorage.getItem("loggedin.username") == null) {
        if (userCtx.loggedin.username !== "") {
            userCtx.logout();
        }
    } else {
        let admin = localStorage.getItem("loggedin.admin");
        if (admin === "true") {
            userCtx.setLoggedinAdmin(true);
        } else {
            userCtx.setLoggedinAdmin(false);
        }
        userCtx.setLoggedinToken(localStorage.getItem("loggedin.token"));
        userCtx.setLoggedinUsername(localStorage.getItem("loggedin.username"));
    }
    return null;

}
