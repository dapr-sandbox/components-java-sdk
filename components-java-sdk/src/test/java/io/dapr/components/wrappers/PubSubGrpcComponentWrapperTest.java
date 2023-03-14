package io.dapr.components.wrappers;

import com.google.common.base.Function;
import dapr.proto.components.v1.PubSubGrpc;
import dapr.proto.components.v1.Pubsub;
import io.dapr.components.domain.pubsub.PubSub;
import io.dapr.v1.ComponentProtos;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

class PubSubGrpcComponentWrapperTest {

  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private PubSub mockComponent;
  private PubSubGrpc.PubSubBlockingStub client;

  @BeforeEach
  void setUp() throws IOException {
    mockComponent = mock();

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(new PubSubGrpcComponentWrapper(mockComponent))
        .build()
        .start()
    );

    client = PubSubGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  //
  // Tests for common component aspects
  //

  @TestFactory
  List<DynamicTest> init() {
    final Function<Map<String, String>, Pubsub.PubSubInitRequest> initWithFeaturesFactory = (expectedMetadata) -> Pubsub.PubSubInitRequest.newBuilder()
        .setMetadata(ComponentProtos.MetadataRequest.newBuilder()
            .putAllProperties(expectedMetadata)
            .build())
        .build();
    final Pubsub.PubSubInitResponse defaultResponseInstance = Pubsub.PubSubInitResponse.getDefaultInstance();

    return AspectsTestGenerators.generateInitTests(mockComponent, client::init, initWithFeaturesFactory, defaultResponseInstance);
  }

  @TestFactory
  List<DynamicTest> ping() {
    return AspectsTestGenerators.generatePingTests(mockComponent, client::ping);
  }

  @TestFactory
  List<DynamicTest> features() {
    return AspectsTestGenerators.generateGetFeaturesTests(mockComponent, client::features);
  }


  //
  // Component-specific tests
  //


  @Test
  void publish() {
  }

  @Test
  void pullMessages() {
  }
}