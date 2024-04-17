package tukano.clients;

import tukano.api.java.Blobs;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.grpc.GrpcBlobsClient;
import tukano.clients.grpc.GrpcShortsClient;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.clients.rest.RestUsersClient;
import tukano.clients.grpc.GrpcUsersClient;

import java.net.URI;

public class Clients {
    public static final ClientFactory<Users> USERS_CLIENT = new ClientFactory<Users>(Users.NAME,
            (uri) -> new RestUsersClient(URI.create(uri)),
            (uri) -> new GrpcUsersClient(URI.create(uri)));

    public static final ClientFactory<Shorts> SHORTS_CLIENT = new ClientFactory<Shorts>(Shorts.NAME,
            (uri) -> new RestShortsClient(URI.create(uri)),
            (uri) -> new GrpcShortsClient(URI.create(uri)));

    public static final ClientFactory<Blobs> BLOBS_CLIENT = new ClientFactory<Blobs>(Blobs.NAME,
            (u) -> new RestBlobsClient(URI.create(u)),
            (u) -> new GrpcBlobsClient(URI.create(u)));
}
