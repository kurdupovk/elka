package com.elka.storage;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class FriendsFriendsStorage {

    private static class FriendsFriendsStorageHolder {

        private static FriendsFriendsStorage INSTANCE = new FriendsFriendsStorage();
    }
    private Map<String, JSONObject> friends = new HashMap<>();

    private FriendsFriendsStorage() {
    }

    public static FriendsFriendsStorage getInstance() {
        return FriendsFriendsStorageHolder.INSTANCE;
    }

    public void put(String friendFriendId, JSONObject data) {
        friends.put(friendFriendId, data);
    }

    public JSONObject get(String friendFriendId) {
        return friends.get(friendFriendId);
    }

    public int size() {
        return friends.size();
    }
}
