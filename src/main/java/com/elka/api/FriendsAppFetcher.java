package com.elka.api;

import com.elka.storage.CredentialsStorage;
import com.elka.storage.FriendsStorage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class FriendsAppFetcher {

    private static final Logger LOG = Logger.getLogger(FriendsAppFetcher.class.getName());
    private CredentialsStorage credentialsStorage;

    public FriendsAppFetcher(CredentialsStorage credentials) {
        this.credentialsStorage = credentials;
    }

    public void fetchTo(FriendsStorage storage) {
        if (credentialsStorage.isEmpty()) {
            return;
        }
        if (credentialsStorage.get().isInvalid()) {
            LOG.log(Level.WARNING, "Current credentials are invalid. No fetch user profiles process.");
            return;
        }
        JSONArray fetchedFriends = new JSONArray();
        LOG.info("Starting fetching users profiles.");
        try {
            VKApi vkApi = new VKApi(credentialsStorage.get());
            ElkaApi elkaApi = new ElkaApi(credentialsStorage.get());
            JSONObject result = vkApi.getAppFriends();
            JSONArray ids = result.getJSONArray("response");
            List<String> friendIds = new ArrayList<>();
            for (int i = 0; i < ids.length(); i++) {
                friendIds.add(ids.getString(i));
            }
            result = elkaApi.init(friendIds);
            JSONArray friendsArray = result.getJSONObject("data").getJSONArray("friends");
            result = vkApi.getAreFriends(friendIds);
            JSONArray areFriends = result.getJSONArray("response");
            result = vkApi.getUsers(friendIds);
            JSONArray users = result.getJSONArray("response");
            boolean found = false;
            for (String friendId : friendIds) {
                JSONObject user = new JSONObject();
                for (int i = 0; i < friendsArray.length(); i++) {
                    JSONObject friend = friendsArray.getJSONObject(i);
                    if (friend.getString("sUserId").equals(friendId)) {
                        user.put("userId", friend.getString("userId"));
                        user.put("sUserId", friend.getString("sUserId"));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LOG.warning("Can not find user with id '" + friendId + "' in friends");
                    continue;
                }
                found = false;
                for (int i = 0; i < areFriends.length(); i++) {
                    JSONObject areFriend = areFriends.getJSONObject(i);
                    if (areFriend.getString("user_id").equals(friendId)) {
                        user.put("sign", areFriend.getString("sign"));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LOG.warning("Can not find user with id '" + friendId + "' in areFriends");
                    continue;
                }
                found = false;
                for (int i = 0; i < users.length(); i++) {
                    JSONObject userJson = users.getJSONObject(i);
                    if (userJson.getString("uid").equals(friendId)) {
                        user.put("name", userJson.getString("first_name") + " " + userJson.getString("last_name"));
                        user.put("photo", userJson.getString("photo"));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LOG.warning("Can not find user with id '" + friendId + "' in users");
                    continue;
                }
                fetchedFriends.put(user);
            }
            storage.setFriends(fetchedFriends);
            LOG.info("Fetched friends:");
            LOG.info(fetchedFriends.toString(2));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            LOG.info(ex.getMessage());
        } finally {
            LOG.info("Users profiles fetching is finished.");
        }
    }
}
