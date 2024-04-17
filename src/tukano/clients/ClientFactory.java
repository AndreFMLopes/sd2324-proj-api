package tukano.clients;

import java.net.URI;

import tukano.Discovery;
import tukano.api.java.*;
import tukano.api.java.Result.ErrorCode;
import tukano.clients.rest.*;
import tukano.clients.grpc.*;

public class ClientFactory {

    public static Result<Users> getUsersClient() {
        URI[] usersServers = Discovery.getInstance().knownUrisOf("users");

        if (usersServers.length == 0) return Result.error(ErrorCode.NOT_FOUND);

        if (usersServers[0].toString().endsWith("rest")) return Result.ok(new RestUsersClient(usersServers[0]));

        return Result.ok(new GrpcUsersClient(usersServers[0]));
    }

    public static Result<Shorts> getShortsClient() {
        URI[] shortsServers = Discovery.getInstance().knownUrisOf("shorts");

        if (shortsServers.length == 0) return Result.error(ErrorCode.NOT_FOUND);

        if (shortsServers[0].toString().endsWith("rest")) return Result.ok(new RestShortsClient(shortsServers[0]));

        return Result.ok(new GrpcShortsClient(shortsServers[0]));
    }

    public static Result<Blobs> getBlobsClient() {
        URI[] blobsServers = Discovery.getInstance().knownUrisOf("blobs");

        if (blobsServers.length == 0) return Result.error(ErrorCode.NOT_FOUND);

        if (blobsServers[0].toString().endsWith("rest")) return Result.ok(new RestBlobsClient(blobsServers[0]));

        return Result.ok(new GrpcBlobsClient(blobsServers[0]));
    }

}
