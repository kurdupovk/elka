package com.elka;

import com.elka.storage.ApplicationStorage;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
@Path("expiditions")
public class ExpeditionsResource {

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public String addExpidition(@FormParam("id") String id) {
        boolean areSaved = ApplicationStorage.getInstance().getExpeditions().tryToAdd(id);
        Map<String, Object> result = new HashMap<>();
        result.put("saved", areSaved);
        if (!areSaved) {
            result.put("msg", "Count of deers greater than available count of deers.");
        }
        return new JSONObject(result).toString();
    }

    @POST
    @Path("/del")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public String removeExpidition(@FormParam("id") String id) {
        boolean areSaved = ApplicationStorage.getInstance().getExpeditions().tryToRemove(id);
        Map<String, Object> result = new HashMap<>();
        result.put("saved", areSaved);
        return new JSONObject(result).toString();
    }
}
