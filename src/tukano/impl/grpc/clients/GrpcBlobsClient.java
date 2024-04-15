package tukano.impl.grpc.clients;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.*;

public class GrpcBlobsClient implements Blobs{
	
	private static final long GRPC_REQUEST_TIMEOUT = 5000;
	final BlobsGrpc.BlobsBlockingStub stub;

	public GrpcBlobsClient(URI serverURI) {
		var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
		stub = BlobsGrpc.newBlockingStub( channel ).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		return toJavaResult(() -> {
			var res = stub.upload(UploadArgs.newBuilder()
						.setBlobId(blobId).setData(ByteString.copyFrom(bytes))
						.build());
			return null ;
		});
	}

	@Override
	public Result<byte[]> download(String blobId) {
		return toJavaResult(() -> {
			var res = stub.download(DownloadArgs.newBuilder().setBlobId(blobId).build());
			List<ByteString> temp = new ArrayList<ByteString>();
			while(res.hasNext()) {
				temp.add(res.next().getChunk());
			}
			ByteString bs = ByteString.copyFrom(temp);
			byte[] bytes = new byte[bs.size()];
			bs.copyTo(bytes, 0);
			return bytes;
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
