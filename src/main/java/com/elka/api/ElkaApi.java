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
    private final Credentials credentials;

    public ElkaApi(Credentials credentials) {
        this.credentials = credentials;
    }

    private static Map<String, Object> defaultRequestData(Credentials cred) {
        Map<String, Object> data = new HashMap<>();
        data.put("sessionKey", cred.getSessionKey());
        data.put("aid", cred.getAppId());
        data.put("uid", cred.getUid());
        data.put("version", cred.getVersion());
        data.put("authKey", cred.getAuthKey());
        data.put("suid", cred.getSuid());
        return data;
    }

    private static Map<String, Object> createParams(String userId, Integer chestId, Integer screenId, String sUserId) {
        Map<String, Object> params = new HashMap<>();
        if (userId != null) {
            params.put("userId", userId);
        }
        if (chestId != null) {
            params.put("chestId", chestId);
        }
        if (screenId != null) {
            params.put("screenId", screenId);
        }
        if (sUserId != null) {
            params.put("sUserId", sUserId);
        }
        return params;
    }

    private JSONObject sendRequest(String url, JSONObject sign) throws IOException, JSONException {
        Response response = Request.Post(url).bodyByteArray(sign.toString().getBytes(), ContentType.APPLICATION_FORM_URLENCODED).execute();
        String responseString = response.returnContent().asString();
        JSONObject responseJson = new JSONObject(responseString);
        if (responseJson.has("error")) {
            if (responseJson.getJSONObject("error").getString("text").contains("Authkey is invalid")) {
                credentials.setInvalid(true);
            }
            throw new JSONException(responseString);
        }
        JSONObject server = responseJson.optJSONObject("server");
        if(server != null && server.has("reload")){
            credentials.setInvalid(true);
        }
        return responseJson;
    }

    public JSONObject getFriend(String friendId) throws IOException, JSONException {
        final String url = API_URL + "/friend/getFriend/";

        Map<String, Object> data = defaultRequestData(credentials);
        Map<String, Object> params = createParams(friendId, 1, null, null);
        data.put("params", new JSONObject(params));
        JSONObject sign = SignGenerator.getSignRequest(new JSONObject(data), url);
        return sendRequest(url, sign);
    }

    public JSONObject getSantaChest() throws IOException, JSONException {
        final String url = API_URL + "/friend/getChest/";

        Map<String, Object> data = defaultRequestData(credentials);
        Map<String, Object> params = createParams(null, 2, null, null);
        data.put("params", new JSONObject(params));
        JSONObject sign = SignGenerator.getSignRequest(new JSONObject(data), url);
        return sendRequest(url, sign);
    }

    /*
     * {"params":{"userId":2883430,"sign":"9935c9882fbb0e8591995ce63ae2d655",
     * "screenId":1,"chestId":1,"sUserId":"12482981"},"sessionKey":"59555fb770e03034a5ddefa1bec6c3ea",
     * "aid":"4606044","uid":"6591130","version":11,"sign":"5148d7f9f613362a5d30573a823a58aa",
     * "authKey":"e87047d9d5aac0a66cfb477c36120568","suid":"12143235"}
     */
    public JSONObject openChest(String friendId, String sFrindUserId) throws IOException, JSONException {
        final String url = API_URL + "/friend/openChest/";
        Map<String, Object> data = defaultRequestData(credentials);
        Map<String, Object> params = createParams(friendId, 1, 1, sFrindUserId);
        data.put("params", new JSONObject(params));
        JSONObject sign = SignGenerator.getSignRequest(new JSONObject(data), url);
        return sendRequest(url, sign);
    }
}
