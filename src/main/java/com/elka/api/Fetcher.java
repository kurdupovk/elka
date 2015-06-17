package com.elka.api;

import com.elka.storage.Credentials;
import com.elka.storage.FriendsStorage;
import com.elka.storage.UserChestsStorage;
import java.io.IOException;
import java.util.Date;
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
            JSONArray users = FriendsStorage.getInstance().getFriends();
            if (users == null) {
                LOG.log(Level.INFO, "Defined users must not be null");
                return;
            }
            if (credentials.isInvalid()) {
                LOG.log(Level.INFO, "Current credentials are invalid. No fetch users chetsts process.");
                return;
            }
            LOG.info("Starting fetching users chests.");
            try {
                ElkaApi elkaApi = new ElkaApi(credentials);
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String userId = user.getString("userId");
                    try {
                        JSONObject friend = null;
                        if (userId.equals("santa")) {
                            friend = elkaApi.getSantaChest();
                            JSONObject userJSON = new JSONObject().put("loginTime", new Date().getTime() / 1000);
                            friend.getJSONObject("data").put("user", userJSON);
                        } else {
                            friend = elkaApi.getFriend(userId);
                        }
                        JSONObject chest = friend.getJSONObject("data").getJSONObject("chest");
                        chest.put("photo", user.getString("photo"));
                        chest.put("name", user.getString("name"));
                        chest.put("logintime", friend.getJSONObject("data").getJSONObject("user").getLong("loginTime"));
                        storage.put(userId, chest);
                    } catch (JSONException ex) {
                        LOG.log(Level.SEVERE, ex.getMessage());
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Can not request user - " + user.optString("name") + ". Message: " + ex.getMessage());
                    }
                }
            } finally {
                LOG.info("Users chests fetching is finished.");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "There is an unhandled exception", ex);
        }
    }
}
