package com.elka.api;

import com.elka.storage.Credentials;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class VKApi {

    private static final String API_URL = "http://vk.com/api.php";

    private static String sign(Map<String, Object> params, Credentials credentials) {
        String toSign = credentials.getSuid();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            toSign += entry.getKey() + "=" + entry.getValue();
        }
        toSign += credentials.getSecret();
        return DigestUtils.md5Hex(toSign);
    }

    public static JSONObject getAppUsers(Credentials credentials) {
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", credentials.getAppId());
        params.put("format", "json");
        params.put("method", "friends.getAppUsers");
        params.put("rnd", 5123);
        params.put("v", "3.0");
        String sig = sign(params, credentials);
        params.put("sig", sig);
        Form form = Form.form();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            form.add(entry.getKey(), entry.getValue().toString());
        }
        try {
            Content content = Request.Post(API_URL).bodyForm(form.build()).execute().returnContent();
            return new JSONObject(content.asString());
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }
        return null;


    }
}
