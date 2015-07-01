package com.elka.api;

import com.elka.storage.CredentialsStorage;
import com.elka.storage.AppFriendsStorage;
import com.elka.storage.FriendsFriendsStorage;
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
public class UsersChestsFetcher {

    private static final Logger LOG = Logger.getLogger(UsersChestsFetcher.class.getName());
    private final CredentialsStorage credentialsStorage;
    private final AppFriendsStorage appFriendsStorage;
    private final FriendsFriendsStorage friendsFriendsStorage;

    public UsersChestsFetcher(CredentialsStorage credentials, AppFriendsStorage friendsStorage, FriendsFriendsStorage friendsFriendsStorage) {
        this.credentialsStorage = credentials;
        this.appFriendsStorage = friendsStorage;
        this.friendsFriendsStorage = friendsFriendsStorage;
    }

    public void fetchTo(UserChestsStorage userChestStorage) {
        try {
            if (credentialsStorage.isEmpty()) {
                return;
            }
            JSONArray appFriends = appFriendsStorage.getFriends();
            if (credentialsStorage.get().isInvalid()) {
                LOG.log(Level.WARNING, "Current credentials are invalid. No fetch users chetsts process.");
                return;
            }
            LOG.info("Starting fetching users chests.");
            try {
                ElkaApi elkaApi = new ElkaApi(credentialsStorage.get());
                for (int i = 0; i < appFriends.length(); i++) {
                    JSONObject appFriend = appFriends.getJSONObject(i);
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
                        JSONObject chest = friend.getJSONObject("data").getJSONObject("chest");
                        chest.put("photo", appFriend.getString("photo"));
                        chest.put("name", appFriend.getString("name"));
                        chest.put("logintime", friend.getJSONObject("data").getJSONObject("user").getLong("loginTime"));
                        String openedByUser = chest.getString("user");
                        JSONObject friendFriend = friendsFriendsStorage.get(openedByUser);
                        if (friendFriend != null) {
                            JSONObject openedBy = new JSONObject();
                            openedBy.put("userId", openedByUser);
                            openedBy.put("name", friendFriend.getString("name"));
                            openedBy.put("photo", friendFriend.getString("photo"));
                            chest.put("openedBy", openedBy);
                            chest.remove("user");
                        }
                        userChestStorage.put(appFriendId, chest);
                    } catch (JSONException ex) {
                        LOG.log(Level.SEVERE, ex.getMessage());
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Can not request user - " + appFriend.optString("name") + ". Message: " + ex.getMessage());
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
