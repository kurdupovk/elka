package com.elka.api;

import com.elka.storage.ApplicationStorage;
import com.elka.storage.CredentialsStorage;
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
public class FriendsFriendsFetcher {

    private static final Logger LOG = Logger.getLogger(FriendsFriendsFetcher.class.getName());
    private CredentialsStorage credentialsStorage;

    public FriendsFriendsFetcher(CredentialsStorage credentials) {
        this.credentialsStorage = credentials;
    }

    public void fetchTo(ApplicationStorage applicationStorage) {
        if (credentialsStorage.isEmpty()) {
            return;
        }
        if (credentialsStorage.get().isInvalid()) {
            LOG.log(Level.WARNING, "Current credentials are invalid. No fetch user profiles process.");
            return;
        }
        VKApi vkApi = new VKApi(credentialsStorage.get());
        LOG.info("Starting fetching friends of friends.");
        try {
            for (JSONObject friend : applicationStorage.getFriends().values()) {
                String sUserId = friend.getString("sUserId");
                if (sUserId.equals("santa")) {
                    continue;
                }
                JSONArray friendsFriend = vkApi.getFriends(sUserId);
                for (int j = 0; j < friendsFriend.length(); j++) {
                    JSONObject friendFriend = friendsFriend.getJSONObject(j);
                    JSONObject result = new JSONObject();
                    result.put("name", friendFriend.getString("first_name") + " " + friendFriend.getString("last_name"));
                    result.put("photo", friendFriend.getString("photo"));
                    result.put("id", friendFriend.getString("id"));
                    applicationStorage.getFriendsOfFriends().put(friendFriend.getString("id"), result);
                }
            }
        } catch (JSONException ex) {
            LOG.warning(ex.getMessage());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            LOG.info("Fetching friends of friends finished. Count - " + applicationStorage.getFriendsOfFriends().size());
        }
    }
}
