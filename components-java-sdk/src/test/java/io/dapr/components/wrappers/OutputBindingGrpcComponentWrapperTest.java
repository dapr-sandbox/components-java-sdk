package io.dapr.components.wrappers;

import com.google.common.base.Function;
import dapr.proto.components.v1.Bindings;
import dapr.proto.components.v1.OutputBindingGrpc;
import io.dapr.components.domain.bindings.OutputBinding;
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

class OutputBindingGrpcComponentWrapperTest {

  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private OutputBinding mockComponent;
  private OutputBindingGrpc.OutputBindingBlockingStub client;

  @BeforeEach
  void setUp() throws IOException {
    mockComponent = mock();

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(new OutputBindingGrpcComponentWrapper(mockComponent))
        .build()
        .start()
    );

    client = OutputBindingGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  //
  // Tests for common component aspects
  //

  @TestFactory
  List<DynamicTest> init() {
    final Function<Map<String, String>, Bindings.OutputBindingInitRequest> initWithFeaturesFactory = (expectedMetadata) -> Bindings.OutputBindingInitRequest.newBuilder()
        .setMetadata(ComponentProtos.MetadataRequest.newBuilder()
            .putAllProperties(expectedMetadata)
            .build())
        .build();
    final Bindings.OutputBindingInitResponse defaultResponseInstance = Bindings.OutputBindingInitResponse.getDefaultInstance();

    return AspectsTestGenerators.generateInitTests(mockComponent, client::init, initWithFeaturesFactory, defaultResponseInstance);
  }

  @TestFactory
  List<DynamicTest> ping() {
    return AspectsTestGenerators.generatePingTests(mockComponent, client::ping);
  }


  //
  // Component-specific tests
  //

  @Test
  void invoke() {
  }

  @Test
  void listOperations() {
  }
}