package com.elka.storage;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class Credentials implements Serializable{

    private String sid;
    private String sessionKey;
    private String authKey;
    private String suid;
    private String uid;
    private long appId;
    private String secret;
    private String version;
    private boolean invalid = false;

    private Credentials() {
    }

    public Credentials(String sid, String sessionKey, String authKey, String suid, String uid, long appId, String secret, String version) {
        this.sid = sid;
        this.sessionKey = sessionKey;
        this.authKey = authKey;
        this.suid = suid;
        this.uid = uid;
        this.appId = appId;
        this.secret = secret;
        this.version = version;
    }

    public static Credentials parseJSON(String credentials) throws JSONException {
        JSONObject json = new JSONObject(credentials);
        return new Credentials(json.getJSONObject("request").getString("sid"), json.getString("sessionKey"),
                json.getString("authKey"), json.getString("suid"), json.getString("uid"), json.getLong("appId"),
                json.getJSONObject("request").getString("secret"), json.getString("version"));

    }

    public String getVersion() {
        return version;
    }

    public long getAppId() {
        return appId;
    }

    public String getSid() {
        return sid;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getSuid() {
        return suid;
    }

    public String getUid() {
        return uid;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }
}
