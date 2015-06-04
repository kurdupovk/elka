package com.elka.api;

import com.elka.storage.Credentials;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class ElkaApi {

    private static final String API_URL = "http://elka2015.ereality.org";

    public static JSONObject getFriend(String friendId, Credentials credentials) throws IOException {
        final String url = API_URL + "/friend/getFriend/";

        Map<String, Object> data = new HashMap<>();
        data.put("sessionKey", credentials.getSessionKey());
        data.put("aid", credentials.getAppId());
        data.put("uid", credentials.getUid());
        data.put("version", credentials.getVersion());
        data.put("authKey", credentials.getAuthKey());
        data.put("suid", credentials.getSuid());
        Map<String, Object> params = new HashMap<>();
        params.put("userId", friendId);
        params.put("chestId", 1);
        data.put("params", new JSONObject(params));
        JSONObject sign = SignGenerator.getSignRequest(new JSONObject(data), url);
        Response response;
        try {
            response = Request.Post(url).bodyByteArray(sign.toString().getBytes(),
                    ContentType.APPLICATION_FORM_URLENCODED).execute();
            return new JSONObject(response.returnContent().asString());
        }  catch (JSONException ex) {
            Logger.getLogger(ElkaApi.class.getName()).log(Level.SEVERE, "JSON exception parsing", ex);
        }
        return null;
    }
}
