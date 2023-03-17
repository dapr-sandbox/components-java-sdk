package io.dapr.components.wrappers;

import com.google.common.base.Function;
import com.google.protobuf.ByteString;
import dapr.proto.components.v1.Bindings;
import dapr.proto.components.v1.OutputBindingGrpc;
import io.dapr.components.domain.bindings.InvokeRequest;
import io.dapr.components.domain.bindings.InvokeResponse;
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
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutputBindingGrpcComponentWrapperTest {

  public static final String OPERATION_NAME = "myOperation";
  public static final String INVOKE_RESPONSE_DATA = "💡invokeData";
  public static final String INVOKE_REQUEST_DATA = "Invoke Request🎒";
  public static final String CONTENT_TYPE = "text/plain;charset=utf-8";
  public static final Map<String, String> REQUEST_METADATA = Map.of("ttlInSeconds", "120");
  public static final Map<String, String> RESPONSE_METADATA = Map.of("response", "metadata");
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
    final InvokeResponse mockedInvokeResponse = new InvokeResponse(
        ByteString.copyFromUtf8(INVOKE_RESPONSE_DATA),
        RESPONSE_METADATA,
        CONTENT_TYPE);

    when(mockComponent.invoke(any())).thenReturn(Mono.just(mockedInvokeResponse));

    final Bindings.InvokeResponse response = client.invoke(Bindings.InvokeRequest.newBuilder()
            .setData(ByteString.copyFromUtf8(INVOKE_REQUEST_DATA))
            .setOperation(OPERATION_NAME)
            .putAllMetadata(REQUEST_METADATA)
        .build());
    var capturedRequest = ArgumentCaptor.forClass(InvokeRequest.class);

    verify(mockComponent, times(1)).invoke(capturedRequest.capture());
    // Validate how request was translated
    final InvokeRequest translatedRequest = capturedRequest.getValue();
    assertThat(translatedRequest.data().toStringUtf8()).isEqualTo(INVOKE_REQUEST_DATA);
    assertThat(translatedRequest.metadata()).isEqualTo(REQUEST_METADATA);
    assertThat(translatedRequest.operation()).isEqualTo(OPERATION_NAME);
    // Validate response
    assertThat(response).isNotNull();
    assertThat(response.getData().toStringUtf8()).isEqualTo(INVOKE_RESPONSE_DATA);
    assertThat(response.getMetadataMap()).isEqualTo(RESPONSE_METADATA);
    assertThat(response.getContentType()).isEqualTo(CONTENT_TYPE);
  }

  @Test
  void listOperations() {
    final List<String> expectedOperationList = List.of(OPERATION_NAME);
    when(mockComponent.listOperations()).thenReturn(Mono.just(expectedOperationList));

    final Bindings.ListOperationsResponse response =
        client.listOperations(Bindings.ListOperationsRequest.newBuilder().build());

    verify(mockComponent, times(1)).listOperations();
    assertThat(response).isNotNull();
    assertThat(response.getOperationsList()).isEqualTo(expectedOperationList);
  }
}