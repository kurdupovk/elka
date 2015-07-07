package com.elka.storage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class Expiditions {

    private final List<JSONObject> active = new CopyOnWriteArrayList<>();
    private final List<String> saved = new CopyOnWriteArrayList<>();

    public Expiditions() {
    }

    public void parseActiveExpiditions(JSONObject init) {
        if (init == null || !init.has("data")) {
            return;
        }
        JSONArray exps = init.optJSONObject("data").optJSONArray("expeditions");
        for (int i = 0; i < exps.length(); i++) {
            active.add(exps.optJSONObject(i));
        }
    }

    public List<JSONObject> getActive() {
        return active;
    }

    public List<String> getSaved() {
        return saved;
    }
}
