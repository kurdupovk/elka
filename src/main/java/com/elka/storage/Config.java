package com.elka.storage;

/**
 *
 * @author kkurdupov
 */
public class Config {

    boolean openChests = true;
    boolean fetchFrieds = true;
    int screenId = 0;

    public Config() {
    }

    public boolean isOpenChests() {
        return openChests;
    }

    public void setOpenChests(boolean openChests) {
        this.openChests = openChests;
    }

    public boolean isFetchFrieds() {
        return fetchFrieds;
    }

    public void setFetchFrieds(boolean fetchFrieds) {
        this.fetchFrieds = fetchFrieds;
    }

    public int getScreenId() {
        return screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }
}
