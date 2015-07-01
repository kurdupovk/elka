package com.elka.storage;

import java.util.HashMap;
import jersey.repackaged.com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Костя
 */
public class AppFriendsStorage {

    private static class FriendsStorageHolder {

        private static AppFriendsStorage INSTANCE = new AppFriendsStorage();
    }
    private JSONArray friends = new JSONArray();

    private AppFriendsStorage() {
    }

    private void addSanta(JSONArray friends) {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("userId", "santa");
        map.put("sUserId", "santa");
        map.put("photo", "");
        map.put("name", "Дед мороз");
        map.put("sign", "");
        JSONObject santa = new JSONObject(map);
        friends.put(santa);
    }

    public static AppFriendsStorage getInstance() {
        return FriendsStorageHolder.INSTANCE;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public void setFriends(JSONArray friends) {
        addSanta(friends);
        this.friends = friends;
    }

    public JSONObject getFriend(String userId) {
        for (int i = 0; i < friends.length(); i++) {
            JSONObject friend = friends.optJSONObject(i);
            if (friend.optString("userId").equals(userId)) {
                return friend;
            }
        }
        return null;
    }
}
