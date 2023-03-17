package io.dapr.components.wrappers;

import com.google.common.base.Function;
import dapr.proto.components.v1.State;
import dapr.proto.components.v1.StateStoreGrpc;
import io.dapr.components.TestData;
import io.dapr.components.domain.state.BulkGetStateItem;
import io.dapr.components.domain.state.DeleteRequest;
import io.dapr.components.domain.state.Exceptions;
import io.dapr.components.domain.state.GetRequest;
import io.dapr.components.domain.state.GetResponse;
import io.dapr.components.domain.state.SetRequest;
import io.dapr.components.domain.state.StateStore;
import io.dapr.components.domain.state.options.StateConcurrency;
import io.dapr.components.domain.state.options.StateConsistency;
import io.dapr.v1.ComponentProtos;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

  @TestFactory
  List<DynamicTest> init() {
    final Function<Map<String, String>, State.InitRequest> initWithFeaturesFactory = (expectedMetadata) -> State.InitRequest.newBuilder()
        .setMetadata(ComponentProtos.MetadataRequest.newBuilder()
            .putAllProperties(expectedMetadata)
            .build())
        .build();
    final State.InitResponse defaultResponseInstance = State.InitResponse.getDefaultInstance();

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
  void delete() {
    // Happy case: delete was accepted.
    when(mockComponent.delete(any())).thenReturn(Mono.empty());

    final State.DeleteResponse response = client.delete(makeDeleteRequest(State.DeleteRequest.newBuilder(), TestData.TEST_KEY));

    assertThat(response)
        .isNotNull()
        .isEqualTo(State.DeleteResponse.getDefaultInstance());

    var capturedLocalRequest = ArgumentCaptor.forClass(DeleteRequest.class);
    verify(mockComponent, times(1)).delete(capturedLocalRequest.capture());

    final var translatedRequest = capturedLocalRequest.getValue();
    validateTranslatedDeleteRequest(translatedRequest, TestData.TEST_KEY);
  }

  static State.DeleteRequest makeDeleteRequest(State.DeleteRequest.Builder newBuilder, String testKey) {
    return newBuilder
        .setKey(testKey)
        .putAllMetadata(TestData.METADATA_REQUEST_MAP)
        .setOptions(State.StateOptions.newBuilder()
            .setConcurrency(State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE)
            .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL)
            .build())
        .setEtag(State.Etag.newBuilder()
            .setValue(TestData.ETAG_VALUE).build())
        .build();
  }

  static void validateTranslatedDeleteRequest(DeleteRequest translatedDeleteRequest, String testKey) {
    assertThat(translatedDeleteRequest.key()).isEqualTo(testKey);
    assertThat(translatedDeleteRequest.metadata()).isEqualTo(TestData.METADATA_REQUEST_MAP);
    assertThat(translatedDeleteRequest.etag()).isEqualTo(TestData.ETAG_VALUE);
    assertThat(translatedDeleteRequest.options().concurrency()).isEqualTo(StateConcurrency.LAST_WRITE);
    assertThat(translatedDeleteRequest.options().consistency()).isEqualTo(StateConsistency.EVENTUAL);
  }

  @Test
  void get() {
    // Happy case: data found
    when(mockComponent.get(any())).thenReturn(Mono.just(
        new GetResponse(TestData.TEST_VALUE, TestData.ETAG_VALUE, TestData.METADATA_RESPONSE_MAP, TestData.CONTENT_TYPE )));

    final State.GetResponse response = client.get(State.GetRequest.newBuilder()
        .setKey(TestData.TEST_KEY)
        .putAllMetadata(TestData.METADATA_REQUEST_MAP)
        .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL)
        .build());


    assertThat(response).isNotNull();
    assertThat(response.getData()).isEqualTo(TestData.TEST_VALUE);
    assertThat(response.getEtag().getValue()).isEqualTo(TestData.ETAG_VALUE);
    assertThat(response.getMetadataMap()).isEqualTo(TestData.METADATA_RESPONSE_MAP);
    assertThat(response.getContentType()).isEqualTo(TestData.CONTENT_TYPE);

    var capturedLocalRequest = ArgumentCaptor.forClass(GetRequest.class);
    verify(mockComponent, times(1)).get(capturedLocalRequest.capture());
    final var translatedRequest = capturedLocalRequest.getValue();
    assertThat(translatedRequest.key()).isEqualTo(TestData.TEST_KEY);
    assertThat(translatedRequest.metadata()).isEqualTo(TestData.METADATA_REQUEST_MAP);
    assertThat(translatedRequest.consistency()).isEqualTo(StateConsistency.EVENTUAL);
  }

  @Test
  void set() {
    // Happy case: set was accepted.
    when(mockComponent.set(any())).thenReturn(Mono.empty());

    final State.SetResponse response = client.set(makeSetRequest(State.SetRequest.newBuilder(), TestData.TEST_KEY)
        .build());

    assertThat(response)
        .isNotNull()
        .isEqualTo(State.SetResponse.getDefaultInstance());

    var capturedLocalRequest = ArgumentCaptor.forClass(SetRequest.class);
    verify(mockComponent, times(1)).set(capturedLocalRequest.capture());
    final var translatedRequest = capturedLocalRequest.getValue();
    validateTranslatedSetRequest(translatedRequest, TestData.TEST_KEY);
  }

  static State.SetRequest.Builder makeSetRequest(State.SetRequest.Builder builder, String testKey) {
    return builder
        .setKey(testKey)
        .setValue(TestData.TEST_VALUE)
        .putAllMetadata(TestData.METADATA_REQUEST_MAP)
        .setOptions(State.StateOptions.newBuilder()
            .setConcurrency(State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE)
            .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL)
            .build())
        .setEtag(State.Etag.newBuilder()
            .setValue(TestData.ETAG_VALUE).build())
        .setContentType(TestData.CONTENT_TYPE);
  }

  static void validateTranslatedSetRequest(SetRequest setRequest, String testKey) {
    assertThat(setRequest.key()).isEqualTo(testKey);
    assertThat(setRequest.value()).isEqualTo(TestData.TEST_VALUE);
    assertThat(setRequest.metadata()).isEqualTo(TestData.METADATA_REQUEST_MAP);
    assertThat(setRequest.etag()).isEqualTo(TestData.ETAG_VALUE);
    assertThat(setRequest.options().concurrency()).isEqualTo(StateConcurrency.LAST_WRITE);
    assertThat(setRequest.options().consistency()).isEqualTo(StateConsistency.EVENTUAL);
  }

  @Test
  void setWithInvalidEtag() {
    // Happy case: set was accepted.
    when(mockComponent.set(any()))
        .thenReturn(Mono.error(Exceptions.getInvalidETagException(
            "No Emojis, please!",
            "You used an emoji in the etag field")));

    StatusRuntimeException grpcException = catchThrowableOfType(
        () -> client.set(State.SetRequest.newBuilder()
          .setKey(TestData.TEST_KEY)
          .setValue(TestData.TEST_VALUE)
          .setEtag(State.Etag.newBuilder()
              .setValue("😜").build())
          .build()),
        StatusRuntimeException.class);

    assertThat(grpcException.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
  }


  @Test
  void bulkDelete() {
    // Happy case: bulkDelete was collectively successful.
    when(mockComponent.bulkDelete(any())).thenReturn(Mono.empty());

    final State.BulkDeleteRequest.Builder requestBuilder = State.BulkDeleteRequest.newBuilder();
    // Key #1
    makeDeleteRequest(requestBuilder.addItemsBuilder(), TestData.TEST_KEY);
    // Key #2
    makeDeleteRequest(requestBuilder.addItemsBuilder(), TestData.TEST_KEY_2);
    final State.BulkDeleteResponse response = client.bulkDelete(requestBuilder.build());


    assertThat(response)
        .isNotNull()
        .isEqualTo(State.BulkDeleteResponse.getDefaultInstance());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<DeleteRequest>> capturedLocalRequest = ArgumentCaptor.forClass(List.class);
    verify(mockComponent, times(1)).bulkDelete(capturedLocalRequest.capture());

    var translatedRequest = capturedLocalRequest.getValue();
    assertThat(translatedRequest.size()).isEqualTo(2);

    validateTranslatedDeleteRequest(translatedRequest.get(0), TestData.TEST_KEY);
    validateTranslatedDeleteRequest(translatedRequest.get(1), TestData.TEST_KEY_2);
  }

  @Test
  void bulkGet() {
    // Happy case: data found
    when(mockComponent.bulkGet(any())).thenReturn(Flux.just(
        new BulkGetStateItem(
            TestData.TEST_KEY,
            // TODO(tmacam) Are we really sure that a Mono.just is the best way to wrap a response
            //              in a BulkGetStateItem? We might want to revisit this.
            Mono.just(new GetResponse(TestData.TEST_VALUE, TestData.ETAG_VALUE, TestData.METADATA_RESPONSE_MAP, TestData.CONTENT_TYPE ))
        ),
        new BulkGetStateItem(
            TestData.TEST_KEY_2,
            Mono.just(new GetResponse(TestData.TEST_VALUE_2, TestData.ETAG_VALUE, TestData.METADATA_RESPONSE_MAP, TestData.CONTENT_TYPE ))
          )
        )
    );

    final State.BulkGetRequest.Builder requestBuilder = State.BulkGetRequest.newBuilder();

    requestBuilder.addItemsBuilder()
        .setKey(TestData.TEST_KEY)
        .putAllMetadata(TestData.METADATA_REQUEST_MAP)
        .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL);
    requestBuilder.addItemsBuilder()
        .setKey(TestData.TEST_KEY_2)
        .putAllMetadata(TestData.METADATA_REQUEST_MAP)
        .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_STRONG);

    final State.BulkGetResponse response = client.bulkGet(requestBuilder.build());


    assertThat(response).isNotNull();
    assertThat(response.getItemsCount()).isEqualTo(2);

    assertThat(response.getItems(0).getData()).isEqualTo(TestData.TEST_VALUE);
    assertThat(response.getItems(0).getEtag().getValue()).isEqualTo(TestData.ETAG_VALUE);
    assertThat(response.getItems(0).getMetadataMap()).isEqualTo(TestData.METADATA_RESPONSE_MAP);
    assertThat(response.getItems(0).getContentType()).isEqualTo(TestData.CONTENT_TYPE);

    assertThat(response.getItems(1).getData()).isEqualTo(TestData.TEST_VALUE_2);
    assertThat(response.getItems(1).getEtag().getValue()).isEqualTo(TestData.ETAG_VALUE);
    assertThat(response.getItems(1).getMetadataMap()).isEqualTo(TestData.METADATA_RESPONSE_MAP);
    assertThat(response.getItems(1).getContentType()).isEqualTo(TestData.CONTENT_TYPE);


    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<GetRequest>> capturedLocalRequest = ArgumentCaptor.forClass(List.class);
    verify(mockComponent, times(1)).bulkGet(capturedLocalRequest.capture());

    final var translatedRequest = capturedLocalRequest.getValue();
    assertThat(translatedRequest.size()).isEqualTo(2);

    assertThat(translatedRequest.get(0).key()).isEqualTo(TestData.TEST_KEY);
    assertThat(translatedRequest.get(0).metadata()).isEqualTo(TestData.METADATA_REQUEST_MAP);
    assertThat(translatedRequest.get(0).consistency()).isEqualTo(StateConsistency.EVENTUAL);

    assertThat(translatedRequest.get(1).key()).isEqualTo(TestData.TEST_KEY_2);
    assertThat(translatedRequest.get(1).metadata()).isEqualTo(TestData.METADATA_REQUEST_MAP);
    assertThat(translatedRequest.get(1).consistency()).isEqualTo(StateConsistency.STRONG);
  }

  @Test
  void bulkSet() {
    // Happy case: set was accepted.
    when(mockComponent.bulkSet(any())).thenReturn(Mono.empty());

    final State.BulkSetRequest.Builder requestBuilder = State.BulkSetRequest.newBuilder();
    makeSetRequest(requestBuilder.addItemsBuilder(), TestData.TEST_KEY);
    makeSetRequest(requestBuilder.addItemsBuilder(), TestData.TEST_KEY_2);

    final State.BulkSetResponse response = client.bulkSet(requestBuilder.build());


    assertThat(response)
        .isNotNull()
        .isEqualTo(State.BulkSetResponse.getDefaultInstance());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<SetRequest>> capturedLocalRequest = ArgumentCaptor.forClass(List.class);
    verify(mockComponent, times(1)).bulkSet(capturedLocalRequest.capture());

    var translatedRequest = capturedLocalRequest.getValue();
    assertThat(translatedRequest.size()).isEqualTo(2);

    validateTranslatedSetRequest(translatedRequest.get(0), TestData.TEST_KEY);
    validateTranslatedSetRequest(translatedRequest.get(1), TestData.TEST_KEY_2);
  }

}