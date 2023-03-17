package io.dapr.components.wrappers;

import dapr.proto.components.v1.State;
import dapr.proto.components.v1.TransactionalStateStoreGrpc;
import io.dapr.components.domain.state.DeleteRequest;
import io.dapr.components.domain.state.SetRequest;
import io.dapr.components.domain.state.TransactionalStateRequest;
import io.dapr.components.domain.state.TransactionalStateStore;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static io.dapr.components.TestData.METADATA_REQUEST_MAP;
import static io.dapr.components.wrappers.StateStoreGrpcComponentWrapperTest.makeDeleteRequest;
import static io.dapr.components.wrappers.StateStoreGrpcComponentWrapperTest.makeSetRequest;
import static io.dapr.components.wrappers.StateStoreGrpcComponentWrapperTest.validateTranslatedDeleteRequest;
import static io.dapr.components.wrappers.StateStoreGrpcComponentWrapperTest.validateTranslatedSetRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionalStateStoreComponentWrapperTest {

  public static final String DELETE_KEY = "delete-key";
  public static final String SET_KEY = "set-key";
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private TransactionalStateStore mockComponent;
  private TransactionalStateStoreGrpc.TransactionalStateStoreBlockingStub client;

  @BeforeEach
  void setUp() throws IOException {
    mockComponent = mock();

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(new TransactionalStateStoreComponentWrapper(mockComponent))
        .build()
        .start()
    );

    client = TransactionalStateStoreGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  @Test
  void transact() {
    when(mockComponent.transact(any())).thenReturn(Mono.empty());

    final var requestBuilder = State.TransactionalStateRequest.newBuilder()
        .putAllMetadata(METADATA_REQUEST_MAP);
    // A delete followed by a set
    requestBuilder.addOperationsBuilder().setDelete(
        makeDeleteRequest(State.DeleteRequest.newBuilder(), DELETE_KEY));
    requestBuilder.addOperationsBuilder().setSet(
        makeSetRequest(State.SetRequest.newBuilder(), SET_KEY));
    final var request = requestBuilder.build();

    final State.TransactionalStateResponse response = client.transact(request);

    assertThat(response).isEqualTo(State.TransactionalStateResponse.getDefaultInstance());

    var capturedRequest = ArgumentCaptor.forClass(TransactionalStateRequest.class);
    verify(mockComponent, times(1)).transact(capturedRequest.capture());

    final TransactionalStateRequest translatedRequest = capturedRequest.getValue();
    assertThat(translatedRequest).isNotNull();
    assertThat(translatedRequest.metadata()).isEqualTo(METADATA_REQUEST_MAP);

    assertThat(translatedRequest.operations()).size().isEqualTo(2);

    assertThat(translatedRequest.operations().get(0)).isInstanceOf(DeleteRequest.class);
    validateTranslatedDeleteRequest((DeleteRequest)translatedRequest.operations().get(0), DELETE_KEY);

    assertThat(translatedRequest.operations().get(1)).isInstanceOf(SetRequest.class);
    validateTranslatedSetRequest((SetRequest)translatedRequest.operations().get(1), SET_KEY);

    // now... ignoring order
    translatedRequest.forEachOperation(
        deleteRequest -> assertThat(deleteRequest.key()).isEqualTo(DELETE_KEY),
        setRequest -> assertThat(setRequest.key()).isEqualTo(SET_KEY)
    );
  }
}