package com.elka.storage;

import java.util.HashMap;
import jersey.repackaged.com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Костя
 */
public class FriendsStorage {

    private static class FriendsStorageHolder {

        private static FriendsStorage INSTANCE = new FriendsStorage();
    }
    private JSONArray friends = new JSONArray();

    private FriendsStorage() {
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

    public static FriendsStorage getInstance() {
        return FriendsStorageHolder.INSTANCE;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public void setFriends(JSONArray friends) {
        addSanta(friends);
        this.friends = friends;
    }
}
