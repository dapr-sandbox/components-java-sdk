package io.dapr.components.domain.state.options;

import dapr.proto.components.v1.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StateConcurrencyTest {

  @Test
  void sanityCheckWithKnownEquivalent() {
    final StateConcurrency lastWrite = StateConcurrency.LAST_WRITE;
    assertThat(lastWrite.toProto())
        .isNotNull()
        .isEqualTo(State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE);
  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromSDK(StateConcurrency sdkDomain) {
    final State.StateOptions.StateConcurrency proto = sdkDomain.toProto();
    assertThat(proto)
        .as("Instance and static conversion methods produce the same result")
        .isNotNull()
        .isEqualTo(StateConcurrency.toProto(sdkDomain));
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part.
    assertThat(StateConcurrency.fromProto(proto))
        .isNotNull()
        .isEqualTo(sdkDomain);

  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromProto(State.StateOptions.StateConcurrency proto) {
    final StateConcurrency sdkDomain = StateConcurrency.fromProto(proto);
    assertThat(sdkDomain)
        .as("Conversion to SDK domain is defined")
        .isNotNull();
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part: proto -> sdkDomain -> back to proto
    assertThat(StateConcurrency.toProto(sdkDomain))
        .isNotNull()
        .isEqualTo(proto);
  }
}