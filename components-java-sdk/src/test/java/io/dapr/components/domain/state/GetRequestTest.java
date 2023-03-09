package io.dapr.components.domain.state;

import dapr.proto.components.v1.State;
import io.dapr.components.domain.state.options.StateConsistency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GetRequestTest {


  static Stream<State.GetRequest> emptyProtoProvider() {
    return Stream.of(
        State.GetRequest.newBuilder().build(),
        State.GetRequest.getDefaultInstance());
  }


  @ParameterizedTest
  @MethodSource("emptyProtoProvider")
  void testDefaultInstance(State.GetRequest requestAsProto) {
    var request = GetRequest.fromProto(requestAsProto);

    assertThat(request.key()).isNotNull().isEmpty();
    assertThat(request.metadata()).isNotNull().isEmpty();
    assertThat(request.consistency()).isNotNull().isEqualTo(StateConsistency.UNSPECIFIED);
  }

  @Test
  void allFields() {
    final String expectedKey = "request-key";
    final Map<String, String> expectedMetadata = Map.of(
        "keyA", "valueA",
        "keyB", "valueB");
    final StateConsistency expectedConsistency = StateConsistency.EVENTUAL;

    State.GetRequest protoBaseRequest = State.GetRequest.newBuilder()
        .setKey(expectedKey)
        .putAllMetadata(expectedMetadata)
        .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL)
        .build();

    final GetRequest sdkRequest = GetRequest.fromProto(protoBaseRequest);

    assertThat(sdkRequest).isNotNull();
    assertThat(sdkRequest.key()).isEqualTo(expectedKey);
    assertThat(sdkRequest.metadata()).isEqualTo(expectedMetadata);
    assertThat(sdkRequest.consistency()).isEqualTo(expectedConsistency);
  }
}