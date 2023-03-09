package io.dapr.components.wrappers;

import dapr.proto.components.v1.State;
import dapr.proto.components.v1.StateStoreGrpc;
import io.dapr.components.domain.state.StateStore;
import io.dapr.v1.ComponentProtos;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.dapr.components.TestUtils.assertThrowsGrpcRuntimeException;
import static io.dapr.components.TestUtils.monoError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StateStoreGrpcComponentWrapperTest {

  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private StateStore mockComponent;
  private StateStoreGrpc.StateStoreBlockingStub client;

  @BeforeEach
  void setUp() throws IOException {
    mockComponent = mock();

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(new StateStoreGrpcComponentWrapper(mockComponent))
        .build()
        .start()
    );

    client = StateStoreGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  @AfterEach
  void tearDown() {
  }

  //
  // Tests for common component aspects
  //

  @Test
  void initHappyCase() {
    final Map<String, String> expectedMetadata = Map.of("key", "value");

    when(mockComponent.init(anyMap())).thenReturn(Mono.empty());

    final var response = client.init(State.InitRequest.newBuilder()
        .setMetadata(ComponentProtos.MetadataRequest.newBuilder()
            .putAllProperties(expectedMetadata)
            .build())
        .build());

    assertThat(response).as("init return just a default InitResponse")
        .isNotNull()
        .isEqualTo(State.InitResponse.getDefaultInstance());
    var capturedMetadata = ArgumentCaptor.forClass(Map.class);
    verify(mockComponent).init(capturedMetadata.capture());
    assertThat(capturedMetadata.getValue())
        .as("An equivalent metadata map should be provided to the component")
        .isNotNull()
        .isEqualTo(expectedMetadata);
  }

  @Test
  void initMonoErrorThrows() {
    when(mockComponent.init(anyMap())).thenReturn(monoError());

    assertThrowsGrpcRuntimeException(() -> client.init(State.InitRequest.newBuilder().build()));
  }


  @Test
  void pingHappyCase() {
    when(mockComponent.ping()).thenReturn(Mono.empty());
    final var response = client.ping(ComponentProtos.PingRequest.getDefaultInstance());
    assertThat(response).isNotNull();
  }

  @Test
  void pingMonoErrorThrows() {
    when(mockComponent.ping()).thenReturn(monoError());

    assertThrowsGrpcRuntimeException(() -> client.ping(ComponentProtos.PingRequest.getDefaultInstance()));
  }

  @Test
  void pingInternalExceptionsThrows() {
    when(mockComponent.ping())
        .thenThrow(new RuntimeException("Ooops!"));

    assertThrowsGrpcRuntimeException(() -> client.ping(ComponentProtos.PingRequest.getDefaultInstance()));
  }

  @Test
  void featuresHappyCase() {
    final var expectedFeatures = List.of("feature1", "feature2");

    when(mockComponent.getFeatures()).thenReturn(Mono.just(expectedFeatures));

    final var response = client.features(ComponentProtos.FeaturesRequest.newBuilder().build());

    assertThat(response)
        .isNotNull();
    assertThat(response.getFeaturesList())
        .as("getFeatures should return the same supported Features as returned by the component")
        .isEqualTo(expectedFeatures);
  }

  @Test
  void featuresMonoErrorThrows() {
    when(mockComponent.getFeatures()).thenReturn(monoError());

    assertThrowsGrpcRuntimeException(() ->
        client.features(ComponentProtos.FeaturesRequest.newBuilder().build()));
  }


  //
  // Component-specific tests
  //


  @Test
  void delete() {
  }

  @Test
  void get() {
  }

  @Test
  void set() {
  }


  @Test
  void bulkDelete() {
  }

  @Test
  void bulkGet() {
  }

  @Test
  void bulkSet() {
  }

  //
  // Aux. methods
  //
}