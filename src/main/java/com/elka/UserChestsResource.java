package com.elka;

import com.elka.api.UsersChestsFetcher;
import com.elka.storage.ApplicationStorage;
import com.elka.storage.Credentials;
import com.elka.storage.CredentialsStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("userchests")
public class UserChestsResource {

    private static final Logger LOG = Logger.getLogger(UserChestsResource.class.getName());

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
        expedtions.put("earnedMoney", ApplicationStorage.getInstance().getExpeditions().getEarnedMoney());
        result.put("expiditions", new JSONObject(expedtions));
        result.put("screenId", ApplicationStorage.getInstance().getConfig().getScreenId());
        return new JSONObject(result).toString();
    }

    @POST
    @Path("/screen")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public String setScreen(@FormParam("id") int id) {
        ApplicationStorage.getInstance().getConfig().setScreenId(id);
        boolean setForAll = true;
        for (Map.Entry<String, JSONObject> entry : ApplicationStorage.getInstance().getFriends().entrySet()) {
            JSONObject user = entry.getValue();
            try {
                user.put("screenId", id);
            } catch (JSONException ex) {
                LOG.log(Level.SEVERE, "Screen id " + id + " can not be set for user - " + user.toString(), ex);
                setForAll = false;
            }
        }
        Map<String, Object> result = new HashMap();
        result.put("setForAll", setForAll);
        return new JSONObject(result).toString();
    }
}
