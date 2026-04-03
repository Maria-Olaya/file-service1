package eafit.gruopChat.grpc;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Component
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userStub;

    public Optional<UserResponse> getUserById(String userId) {
        try {
            return Optional.of(userStub.getUserById(
                UserIdRequest.newBuilder().setUserId(userId).build()));
        } catch (StatusRuntimeException e) {
            return Optional.empty();
        }
    }

    public boolean existsUser(String userId) {
        try {
            return userStub.existsUser(
                UserIdRequest.newBuilder().setUserId(userId).build()).getExists();
        } catch (StatusRuntimeException e) {
            return false;
        }
    }
}