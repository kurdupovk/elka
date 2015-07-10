package com.elka.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class ApplicationStorage {

    private static class ApplicationStorageHolder {

        private static ApplicationStorage INSTANCE = new ApplicationStorage();
    }
    public static final JSONObject SANTA = new JSONObject(new HashMap() {
        {
            put("userId", "santa");
            put("sUserId", "santa");
            put("photo", "");
            put("name", "Дед мороз");
            put("sign", "");
        }
    });
    private Map<String, JSONObject> userChests = new ConcurrentHashMap<>();
    private Map<String, JSONObject> friends = new ConcurrentHashMap<>();
    private Map<String, JSONObject> friendsOfFriends = new ConcurrentHashMap<>();
    private Config config = new Config();
    private Expiditions expiditions = new Expiditions();

    private ApplicationStorage() {
    }

    public static ApplicationStorage getInstance() {
        return ApplicationStorageHolder.INSTANCE;
    }

    public Map<String, JSONObject> getUserChests() {
        return userChests;
    }

    public Map<String, JSONObject> getFriends() {
        return friends;
    }

    public Map<String, JSONObject> getFriendsOfFriends() {
        return friendsOfFriends;
    }

    public Expiditions getExpiditions() {
        return expiditions;
    }

    public Config getConfig() {
        return config;
    }
}