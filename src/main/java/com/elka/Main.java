package com.elka;

import com.elka.api.ChestUnlockerCollection;
import com.elka.storage.CredentialsStorage;
import com.elka.api.UsersChestsFetcher;
import com.elka.api.FriendsAppFetcher;
import com.elka.api.FriendsFriendsFetcher;
import com.elka.storage.ApplicationStorage;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.json.JSONException;

/**
 * Main class.
 *
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://localhost:8080/elka/";

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
                new UsersChestsFetcher(CredentialsStorage.getInstance()).fetchTo(ApplicationStorage.getInstance());
                ChestUnlockerCollection.getInstance().startWith(CredentialsStorage.getInstance(), ApplicationStorage.getInstance());
            }
        }, 5000, 60000, TimeUnit.MILLISECONDS);
        return executor;
    }

    private static void parseArgs(String[] args) {
        if (args.length <= 0) {
            return;
        }
        for (String arg : args) {
            if (arg.contains("no-chests")) {
                ApplicationStorage.getInstance().getConfig().setOpenChests(false);
            }
            if(arg.contains("no-friends")){
                ApplicationStorage.getInstance().getConfig().setFetchFrieds(false);
            }
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        parseArgs(args);
        final HttpServer server = startServer();
        CredentialsStorage.getInstance().loadFromFile();
        new FriendsAppFetcher(CredentialsStorage.getInstance()).fetchTo(ApplicationStorage.getInstance());
        new FriendsFriendsFetcher(CredentialsStorage.getInstance()).fetchTo(ApplicationStorage.getInstance());
        ScheduledExecutorService chestFetchedExecutor = startChestFetcher();
        System.in.read();
        chestFetchedExecutor.shutdownNow();
        ChestUnlockerCollection.getInstance().shutDown();
        server.stop();
    }
}
