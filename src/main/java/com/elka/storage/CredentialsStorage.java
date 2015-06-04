package com.elka.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kkurdupov
 */
public class CredentialsStorage {

    private static class CredentialsStorageHolder {

        private static final CredentialsStorage INSTANCE = new CredentialsStorage();
    }
    private Map<String, Credentials> storage = new HashMap<>();

    private CredentialsStorage() {
    }

    public static CredentialsStorage getInstance() {
        return CredentialsStorageHolder.INSTANCE;
    }

    public Credentials get(String key) {
        return storage.get(key);
    }

    public void add(Credentials credentials) {
        storage.put(credentials.getSuid(), credentials);
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public Collection<Credentials> credentials() {
        return storage.values();
    }

    public Credentials getFrist() {
        if (storage.isEmpty()) {
            return null;
        }
        return storage.values().iterator().next();
    }
}
