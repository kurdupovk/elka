package com.elka.storage;

import com.elka.api.ElkaApi;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
public class Expiditions {

    public static final int TOTAL_DEERS = 6;
    private final List<Expidition> expiditions = new CopyOnWriteArrayList<>();
    private final Semaphore semaphore = new Semaphore(TOTAL_DEERS);
    private final ExecutorService expeditionPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, r.toString());
        }
    });

    private class Expidition implements Runnable {

        private final static int NOT_STARTED = 1;
        private final static int RUNNING = 2;
        private final static int SHUTDOWN = 3;
        private final Logger logger = Logger.getLogger(Expidition.class.getName());
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private final String id;
        private final int requiredDeers;
        private Integer timeEnd;
        private String assignedId;
        private boolean repeatable;
        private int status;
        private Future future;

        public Expidition(String id, Integer timeEnd, String assignedId, boolean repeatable) {
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
                            startExpidition(elkaApi);
                        }
                        int now = (int) (System.currentTimeMillis() / 1000);
                        if (timeEnd > now) {
                            logger.info(getName() + " is going to sleep " + (timeEnd - now) / 60 + " mins");
                            Thread.sleep((timeEnd - now) * 1000L + 5000);
                        }
                        endExpidition(elkaApi);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (JSONException ex) {
                        logger.log(Level.WARNING, "Error with json parsing. Expiditions count - " + expiditions.size(), ex);
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

        private void startExpidition(ElkaApi elkaApi) throws IOException, JSONException {
            JSONObject started = elkaApi.startExpidition(id);
            if (!started.has("data")) {
                throw new JSONException(started.toString());
            }
            JSONObject data = started.getJSONObject("data");
            assignedId = data.getString("id");
            timeEnd = data.getInt("timeEnd");
            logger.info("Expidition '" + id + "' started with assigned id " + assignedId);
            status = RUNNING;
        }

        private void endExpidition(ElkaApi elkaApi) throws IOException, JSONException {
            JSONObject ended = elkaApi.endExpidition(assignedId);
            if (!ended.has("data")) {
                throw new JSONException(ended.toString());
            }
            logger.info("Expidition '" + assignedId + "' finished. Awards - " + ended.getJSONObject("data").getJSONObject("awards").toString());
            status = NOT_STARTED;
        }

        public void shutDown() {
            expiditions.remove(this);
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
            String name = "Expidition[" + id + "]";
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

    public Expiditions() {
    }

    public static int getCountDeersById(int expeditionId) {
        int countDeers = expeditionId % TOTAL_DEERS;
        return countDeers == 0 ? TOTAL_DEERS : countDeers;
    }

    public void shutDown() {
        for (Expidition expidition : expiditions) {
            expidition.shutDown();
        }
        expeditionPool.shutdownNow();
    }

    public void parseActiveExpiditions(JSONObject init) {
        if (init == null || !init.has("data")) {
            return;
        }
        JSONArray exps = init.optJSONObject("data").optJSONArray("expeditions");
        for (int i = 0; i < exps.length(); i++) {
            JSONObject exp = exps.optJSONObject(i);
            Expidition expidition = new Expidition(exp.optString("data_id"), exp.optInt("time_end"),
                    exp.optString("expedition_id"), false);
            expiditions.add(expidition);
            expidition.send();
        }
    }

    public List<String> getActive() {
        List<String> active = new ArrayList<>();
        for (Expidition expidition : expiditions) {
            if (expidition.isRunning()) {
                active.add(expidition.id);
            }
        }
        return active;
    }

    public List<String> getRepeatable() {
        List<String> releatable = new ArrayList<>();
        for (Expidition expidition : expiditions) {
            if (expidition.repeatable) {
                releatable.add(expidition.id);
            }
        }
        return releatable;
    }

    public boolean tryToSet(Collection<String> toSave) {
        int countOfDeers = 0;
        for (String id : toSave) {
            countOfDeers += getCountDeersById(Integer.parseInt(id));
        }
        if (countOfDeers > TOTAL_DEERS) {
            return false;
        }
        for (Expidition expedition : expiditions) {
            if (expedition.isRunning()) {
                expedition.repeatable = false;
            } else {
                expedition.shutDown();
            }
        }
        for (String id : toSave) {
            Expidition expedition = new Expidition(id, null, null, true);
            expiditions.add(expedition);
            expedition.send();
        }
        return true;
    }
}
