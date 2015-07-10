package com.elka.api;

import com.elka.storage.ApplicationStorage;
import com.elka.storage.CredentialsStorage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class ChestUnlockerCollection {

    private static final Logger LOG = Logger.getLogger(ChestUnlockerCollection.class.getName());
    public static final int THREE_DAYS = 3600 * 24 * 3;

    private static class ChestUnlockerCollectionHolder {

        private static ChestUnlockerCollection INSTANCE = new ChestUnlockerCollection();
    }
    private Set<ChestUnlocker> chestsUnlockers = new HashSet<>();

    private ChestUnlockerCollection() {
    }

    public static ChestUnlockerCollection getInstance() {
        return ChestUnlockerCollectionHolder.INSTANCE;
    }

    private static boolean isChestValid(JSONObject chest) {
        int loginTime = chest.optInt("logintime");
        long now = System.currentTimeMillis() / 1000;
        if (now - loginTime > THREE_DAYS) {
            return false;
        }
        return true;
    }

    public void startWith(CredentialsStorage credentialsStorage, ApplicationStorage applicationStorage) {
        if (!applicationStorage.getConfig().isOpenChests()) {
            return;
        }
        if (credentialsStorage.isEmpty()) {
            return;
        }
        if (credentialsStorage.get().isInvalid()) {
            LOG.log(Level.WARNING, "Current credentials are invalid. No start open chest process");
            return;
        }
        for (Map.Entry<String, JSONObject> entry : applicationStorage.getUserChests().entrySet()) {
            JSONObject chest = entry.getValue();
            if (isChestValid(chest)) {
                add(new ChestUnlocker(this, credentialsStorage, chest.optInt("time"), applicationStorage.getFriends().get(entry.getKey())));
            }
        }
    }

    public void add(ChestUnlocker chestUnlocker) {
        if (chestsUnlockers.add(chestUnlocker)) {
            chestUnlocker.start();
        }
    }

    public void remove(ChestUnlocker chestUnlocker) {
        chestsUnlockers.remove(chestUnlocker);
    }

    public void shutDown() {
        for (ChestUnlocker chestUnlocker : chestsUnlockers) {
            chestUnlocker.interrupt();
        }
    }
}
