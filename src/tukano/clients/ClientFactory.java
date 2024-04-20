package tukano.clients;

import java.net.URI;

import tukano.Discovery;
import tukano.api.java.*;
import tukano.api.java.Result.ErrorCode;
import tukano.clients.grpc.GrpcBlobsClient;
import tukano.clients.grpc.GrpcShortsClient;
import tukano.clients.grpc.GrpcUsersClient;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.clients.rest.RestUsersClient;

public class ClientFactory {

    public static Result<Users> getUsersClient() {
        URI[] usersServers = Discovery.getInstance().knownUrisOf("users");

        if (usersServers == null) return Result.error(ErrorCode.NOT_FOUND);

        if (usersServers[0].toString().endsWith("rest")) return Result.ok(new RestUsersClient(usersServers[0]));

        return Result.ok(new GrpcUsersClient(usersServers[0]));
    }

    public static Result<Shorts> getShortsClient() {
        URI[] shortsServers = Discovery.getInstance().knownUrisOf("shorts");

        if (shortsServers == null) return Result.error(ErrorCode.NOT_FOUND);

        if (shortsServers[0].toString().endsWith("rest")) return Result.ok(new RestShortsClient(shortsServers[0]));

        return Result.ok(new GrpcShortsClient(shortsServers[0]));
    }

    public static Result<Blobs> getBlobsClient() {
        URI[] blobsServers = Discovery.getInstance().knownUrisOf("blobs");

        if (blobsServers == null) return Result.error(ErrorCode.NOT_FOUND);

        if (blobsServers[0].toString().endsWith("rest")) return Result.ok(new RestBlobsClient(blobsServers[0]));

        return Result.ok(new GrpcBlobsClient(blobsServers[0]));
    }

    public static Result<Blobs> getBlobsClient(String serverUrl) {
        URI serverURI = URI.create(serverUrl);
        System.out.println(serverURI);
        if (serverUrl.contains("rest")) return Result.ok(new RestBlobsClient(serverURI));
        return Result.ok(new GrpcBlobsClient(serverURI));
    }

}
