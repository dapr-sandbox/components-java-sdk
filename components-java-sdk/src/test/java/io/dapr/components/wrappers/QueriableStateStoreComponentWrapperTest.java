package io.dapr.components.wrappers;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import dapr.proto.components.v1.QueriableStateStoreGrpc;
import dapr.proto.components.v1.State;
import io.dapr.components.domain.bindings.InvokeRequest;
import io.dapr.components.domain.state.QueriableStateStore;
import io.dapr.components.domain.state.Query;
import io.dapr.components.domain.state.QueryRequest;
import io.dapr.components.domain.state.QueryResponse;
import io.dapr.components.domain.state.QueryResponseItem;
import io.dapr.components.domain.state.options.Order;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static io.dapr.components.TestData.*;

class QueriableStateStoreComponentWrapperTest {

  public static final String QUERY_RESPONSE_TOKEN_VALUE = "query-response-token-value";
  public static final int LIMIT = 1233;
  public static final String PAGINATION_REQUEST_TOKEN = "pagination-request-token";
  public static final String SORT_KEY_1 = "sort-key";
  public static final String SORT_KEY_2 = "sort-key-2";
  public static final String FILTER_KEY = "filter-key";
  public static final Any FILTER_VALUE = Any.newBuilder().build();
  public static final String NO_ERROR = "";
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private QueriableStateStore mockComponent;
  private QueriableStateStoreGrpc.QueriableStateStoreBlockingStub client;

  @BeforeEach
  void setUp() throws IOException {
    mockComponent = mock();

    // Generate a unique in-process server name.
    final String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(new QueriableStateStoreComponentWrapper(mockComponent))
        .build()
        .start()
    );

    client = QueriableStateStoreGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  @Test
  void query() {
    when(mockComponent.query(any())).thenReturn(Mono.just(
        new QueryResponse(
            List.of(
                new QueryResponseItem(TEST_KEY, TEST_VALUE, ETAG_VALUE, NO_ERROR, CONTENT_TYPE),
                new QueryResponseItem(TEST_KEY_2, TEST_VALUE_2, ETAG_VALUE, NO_ERROR, CONTENT_TYPE)
            ),
            QUERY_RESPONSE_TOKEN_VALUE,
            METADATA_RESPONSE_MAP
        )));

    final State.QueryRequest request = State.QueryRequest.newBuilder()
        .setQuery(State.Query.newBuilder()
            .putAllFilter(Map.of(
                FILTER_KEY, FILTER_VALUE
            ))
            .addAllSort(List.of(
                State.Sorting.newBuilder().setKey(SORT_KEY_1).setOrder(State.Sorting.Order.ASC).build(),
                State.Sorting.newBuilder().setKey(SORT_KEY_2).setOrder(State.Sorting.Order.DESC).build()
            ))
            .setPagination(State.Pagination.newBuilder()
                .setLimit(LIMIT)
                .setToken(PAGINATION_REQUEST_TOKEN)
                .build())
            .build())
        .putAllMetadata(METADATA_REQUEST_MAP)
        .build();

    final State.QueryResponse response = client.query(request);

    assertThat(response).isNotNull();
    assertThat(response.getItemsList()).size().isEqualTo(2);
    validateQueryResponseItem(response.getItems(0), TEST_KEY, TEST_VALUE);
    validateQueryResponseItem(response.getItems(1), TEST_KEY_2, TEST_VALUE_2);
    assertThat(response.getToken()).isEqualTo(QUERY_RESPONSE_TOKEN_VALUE);
    assertThat(response.getMetadataMap()).isEqualTo(METADATA_RESPONSE_MAP);

    var capturedRequest = ArgumentCaptor.forClass(QueryRequest.class);
    verify(mockComponent, times(1)).query(capturedRequest.capture());
    final QueryRequest translatedRequest = capturedRequest.getValue();
    assertThat(translatedRequest).isNotNull();
    assertThat(translatedRequest.query()).isNotNull();
    assertThat(translatedRequest.query().filter().keySet()).contains(FILTER_KEY);
    assertThat(translatedRequest.query().sort()).size().isEqualTo(2);
    assertThat(translatedRequest.query().sort().get(0).key()).isEqualTo(SORT_KEY_1);
    assertThat(translatedRequest.query().sort().get(0).order()).isEqualTo(Order.ASC);
    assertThat(translatedRequest.query().sort().get(1).key()).isEqualTo(SORT_KEY_2);
    assertThat(translatedRequest.query().sort().get(1).order()).isEqualTo(Order.DESC);
    assertThat(translatedRequest.query().pagination()).isNotNull();
    assertThat(translatedRequest.query().pagination().limit()).isEqualTo(LIMIT);
    assertThat(translatedRequest.query().pagination().token()).isEqualTo(PAGINATION_REQUEST_TOKEN);

  }

  private static void validateQueryResponseItem(State.QueryItem item, String testKey, ByteString testValue) {
    assertThat(item.getKey()).isEqualTo(testKey);
    assertThat(item.getData()).isEqualTo(testValue);
    assertThat(item.getEtag().getValue()).isEqualTo(ETAG_VALUE);
    assertThat(item.getError()).isEmpty();
    assertThat(item.getContentType()).isEqualTo(CONTENT_TYPE);
  }
}