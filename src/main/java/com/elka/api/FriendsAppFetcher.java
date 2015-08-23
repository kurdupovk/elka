package com.elka.api;

import com.elka.storage.ApplicationStorage;
import com.elka.storage.CredentialsStorage;
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

    public void fetchTo(ApplicationStorage applicationStorage) {
        if (credentialsStorage.isEmpty()) {
            return;
        }
        if (credentialsStorage.get().isInvalid()) {
            LOG.log(Level.WARNING, "Current credentials are invalid. No fetch user profiles process.");
            return;
        }
        LOG.info("Starting fetching users profiles.");
        try {
            List<String> friendIds = new ArrayList<>();
            VKApi vkApi = new VKApi(credentialsStorage.get());
            ElkaApi elkaApi = new ElkaApi(credentialsStorage.get());
            if (!applicationStorage.getConfig().isFetchFrieds()) {
                JSONObject result = elkaApi.init(friendIds);
                applicationStorage.getExpeditions().parseActiveExpeditions(result);
                return;
            }
            friendIds.addAll(getAppFriends(vkApi));
            JSONObject result = elkaApi.init(friendIds);
            applicationStorage.getExpeditions().parseActiveExpeditions(result);
            JSONArray friendsArray = result.getJSONObject("data").getJSONArray("friends");
            result = vkApi.getAreFriends(friendIds);
            JSONArray areFriends = result.getJSONArray("response");
            result = vkApi.getUsers(friendIds);
            JSONArray users = result.getJSONArray("response");
            boolean found = false;
            for (String friendId : friendIds) {
                JSONObject user = new JSONObject();
                user.put("screenId", 0);
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
                applicationStorage.getFriends().put(user.getString("userId"), user);
            }
            applicationStorage.getFriends().put(ApplicationStorage.SANTA.getString("userId"), ApplicationStorage.SANTA);
            LOG.info("Fetched friends:");
            LOG.info(new JSONArray(applicationStorage.getFriends().values()).toString(2));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            LOG.info(ex.getMessage());
        } finally {
            LOG.info("Users profiles fetching is finished.");
        }
    }

    private List<String> getAppFriends(VKApi vkApi) throws IOException, JSONException {
        List<String> friends = new ArrayList<>();
        JSONObject result = vkApi.getAppFriends();
        JSONArray ids = result.getJSONArray("response");
        for (int i = 0; i < ids.length(); i++) {
            friends.add(ids.getString(i));
        }
        return friends;
    }
}
