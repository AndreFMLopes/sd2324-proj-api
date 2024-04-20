package tukano.clients.rest;

import java.net.URI;
import java.util.List;
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
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestShorts;

public class RestShortsClient implements Shorts {

	private static Logger Log = Logger.getLogger(RestShortsClient.class.getName());

	protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	protected static final int MAX_RETRIES = 10;
	protected static final int RETRY_SLEEP = 5000;

	final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestShortsClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();

		config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestShorts.PATH);
    }

    @Override
    public Result<Short> createShort(String userId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId)
                        .queryParam(RestShorts.PWD, pwd)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(null));

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(Short.class));
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
    public Result<Void> deleteShort(String shortId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(shortId).queryParam(RestShorts.PWD, pwd)
                        .request().delete();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
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
    public Result<Short> getShort(String shortId) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(shortId).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(Short.class));
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                tukano.utils.Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<List<String>> getShorts(String userId) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId + RestShorts.SHORTS).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(List.class));
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
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId1).queryParam(RestShorts.PWD, pwd).path(userId2 + RestShorts.FOLLOWERS)
                        .request().post(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
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
    public Result<List<String>> followers(String userId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId).queryParam(RestShorts.PWD, pwd).path(RestShorts.FOLLOWERS)
                        .request().accept(MediaType.APPLICATION_JSON).get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(List.class));
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
    public Result<Void> like(String shortId, String userId, boolean isLiked, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(shortId).path(userId).queryParam(RestShorts.PWD, pwd).path(RestShorts.LIKES)
                        .request().post(Entity.json(null));

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
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

    @SuppressWarnings("unchecked")
    @Override
    public Result<List<String>> likes(String shortId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(shortId).queryParam(RestShorts.PWD, pwd).path(RestShorts.LIKES)
                        .request().accept(MediaType.APPLICATION_JSON).get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(List.class));
            } catch (ProcessingException x) {
                Log.info(x.getMessage());
                tukano.utils.Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<List<String>> getFeed(String userId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId).queryParam(RestShorts.PWD, pwd).path(RestShorts.FEED)
                        .request().accept(MediaType.APPLICATION_JSON).get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(List.class));
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
    public Result<Void> deleteAllAboutUser(String userId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId).queryParam(RestShorts.PWD, pwd).path(RestShorts.DELETE)
                        .request().delete();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
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
    public Result<Void> checkBlobId(String blobIdToCheck) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(blobIdToCheck).path(RestShorts.CHECK)
                        .request().get();

                var status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
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
