package com.elka;

import com.elka.api.Fetcher;
import com.elka.storage.Credentials;
import com.elka.storage.CredentialsStorage;
import com.elka.storage.UserChestsStorage;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.json.JSONException;

/**
 *
 * @author kkurdupov
 */
@Path("credentials")
public class CredentialsResource {

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setCredentials(@FormParam("cred") String cred, @DefaultValue("false") @FormParam("forsefetch") boolean forceFetch) {
        Credentials credentials = null;
        try {
            credentials = Credentials.parseJSON(cred);
        } catch (JSONException ex) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"json invalid\"}").build());
        }
        CredentialsStorage.getInstance().add(credentials);
        if (forceFetch) {
            new Fetcher(credentials).fetchUsersTo(UserChestsStorage.getInstance());
        }
        return Response.ok("{\"status\":\"saved\"}").cookie(NewCookie.valueOf("vasya=petya")).build();
    }
}
