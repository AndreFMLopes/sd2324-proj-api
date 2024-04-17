package tukano.clients;

import java.net.URI;

import tukano.Discovery;
import tukano.api.java.*;
import tukano.api.java.Result.ErrorCode;
import tukano.clients.rest.*;
import tukano.impl.grpc.clients.*;

public class ClientFactory {
	
	public static Result<Users> getUsersClient() throws InterruptedException {
		URI[] usersServers = Discovery.getInstance().knownUrisOf("users", 1);
		if(usersServers.length == 0)return Result.error(ErrorCode.NOT_FOUND);
		if(usersServers[0].toString().endsWith("rest")) return Result.ok(new RestUsersClient(usersServers[0]));
		return Result.ok(new GrpcUsersClient(usersServers[0]));
	}
	
	public static Result<Shorts> getShortsClient() throws InterruptedException {
		URI[] shortsServers = Discovery.getInstance().knownUrisOf("shorts", 1);
		if(shortsServers.length == 0)return Result.error(ErrorCode.NOT_FOUND);
		if(shortsServers[0].toString().endsWith("rest")) return Result.ok(new RestShortsClient(shortsServers[0]));
		return Result.ok(new GrpcShortsClient(shortsServers[0]));
	}
	
	public static Result<Blobs> getBlobsClient() throws InterruptedException {
		URI[] blobsServers = Discovery.getInstance().knownUrisOf("blobs", 1);
		if(blobsServers.length == 0)return Result.error(ErrorCode.NOT_FOUND);
		if(blobsServers[0].toString().endsWith("rest")) return Result.ok(new RestBlobsClient(blobsServers[0]));
		return Result.ok(new GrpcBlobsClient(blobsServers[0]));
	}

}
