package com.elka.storage;

import com.elka.api.ElkaApi;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class Expeditions {

    public static final int TOTAL_DEERS = 6;
    private final List<Expedition> expeditions = new CopyOnWriteArrayList<>();
    private final Semaphore semaphore = new Semaphore(TOTAL_DEERS);
    private final ExecutorService expeditionPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, r.toString());
        }
    });

    private class Expedition implements Runnable {

        private final static int NOT_STARTED = 1;
        private final static int RUNNING = 2;
        private final static int SHUTDOWN = 3;
        private final Logger logger = Logger.getLogger(Expedition.class.getName());
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private final String id;
        private final int requiredDeers;
        private Integer timeEnd;
        private String assignedId;
        private boolean repeatable;
        private int status;
        private Future future;

        public Expedition(String id, Integer timeEnd, String assignedId, boolean repeatable) {
            this.id = id;
            this.requiredDeers = getCountDeersById(Integer.parseInt(id));
            this.timeEnd = timeEnd;
            this.assignedId = assignedId;
            this.repeatable = repeatable;
            this.status = assignedId != null && timeEnd != null ? RUNNING : NOT_STARTED;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire(requiredDeers);
                do {
                    try {
                        if (CredentialsStorage.getInstance().isEmpty() || CredentialsStorage.getInstance().get().isInvalid()) {
                            shutDown();
                            logger.warning("No expedition ran.");
                            break;
                        }
                        ElkaApi elkaApi = new ElkaApi(CredentialsStorage.getInstance().get());
                        if (!isRunning()) {
                            startExpedition(elkaApi);
                        }
                        int now = (int) (System.currentTimeMillis() / 1000);
                        if (timeEnd > now) {
                            logger.info(getName() + " is going to sleep " + (timeEnd - now) / 60 + " mins");
                            Thread.sleep((timeEnd - now) * 1000L + 5000);
                        }
                        endExpedition(elkaApi);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (JSONException ex) {
                        logger.log(Level.WARNING, "Error with json parsing. Expeditions count - " + expeditions.size(), ex);
                    }
                } while (repeatable);
                semaphore.release(requiredDeers);
            } catch (InterruptedException ex) {
                if (status == SHUTDOWN) {
                    logger.info(getName() + " shutting down.");
                } else {
                    logger.log(Level.SEVERE, "Thread was interrupted", ex);
                }
            } finally {
                shutDown();
            }
        }

        private void startExpedition(ElkaApi elkaApi) throws IOException, JSONException {
            JSONObject started = elkaApi.startExpedition(id);
            if (!started.has("data")) {
                throw new JSONException(started.toString());
            }
            JSONObject data = started.getJSONObject("data");
            assignedId = data.getString("id");
            timeEnd = data.getInt("timeEnd");
            logger.info("Expedition '" + id + "' started with assigned id " + assignedId);
            status = RUNNING;
        }

        private void endExpedition(ElkaApi elkaApi) throws IOException, JSONException {
            JSONObject ended = elkaApi.endExpedition(assignedId);
            if (!ended.has("data")) {
                throw new JSONException(ended.toString());
            }
            logger.info("Expedition '" + assignedId + "' finished. Awards - " + ended.getJSONObject("data").getJSONObject("awards").toString());
            status = NOT_STARTED;
        }

        public void shutDown() {
            expeditions.remove(this);
            status = SHUTDOWN;
            if (this.future != null) {
                future.cancel(true);
            }
        }

        public void send() {
            this.future = expeditionPool.submit(this);
        }

        public boolean isRunning() {
            return status == RUNNING;
        }

        private String getName() {
            String name = "Expedition[" + id + "]";
            if (assignedId != null) {
                name += ("-id[" + assignedId + "]");
            }
            if (timeEnd != null) {
                name += ("-end[" + sdf.format(new Date(timeEnd * 1000L)) + "]");
            }
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public Expeditions() {
    }

    public static int getCountDeersById(int expeditionId) {
        int countDeers = expeditionId % TOTAL_DEERS;
        return countDeers == 0 ? TOTAL_DEERS : countDeers;
    }

    public void shutDown() {
        for (Expedition expedition : expeditions) {
            expedition.shutDown();
        }
        expeditionPool.shutdownNow();
    }

    public void parseActiveExpeditions(JSONObject init) {
        if (init == null || !init.has("data")) {
            return;
        }
        JSONArray exps = init.optJSONObject("data").optJSONArray("expeditions");
        for (int i = 0; i < exps.length(); i++) {
            JSONObject exp = exps.optJSONObject(i);
            Expedition expedition = new Expedition(exp.optString("data_id"), exp.optInt("time_end"),
                    exp.optString("expedition_id"), false);
            expeditions.add(expedition);
            expedition.send();
        }
    }

    public List<String> getActive() {
        List<String> active = new ArrayList<>();
        for (Expedition expedition : expeditions) {
            if (expedition.isRunning()) {
                active.add(expedition.id);
            }
        }
        return active;
    }

    public List<String> getRepeatable() {
        List<String> releatable = new ArrayList<>();
        for (Expedition expedition : expeditions) {
            if (expedition.repeatable) {
                releatable.add(expedition.id);
            }
        }
        return releatable;
    }

    public boolean tryToRemove(String id) {
        for (Expedition expedition : expeditions) {
            if (expedition.isRunning()) {
                continue;
            }
            if (expedition.id.equals(id)) {
                expedition.shutDown();
                break;
            }
        }
        return true;
    }

    public boolean tryToAdd(String id) {
        int currentSavedCountDeers = 0;
        for (Expedition expedition : expeditions) {
            if (!expedition.isRunning()) {
                currentSavedCountDeers += expedition.requiredDeers;
            }
        }
        if (getCountDeersById(Integer.parseInt(id)) + currentSavedCountDeers > TOTAL_DEERS) {
            return false;
        }
        for (Expedition expedition : expeditions) {
            if (expedition.isRunning()) {
                expedition.repeatable = false;
            }
        }
        Expedition expedition = new Expedition(id, null, null, true);
        expeditions.add(expedition);
        expedition.send();
        return true;
    }
}
