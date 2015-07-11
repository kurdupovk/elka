package com.elka.api;

import com.elka.storage.CredentialsStorage;
import com.elka.storage.ApplicationStorage;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class UsersChestsFetcher {

    private static final Logger LOG = Logger.getLogger(UsersChestsFetcher.class.getName());
    private final CredentialsStorage credentialsStorage;
    private final ElkaApi elkaApi;

    public UsersChestsFetcher(CredentialsStorage credentials) {
        this.credentialsStorage = credentials;
        this.elkaApi = new ElkaApi(credentialsStorage.get());
    }

    public void fetchTo(ApplicationStorage applicationStorage) {
        try {
            if (credentialsStorage.isEmpty()) {
                return;
            }
            Collection<JSONObject> appFriends = applicationStorage.getFriends().values();
            if (credentialsStorage.get().isInvalid()) {
                LOG.log(Level.WARNING, "Current credentials are invalid. No fetch users chetsts process.");
                return;
            }
            if (appFriends.size() == 0) {
                return;
            }
            LOG.info("Starting fetching users chests.");
            try {
                for (JSONObject appFriend : appFriends) {
                    storeFriendChest(applicationStorage, appFriend);
                }
            } finally {
                LOG.info("Users chests fetching is finished.");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "There is an unhandled exception", ex);
        }
    }

    private JSONObject getFriendChest(ApplicationStorage applicationStorage, JSONObject friend) throws JSONException {
        JSONObject chest = friend.getJSONObject("data").getJSONObject("chest");
        chest.put("logintime", friend.getJSONObject("data").getJSONObject("user").getLong("loginTime"));
        String openedByUserId = chest.getString("user");
        JSONObject friendOfFriend = applicationStorage.getFriendsOfFriends().get(openedByUserId);
        if (friendOfFriend != null) {
            JSONObject openedBy = new JSONObject();
            openedBy.put("userId", openedByUserId);
            openedBy.put("name", friendOfFriend.getString("name"));
            openedBy.put("photo", friendOfFriend.getString("photo"));
            chest.put("openedBy", openedBy);
            chest.remove("user");
        }
        return chest;
    }

    private void storeFriendChest(ApplicationStorage applicationStorage, JSONObject appFriend) throws JSONException {
        String appFriendId = appFriend.getString("userId");
        try {
            JSONObject friend = null;
            if (appFriendId.equals("santa")) {
                friend = elkaApi.getSantaChest();
                JSONObject userJSON = new JSONObject().put("loginTime", new Date().getTime() / 1000);
                friend.getJSONObject("data").put("user", userJSON);
            } else {
                friend = elkaApi.getFriend(appFriendId);
            }
            JSONObject friendChest = getFriendChest(applicationStorage, friend);
            friendChest.put("photo", appFriend.getString("photo"));
            friendChest.put("name", appFriend.getString("name"));
            applicationStorage.getUserChests().put(appFriendId, friendChest);
        } catch (JSONException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Can not request user - " + appFriend.optString("name") + ". Message: " + ex.getMessage());
        }
    }
}
