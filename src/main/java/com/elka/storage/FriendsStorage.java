package com.elka.storage;

import org.json.JSONArray;

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

    public static FriendsStorage getInstance() {
        return FriendsStorageHolder.INSTANCE;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public void setFriends(JSONArray friends) {
        this.friends = friends;
    }
}
