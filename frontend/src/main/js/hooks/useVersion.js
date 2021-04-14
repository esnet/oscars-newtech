import {useQuery} from "react-query";
import axios from "axios";
import accountStore from "../stores/accountStore";

const getBackendVersion = async () => {
    let token = accountStore.loggedin.token;
    const {data} = await axios.get("/api/version", {
        headers: {'Authentication': token}
    });
    return data;
};

export default function useBackendVersion() {
    return useQuery("backendVersion", getBackendVersion);
}