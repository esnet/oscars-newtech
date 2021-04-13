import {useQuery} from "react-query";
import axios from "axios";
import accountStore from "../stores/accountStore";

const getMigrations = async () => {
    let token = accountStore.loggedin.token;
    const {data} = await axios.get("/protected/migration", {
        headers: {'Authentication': token}
    });
    return data;
};

export default function useMigrations() {
    return useQuery("migrations", getMigrations);
}