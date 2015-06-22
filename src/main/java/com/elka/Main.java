package com.elka;

import com.elka.storage.CredentialsStorage;
import com.elka.api.UsersChestsFetcher;
import com.elka.api.FriendsAppFetcher;
import com.elka.storage.FriendsStorage;
import com.elka.storage.UserChestsStorage;
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
                new UsersChestsFetcher(CredentialsStorage.getInstance(), FriendsStorage.getInstance()).fetchTo(UserChestsStorage.getInstance());
            }
        }, 5000, 60000, TimeUnit.MILLISECONDS);
        return executor;
    }

    public static void main(String[] args) throws IOException, JSONException {
        final HttpServer server = startServer();
        CredentialsStorage.getInstance().loadFromFile();
        new FriendsAppFetcher(CredentialsStorage.getInstance()).fetchTo(FriendsStorage.getInstance());
        ScheduledExecutorService chestFetchedExecutor = startChestFetcher();
        System.in.read();
        chestFetchedExecutor.shutdownNow();
        server.stop();
    }
}
