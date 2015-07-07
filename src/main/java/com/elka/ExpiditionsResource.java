package com.elka;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
@Path("expiditions")
public class ExpiditionsResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public String setExpiditions(@FormParam("ids") String ids) {
        String[] idsArray = StringUtils.split(ids, ",");
        if (idsArray.length != 0) {
            
        }
        Map<String, Object> result = new HashMap<>();
        result.put("saved", true);
        return new JSONObject(result).toString();
    }
}
