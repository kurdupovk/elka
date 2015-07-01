package com.elka.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class UserChestsStorage {

    private static class UserChestsStorageHolder {

        private static UserChestsStorage INSTANCE = new UserChestsStorage();
    }
    private Map<String, JSONObject> userChests = new ConcurrentHashMap<>();

    private UserChestsStorage() {
    }

    public void put(String userId, JSONObject json) {
        userChests.put(userId, json);
    }

    public JSONObject remove(String userId) {
        return userChests.remove(userId);
    }

    public Collection<JSONObject> values() {
        return userChests.values();
    }

    public Map<String, JSONObject> asMap() {
        return userChests;
    }

    public static UserChestsStorage getInstance() {
        return UserChestsStorageHolder.INSTANCE;
    }
}
