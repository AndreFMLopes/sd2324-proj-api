package tukano.servers.grpc;

import static tukano.impl.grpc.common.DataModelAdaptor.GrpcUser_to_User;
import static tukano.impl.grpc.common.DataModelAdaptor.User_to_GrpcUser;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.CreateUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.CreateUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GetUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.UpdateUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.UpdateUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.DeleteUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.DeleteUserResult;
import tukano.impl.grpc.generated_java.UsersProtoBuf.SearchUserArgs;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import tukano.servers.java.JavaUsers;

public class GrpcUsersServerStub implements UsersGrpc.AsyncService, BindableService {

    Users impl = new JavaUsers();

    @Override
    public ServerServiceDefinition bindService() {
        return UsersGrpc.bindService(this);
    }

    @Override
    public void createUser(CreateUserArgs request, StreamObserver<CreateUserResult> responseObserver) {
        var res = impl.createUser(GrpcUser_to_User(request.getUser()));
        if (! res.isOK() ) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(CreateUserResult.newBuilder().setUserId(res.value()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUser(GetUserArgs request, StreamObserver<GetUserResult> responseObserver) {
        var res = impl.getUser(request.getUserId(), request.getPassword());
        if (! res.isOK() ) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(GetUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateUser(UpdateUserArgs request, StreamObserver<UpdateUserResult> responseObserver) {
        var res = impl.updateUser(request.getUserId(), request.getPassword(), GrpcUser_to_User(request.getUser()));
        if (! res.isOK() ) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(UpdateUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteUser(DeleteUserArgs request, StreamObserver<DeleteUserResult> responseObserver) {
        var res = impl.deleteUser(request.getUserId(), request.getPassword());
        if (! res.isOK() ) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            responseObserver.onNext(DeleteUserResult.newBuilder().setUser(User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void searchUsers(SearchUserArgs request, StreamObserver<GrpcUser> responseObserver) {
        var res = impl.searchUsers(request.getPattern());
        if (! res.isOK() ) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        } else {
            for (User u: res.value()) {
                responseObserver.onNext(GrpcUser.newBuilder().setUserId(u.getUserId())
                        .setPassword(u.getPwd())
                        .setEmail(u.getEmail())
                        .setDisplayName(u.getDisplayName())
                        .build());
            }
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

        return  status.asException();
    }

}
