package io.dapr.components.domain.state.options;

import dapr.proto.components.v1.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StateConsistencyTest {

  @Test
  void sanityCheckWithKnownEquivalent() {
    final StateConsistency strongConsistency = StateConsistency.STRONG;
    assertThat(strongConsistency.toProto())
        .isNotNull()
        .isEqualTo(State.StateOptions.StateConsistency.CONSISTENCY_STRONG);
  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromSDK(StateConsistency sdkDomain) {
    final State.StateOptions.StateConsistency proto = sdkDomain.toProto();
    assertThat(proto)
        .as("Instance and static conversion methods produce the same result")
        .isNotNull()
        .isEqualTo(StateConsistency.toProto(sdkDomain));
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part.
    assertThat(StateConsistency.fromProto(proto))
        .isNotNull()
        .isEqualTo(sdkDomain);

  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromProto(State.StateOptions.StateConsistency proto) {
    final StateConsistency sdkDomain = StateConsistency.fromProto(proto);
    assertThat(sdkDomain)
        .as("Conversion to SDK domain is defined")
        .isNotNull();
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part: proto -> sdkDomain -> back to proto
    assertThat(StateConsistency.toProto(sdkDomain))
        .isNotNull()
        .isEqualTo(proto);
  }
}