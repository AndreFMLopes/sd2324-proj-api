package tukano.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tukano.Discovery;

public class RestShortsServer {

    private static Logger Log = Logger.getLogger(RestShortsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8001;
    public static final String SERVICE = "ShortsService";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            ResourceConfig config = new ResourceConfig();
            config.register(  RestShortsResource.class );

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s REST Server ready @ %s\n", SERVICE, serverURI));

            // Use Discovery to announce the uri of this server
            Discovery discovery = Discovery.getInstance();
            discovery.announce(SERVICE, serverURI);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
