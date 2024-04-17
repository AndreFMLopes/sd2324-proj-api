package tukano.clients;

import tukano.Discovery;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Function;

public class ClientFactory<T> {
    public static final String REST = "/rest";
    public static final String GRPC = "/grpc";

    private final String serviceName;
    private final Function<String, T> createRestClient;
    private final Function<String, T> createGrpcClient;

    public ClientFactory(String serviceName, Function<String, T> createRestClient, Function<String, T> createGrpcClient) {
        this.serviceName = serviceName;
        this.createRestClient = createRestClient;
        this.createGrpcClient = createGrpcClient;
    }


    public T getClient() {
        String serverUri = this.getServerUri();
        if (serverUri.endsWith("rest")) {
            return createRestClient.apply(String.format("%s%s", getServerUri(), GRPC));
        } else if (serverUri.endsWith("grpc")) {
            return createGrpcClient.apply(String.format("%s%s", getServerUri(), GRPC));
        }
        return null;
    }

    private String getServerUri() {
        // Use Discovery to obtain the hostname and port of the server;
        String serverUrl = "";
        Discovery discovery = Discovery.getInstance();
        System.out.println(serviceName);
        URI[] uris = discovery.knownUrisOf(serviceName);
        System.out.println(Arrays.toString(uris));
        if (uris != null) {
            for (URI uri: uris) {
                if (uri != null) {
                    serverUrl = uri.toString();
                    break;
                }
            }
        }
        return serverUrl;
    }
}
