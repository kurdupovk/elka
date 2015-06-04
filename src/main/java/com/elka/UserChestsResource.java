package com.elka;

import com.elka.storage.UserChestsStorage;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;

@Path("userchests")
public class UserChestsResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt() {
        return new JSONArray(UserChestsStorage.getInstance().values()).toString();
    }
}
