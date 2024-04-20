package tukano.clients.rest;

import java.net.URI;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientProperties;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestBlobs;

public class RestBlobsClient implements Blobs{

	private static Logger Log = Logger.getLogger(RestBlobsClient.class.getName());

	protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int MAX_RETRIES = 4;
	protected static final int RETRY_SLEEP = 5000;
	
	final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
	
	public RestBlobsClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();

		config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestBlobs.PATH );
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		for (int i = 0; i < MAX_RETRIES; i++) {
			try {
				Response r = target.path(blobId).request()
						.post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));

				var status = r.getStatus();
				if( status != Status.OK.getStatusCode() )
					return Result.error( getErrorCodeFrom(status));
				else
					return Result.ok();
			} catch (ProcessingException x) {
				Log.info(x.getMessage());
				tukano.utils.Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return Result.error(ErrorCode.TIMEOUT);
	}

	@Override
	public Result<byte[]> download(String blobId) {
		for (int i = 0; i < MAX_RETRIES; i++) {
			try {
				Response r = target.path(blobId).request()
						.accept(MediaType.APPLICATION_OCTET_STREAM).get();

				var status = r.getStatus();
				if( status != Status.OK.getStatusCode() )
					return Result.error( getErrorCodeFrom(status));
				else
					return Result.ok( r.readEntity( byte[].class ));
			} catch (ProcessingException x) {
				Log.info(x.getMessage());
				tukano.utils.Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return Result.error(ErrorCode.TIMEOUT);
	}

	@Override
	public Result<Void> deleteBlob(String blobId) {
		for (int i = 0; i < MAX_RETRIES; i++) {
			try {
				Response r = target.path(blobId)
						.request()
						.delete();

				var status = r.getStatus();
				if( status != Status.OK.getStatusCode() )
					return Result.error( getErrorCodeFrom(status));
				else
					return Result.ok();
			} catch (ProcessingException x) {
				Log.info(x.getMessage());
				tukano.utils.Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return Result.error(ErrorCode.TIMEOUT);
	}

	public static ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
		case 200, 209 -> ErrorCode.OK;
		case 409 -> ErrorCode.CONFLICT;
		case 403 -> ErrorCode.FORBIDDEN;
		case 404 -> ErrorCode.NOT_FOUND;
		case 400 -> ErrorCode.BAD_REQUEST;
		case 500 -> ErrorCode.INTERNAL_ERROR;
		case 501 -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}
}
