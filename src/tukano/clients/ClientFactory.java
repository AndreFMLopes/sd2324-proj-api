package tukano.clients;

import java.net.URI;

import tukano.Discovery;
import tukano.api.java.*;
import tukano.clients.rest.*;
import tukano.impl.grpc.clients.*;

public class ClientFactory {
	
	public static Users getUsersClient() throws InterruptedException {
		URI[] userServers = Discovery.getInstance().knownUrisOf("users", 1);
		if(userServers[0].toString().endsWith("rest")) return new RestUsersClient(userServers[0]);
		return new GrpcUsersClient(userServers[0]);
	}
	
	public static Shorts getShortsClient() throws InterruptedException {
		URI[] userServers = Discovery.getInstance().knownUrisOf("shorts", 1);
		if(userServers[0].toString().endsWith("rest")) return new RestShortsClient(userServers[0]);
		return new GrpcShortsClient(userServers[0]);
	}
	
	public static Blobs getBlobsClient() throws InterruptedException {
		URI[] userServers = Discovery.getInstance().knownUrisOf("blobs", 1);
		if(userServers[0].toString().endsWith("rest")) return new RestBlobsClient(userServers[0]);
		return new GrpcBlobsClient(userServers[0]);
	}

}
