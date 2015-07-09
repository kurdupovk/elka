package com.elka.storage;

import com.elka.api.ElkaApi;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
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
    ReentrantLock lock = new ReentrantLock();

    private class Expidition implements Runnable {

        private final Logger logger = Logger.getLogger(Expidition.class.getName());
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private final String id;
        private Integer timeEnd;
        private String assignedId;
        private boolean repeatable;
        private Thread worker;

        public Expidition(String id, Integer timeEnd, String assignedId, boolean repeatable) {
            this.id = id;
            this.timeEnd = timeEnd;
            this.assignedId = assignedId;
            this.repeatable = repeatable;
            this.worker = new Thread(this);
            setName(worker);
        }

        @Override
        public void run() {
            try {
                if (CredentialsStorage.getInstance().isEmpty() || CredentialsStorage.getInstance().get().isInvalid()) {
                    expiditions.remove(Expidition.this);
                    logger.warning("No expedition ran.");
                }
                ElkaApi elkaApi = new ElkaApi(CredentialsStorage.getInstance().get());
                if (!isActive()) {
                    startExpidition(elkaApi);
                }
                setName(worker);
                int now = (int) (System.currentTimeMillis() / 1000);
                if (timeEnd > now) {
                    logger.info(worker.getName() + " is going to sleep " + (timeEnd - now) / 60 + " mins");
                    Thread.sleep((timeEnd - now) * 1000L + 5000);
                }
                endExpidition(elkaApi);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                logger.log(Level.WARNING, "Error with json parsing. Expiditions count - " + expiditions.size(), ex);
                expiditions.remove(Expidition.this);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "Thread has been interrupted", ex);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Unhandled exception", ex);
            } finally {
                try {
                    if (repeatable) {
                        reset();
                    } else {
                        expiditions.remove(Expidition.this);
                    }
                } finally {
                    runExpiditions();
                }
            }
        }

        public void send() {
            this.worker.start();
        }

        public boolean isActive() {
            return timeEnd != null && assignedId != null;
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
        }

        private void endExpidition(ElkaApi elkaApi) throws IOException, JSONException {
            JSONObject ended = elkaApi.endExpidition(assignedId);
            if (!ended.has("data")) {
                throw new JSONException(ended.toString());
            }
            logger.info("Expidition '" + assignedId + "' finished. Awards - " + ended.getJSONObject("data").getJSONObject("awards").toString());
        }

        private void reset() {
            assignedId = null;
            timeEnd = null;
            worker = new Thread(this);
            setName(worker);
        }

        private void setName(Thread t) {
            String name = "Expidition[" + id + "]";
            if (assignedId != null) {
                name += ("-id[" + assignedId + "]");
            }
            if (timeEnd != null) {
                name += ("-end[" + sdf.format(new Date(timeEnd * 1000L)) + "]");
            }
            t.setName(name);
        }
    }

    public Expiditions() {
    }

    public static int getCountDeersById(int expiditionId) {
        int countDeers = expiditionId % TOTAL_DEERS;
        return countDeers == 0 ? TOTAL_DEERS : countDeers;
    }

    public void parseActiveExpiditions(JSONObject init) {
        if (init == null || !init.has("data")) {
            return;
        }
        JSONArray exps = init.optJSONObject("data").optJSONArray("expeditions");
        for (int i = 0; i < exps.length(); i++) {
            JSONObject exp = exps.optJSONObject(i);
            Expidition expidition = new Expidition(exp.optString("data_id"), exp.optInt("time_end"), exp.optString("expedition_id"), false);
            expiditions.add(expidition);
            expidition.send();
        }
    }

    public List<String> getActive() {
        List<String> active = new ArrayList<>();
        for (Expidition expidition : expiditions) {
            if (expidition.isActive()) {
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
        List<Expidition> toDelete = new ArrayList<>();
        for (Expidition expidition : expiditions) {
            if (expidition.isActive()) {
                expidition.repeatable = false;
            } else {
                toDelete.add(expidition);
            }
        }
        expiditions.removeAll(toDelete);
        for (String id : toSave) {
            expiditions.add(new Expidition(id, null, null, true));
        }
        runExpiditions();
        return true;
    }

    private void runExpiditions() {
        lock.lock();
        try {
            List<String> active = getActive();
            int busyDeers = 0;
            for (String id : active) {
                busyDeers += getCountDeersById(Integer.parseInt(id));
            }
            int freeDeers = TOTAL_DEERS - busyDeers;
            if (freeDeers <= 0) {
                return;
            }
            List<Expidition> acceptable = findAcceptableExpiditions(freeDeers);
            for (Expidition expidition : acceptable) {
                expidition.send();
            }
        } finally {
            lock.unlock();
        }
    }

    private List<Expidition> findAcceptableExpiditions(int freeDeers) {
        List<Expidition> accetable = new ArrayList<>();
        for (Expidition expidition : expiditions) {
            if (expidition.isActive()) {
                continue;
            }
            int countDeerForExpidition = getCountDeersById(Integer.parseInt(expidition.id));
            if (countDeerForExpidition <= freeDeers) {
                accetable.add(expidition);
                freeDeers -= countDeerForExpidition;
            }
        }
        return accetable;
    }
}
