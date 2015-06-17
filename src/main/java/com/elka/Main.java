package com.elka;

import com.elka.api.ElkaApi;
import com.elka.storage.CredentialsStorage;
import com.elka.api.Fetcher;
import com.elka.api.VKApi;
import com.elka.storage.FriendsStorage;
import com.elka.storage.UserChestsStorage;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main class.
 *
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/elka/";
    
    private static final CountDownLatch CDL = new CountDownLatch(1);

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.elka");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        CLStaticHttpHandler cls = new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "web/");
        server.getServerConfiguration().addHttpHandler(cls, "/");
        return server;
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS] [%4$s]"
                + " - %5$s%6$s%n");
        LOG.setLevel(Level.ALL);
    }
    
    

    public static ScheduledExecutorService startChestFetcher() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    CDL.await();
                } catch (InterruptedException ex) {
                   LOG.log(Level.SEVERE, null, ex);
                }
                if (CredentialsStorage.getInstance().isEmpty()) {
                    return;
                }
                new Fetcher(CredentialsStorage.getInstance().get()).fetchUsersTo(UserChestsStorage.getInstance());
            }
        }, 5000, 60000, TimeUnit.MILLISECONDS);
        return executor;
    }

    public static void main(String[] args) throws IOException, JSONException {
        CredentialsStorage.getInstance().loadFromFile();
        ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
        executor2.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (CredentialsStorage.getInstance().isEmpty()) {
                    return;
                }
                if (CredentialsStorage.getInstance().get().isInvalid()) {
                    LOG.log(Level.INFO, "Current credentials are invalid. No fetch user profiles process.");
                    return;
                }
                JSONArray fetchedFriends = new JSONArray();
                LOG.info("Starting fetching users profiles.");
                try {
                    VKApi vkApi = new VKApi(CredentialsStorage.getInstance().get());
                    ElkaApi elkaApi = new ElkaApi(CredentialsStorage.getInstance().get());
                    JSONObject result = vkApi.getAppFriends();
                    JSONArray ids = result.getJSONArray("response");
                    List<String> friendIds = new ArrayList<>();
                    for (int i = 0; i < ids.length(); i++) {
                        friendIds.add(ids.getString(i));
                    }
                    result = elkaApi.init(friendIds);
                    JSONArray friendsArray = result.getJSONObject("data").getJSONArray("friends");
                    result = vkApi.getAreFriends(friendIds);
                    JSONArray areFriends = result.getJSONArray("response");
                    result = vkApi.getUsers(friendIds);
                    JSONArray users = result.getJSONArray("response");
                    boolean found = false;
                    for (String friendId : friendIds) {
                        JSONObject user = new JSONObject();
                        for (int i = 0; i < friendsArray.length(); i++) {
                            JSONObject friend = friendsArray.getJSONObject(i);
                            if (friend.getString("sUserId").equals(friendId)) {
                                user.put("userId", friend.getString("userId"));
                                user.put("sUserId", friend.getString("sUserId"));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            LOG.warning("Can not find user with id '" + friendId + "' in friends");
                            continue;
                        }
                        found = false;
                        for (int i = 0; i < areFriends.length(); i++) {
                            JSONObject areFriend = areFriends.getJSONObject(i);
                            if (areFriend.getString("user_id").equals(friendId)) {
                                user.put("sign", areFriend.getString("sign"));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            LOG.warning("Can not find user with id '" + friendId + "' in areFriends");
                            continue;
                        }
                        found = false;
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject userJson = users.getJSONObject(i);
                            if (userJson.getString("uid").equals(friendId)) {
                                user.put("name", userJson.getString("first_name") + " " + userJson.getString("last_name"));
                                user.put("photo", userJson.getString("photo"));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            LOG.warning("Can not find user with id '" + friendId + "' in users");
                            continue;
                        }
                        fetchedFriends.put(user);
                    }
                    FriendsStorage.getInstance().setFriends(fetchedFriends);
                    CDL.countDown();
                    LOG.info("Friends:");
                    LOG.info(fetchedFriends.toString(2));
                } catch (IOException | JSONException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    LOG.info("Users profiles fetching is finished.");
                }
            }
        }, 5000, 600000, TimeUnit.MILLISECONDS);
        ScheduledExecutorService chestFetchedExecutor = startChestFetcher();
        final HttpServer server = startServer();
        System.in.read();
        chestFetchedExecutor.shutdownNow();
        executor2.shutdownNow();
        server.stop();
    }
}
