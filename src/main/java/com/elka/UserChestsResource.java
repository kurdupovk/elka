package com.elka;

import com.elka.storage.ApplicationStorage;
import com.elka.storage.Credentials;
import com.elka.storage.CredentialsStorage;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("userchests")
public class UserChestsResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserChests() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> credentialsMap = new HashMap<>();
        Map<String, Object> expedtions = new HashMap<>();
        Credentials credentials = CredentialsStorage.getInstance().get();
        credentialsMap.put("valid", credentials != null ? !credentials.isInvalid() : null);
        result.put("credentials", new JSONObject(credentialsMap));
        result.put("chests", new JSONArray(ApplicationStorage.getInstance().getUserChests().values()));
        expedtions.put("active", new JSONArray(ApplicationStorage.getInstance().getExpeditions().getActive()));
        expedtions.put("saved", new JSONArray(ApplicationStorage.getInstance().getExpeditions().getRepeatable()));
        result.put("expiditions", new JSONObject(expedtions));
        return new JSONObject(result).toString();
    }
}
