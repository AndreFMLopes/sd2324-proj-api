package tukano.servers.grpc;

import static tukano.impl.grpc.common.DataModelAdaptor.Short_to_GrpcShort;

import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadResult;
import tukano.servers.java.JavaBlobs;

import java.util.List;

public class GrpcBlobsServerStub implements BlobsGrpc.AsyncService, BindableService {

    Blobs impl = new JavaBlobs();

    @Override
    public ServerServiceDefinition bindService() {
        return BlobsGrpc.bindService(this);
    }

    @Override
    public void upload(UploadArgs request, StreamObserver<UploadResult> responseObserver) {
        var res = impl.upload(request.getBlobId(), request.getBlobIdBytes().toByteArray());
        if (!res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(UploadResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void download(DownloadArgs request, StreamObserver<DownloadResult> responseObserver) {
        var res = impl.download(request.getBlobId());
        if (!res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(DownloadResult.newBuilder().setChunk(ByteString.copyFrom(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    protected static Throwable errorCodeToStatus(Result.ErrorCode error) {
        var status = switch (error) {
            case NOT_FOUND -> Status.NOT_FOUND;
            case CONFLICT -> Status.ALREADY_EXISTS;
            case FORBIDDEN -> Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> Status.UNIMPLEMENTED;
            case BAD_REQUEST -> Status.INVALID_ARGUMENT;
            default -> Status.INTERNAL;
        };

        return status.asException();
    }

}
