package com.elka.api;

import com.elka.storage.Credentials;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class VKApi {

    private static final String API_URL = "http://vk.com/api.php";
    Credentials credentials;

    public VKApi(Credentials credentials) {
        this.credentials = credentials;
    }

    private static String sign(Map<String, Object> params, Credentials credentials) {
        String toSign = credentials.getSuid();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            toSign += entry.getKey() + "=" + entry.getValue();
        }
        toSign += credentials.getSecret();
        String md5Hex = DigestUtils.md5Hex(toSign);
        params.put("sig", md5Hex);
        params.put("sid", credentials.getSid());
        return md5Hex;
    }

    private static Map<String, Object> generateParams(Credentials credentials, String method) {
        Map<String, Object> params = new TreeMap<>();
        params.put("api_id", credentials.getApiId());
        params.put("format", "json");
        params.put("method", method);
        params.put("rnd", new Random().nextInt(5500) + 3500);
        params.put("v", "3.0");
        return params;
    }

    private JSONObject sendRequest(Map<String, Object> params) throws IOException, JSONException {
        Form form = Form.form();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            form.add(entry.getKey(), entry.getValue().toString());
        }
        Content content = Request.Post(API_URL).bodyForm(form.build()).execute().returnContent();
        String responseString = content.asString();
        JSONObject result = new JSONObject(responseString);
        if (result.has("error") && result.optJSONObject("error").optInt("error_code") == 3) {
            credentials.setInvalid(true);
            throw new JSONException("Credentials are invalid: " + responseString);
        }
        return result;
    }

    public JSONObject getAppFriends() throws IOException, JSONException {
        Map<String, Object> params = generateParams(credentials, "friends.getAppUsers");
        sign(params, credentials);
        return sendRequest(params);
    }

    public JSONObject getAreFriends(List<String> friends) throws IOException, JSONException {
        Map<String, Object> params = generateParams(credentials, "friends.areFriends");
        params.put("v", "5.16");
        params.put("need_sign", 1);
        params.put("user_ids", StringUtils.join(friends, ","));
        sign(params, credentials);
        return sendRequest(params);
    }

    public JSONObject getUsers(List<String> friends) throws IOException, JSONException {
        Map<String, Object> params = generateParams(credentials, "users.get");
        params.put("fields", "uid, photo");
        params.put("uids", StringUtils.join(friends, ","));
        sign(params, credentials);
        return sendRequest(params);
    }

    public JSONArray getFriends(String userId) throws IOException, JSONException {
        Map<String, Object> params = generateParams(credentials, "friends.get");
        params.put("fields", "photo");
        params.put("user_id", userId);
        params.put("count", 5000);
        params.put("offset", 0);
        params.put("v", "5.34");
        sign(params, credentials);
        return sendRequest(params).getJSONObject("response").getJSONArray("items");
    }
}
