package tukano.servers.grpc;

import io.grpc.ServerBuilder;
import tukano.Discovery;
import tukano.api.java.Shorts;

import java.net.InetAddress;
import java.util.logging.Logger;

public class GrpcShortsServer {

    public static final String SERVICE = "shorts";

    public static final int PORT = 9001;

    private static final String GRPC_CTX = "/grpc";

    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(GrpcShortsServer.class.getName());

    public static void main (String[] args) throws Exception {
        var stub = new GrpcShortsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();
        var serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        Log.info(String.format("%s gRPC Server Ready @ %s\n", Shorts.NAME, serverURI));

        // Use Discovery to announce the uri of this server
        Discovery discovery = Discovery.getInstance();
        discovery.announce(SERVICE, serverURI);

        server.start().awaitTermination();
    }
}
