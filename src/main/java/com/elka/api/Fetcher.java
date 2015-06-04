package com.elka.api;

import com.elka.storage.Credentials;
import com.elka.storage.UserChestsStorage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class Fetcher {

    private static final Logger LOG = Logger.getLogger(Fetcher.class.getName());
    private final Credentials credentials;

    public Fetcher(Credentials credentials) {
        this.credentials = credentials;
    }

    public void fetchUsersTo(UserChestsStorage storage) {
        try {
            JSONArray users = UserChestsStorage.definedUsers;
            if (users == null) {
                LOG.log(Level.INFO, "Defined users must not be null");
                return;
            }
            LOG.info("Starting users fetch.");
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = null;
                try {
                    user = users.getJSONObject(i);
                    String userId = user.getString("userId");
                    JSONObject friend = ElkaApi.getFriend(userId, credentials);
                    JSONObject chest = friend.getJSONObject("data").getJSONObject("chest");
                    chest.put("photo", user.getString("photo"));
                    chest.put("name", user.getString("name"));
                    storage.put(userId, chest);
                } catch (JSONException ex) {
                    LOG.log(Level.SEVERE, "Can not parse json", ex);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Can not request user - " + user.optString("name") + ". Message: " + ex.getMessage());
                }
            }
            LOG.info("Users fetch finished.");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "There is unhandled exception", ex);
        }
    }
}
