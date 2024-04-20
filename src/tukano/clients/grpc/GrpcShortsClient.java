package tukano.clients.grpc;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.impl.grpc.common.DataModelAdaptor.GrpcShort_to_Short;
import static tukano.impl.grpc.common.DataModelAdaptor.Short_to_GrpcShort;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import tukano.api.Short;
import tukano.api.java.Shorts;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.*;

public class GrpcShortsClient implements Shorts{



	private static final long GRPC_REQUEST_TIMEOUT = 5000;
	final ShortsGrpc.ShortsBlockingStub stub;

	public GrpcShortsClient(URI serverURI) {
		var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
		stub = ShortsGrpc.newBlockingStub( channel ).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	@Override
	public Result<Short> createShort(String userId, String password) {
		return toJavaResult(() -> {
			var res = stub.createShort(CreateShortArgs.newBuilder()
						.setUserId(userId).setPassword(password)
						.build());
			return GrpcShort_to_Short(res.getValue());
		});
	}



	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		return toJavaResult(() -> {
			var res = stub.deleteShort(DeleteShortArgs.newBuilder().setShortId(shortId).setPassword(password).build());
			return null ;
		});
	}



	@Override
	public Result<Short> getShort(String shortId) {
		return toJavaResult(() -> {
			var res = stub.getShort(GetShortArgs.newBuilder()
						.setShortId(shortId)
						.build());
			return GrpcShort_to_Short(res.getValue());
		});
	}



	@Override
	public Result<List<String>> getShorts(String userId) {
		return toJavaResult(() -> {
			var res = stub.getShorts(GetShortsArgs.newBuilder()
						.setUserId(userId)
						.build());
			return res.getShortIdList();
		});
	}



	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		return toJavaResult(() -> {
			var res = stub.follow(FollowArgs.newBuilder()
						.setUserId1(userId1).setUserId2(userId2)
						.setIsFollowing(isFollowing).setPassword(password)
						.build());
			return null ;
		});
	}



	@Override
	public Result<List<String>> followers(String userId, String password) {
		return toJavaResult(() -> {
			var res = stub.followers(FollowersArgs.newBuilder()
						.setUserId(userId).setPassword(password)
						.build());
			return res.getUserIdList();
		});
	}



	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		return toJavaResult(() -> {
			var res = stub.like(LikeArgs.newBuilder()
						.setShortId(shortId).setUserId(userId)
						.setIsLiked(isLiked).setPassword(password)
						.build());
			return null ;
		});
	}



	@Override
	public Result<List<String>> likes(String shortId, String password) {
		return toJavaResult(() -> {
			var res = stub.likes(LikesArgs.newBuilder()
						.setShortId(shortId).setPassword(password)
						.build());
			return res.getUserIdList();
		});
	}



	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		return toJavaResult(() -> {
			var res = stub.getFeed(GetFeedArgs.newBuilder()
						.setUserId(userId).setPassword(password)
						.build());
			return res.getShortIdList();
		});
	}
	
	static <T> Result<T> toJavaResult(Supplier<T> func) {
		try {
			return ok(func.get());
		} catch(StatusRuntimeException sre) {
			var code = sre.getStatus().getCode();
			if( code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED )
				throw sre;
			return error( statusToErrorCode( sre.getStatus() ) );
		}
	}
	
	@Override
	public Result<Void> deleteAllAboutUser(String userId, String password) {
		return toJavaResult(() -> {
			var res = stub.deleteAllAboutUser(DeleteAllAboutUserArgs.newBuilder().setUserId(userId).setPassword(password).build());
			return null ;
		});
	}

	@Override
	public Result<Void> checkBlobId(String blobIdToCheck) {
		return toJavaResult(() -> {
			var res = stub.checkBlobId(CheckBlobIdArgs.newBuilder().setBlobIdToCheck(blobIdToCheck).build());
			return null ;
		});
	}
	
	static ErrorCode statusToErrorCode( Status status ) {
    	return switch( status.getCode() ) {
    		case OK -> ErrorCode.OK;
    		case NOT_FOUND -> ErrorCode.NOT_FOUND;
    		case ALREADY_EXISTS -> ErrorCode.CONFLICT;
    		case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
    		case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
    		case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
    		default -> ErrorCode.INTERNAL_ERROR;
    	};
    }	


}
