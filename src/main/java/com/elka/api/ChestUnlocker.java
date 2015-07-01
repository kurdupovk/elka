package com.elka.api;

import com.elka.storage.CredentialsStorage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class ChestUnlocker extends Thread {

    private static final Logger LOG = Logger.getLogger(ChestUnlocker.class.getName());
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final long NEXT_CHEST_TIME = 1000 * 3600 * 6;
    private ChestUnlockerCollection chestUnlockerCollection;
    private CredentialsStorage credentialsStorage;
    private JSONObject user;
    private String userId;
    private String sUserId;
    private int chestTime;
    private long nextChestTime;

    public ChestUnlocker(ChestUnlockerCollection chestUnlockerCollection, CredentialsStorage credentialsStorage,
            int chestTime, JSONObject user) {
        this.chestUnlockerCollection = chestUnlockerCollection;
        this.credentialsStorage = credentialsStorage;
        this.chestTime = chestTime;
        this.userId = user.optString("userId");
        this.sUserId = user.optString("sUserId");
        this.user = user;
        this.nextChestTime = chestTime * 1000L + NEXT_CHEST_TIME;
        this.setName("Unlocker_" + user.optString("name") + "[" + sdf.format(new Date(nextChestTime)) + "]");
    }

    @Override
    public void run() {
        try {
            long timeToSleep = nextChestTime - System.currentTimeMillis();
            log("is going to sleep " + timeToSleep + " milliseconds");
            Thread.sleep(timeToSleep);
            ElkaApi elkaApi = new ElkaApi(credentialsStorage.get());
            for (int i = 0; i < 10; i++) {
                JSONObject openChest = elkaApi.openChest(userId, sUserId, user.optString("sign"));
                JSONObject data = openChest.getJSONObject("data");
                boolean isOpenedByMe = !data.has("chest") && data.optInt("success") == 1;
                boolean isOpenedByOther = data.has("chest") && data.getJSONObject("chest").getInt("time") != chestTime;
                if (isOpenedByMe || isOpenedByOther) {
                    if (isOpenedByMe) {
                        log("opened chest by me. Awards - " + data.getJSONObject("awards").toString());
                    } else if (isOpenedByOther) {
                        log("opened chest by user with id " + data.getJSONObject("chest").getString("user"));
                    }
                    log("needed " + (i + 1) + " iterations.");
                    break;
                }
                Thread.sleep(150);
            }
        } catch (InterruptedException ex) {
            log("has been interrupted");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            chestUnlockerCollection.remove(this);
            log("has been released.");
        }
    }

    private void log(String msg) {
        LOG.info(this.getName() + " " + msg);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.userId);
        hash = 97 * hash + Objects.hashCode(this.sUserId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChestUnlocker other = (ChestUnlocker) obj;
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        if (!Objects.equals(this.sUserId, other.sUserId)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(new Date(1435725332 * 1000l));
    }
}
