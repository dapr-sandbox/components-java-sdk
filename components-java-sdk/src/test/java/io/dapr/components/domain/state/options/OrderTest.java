package io.dapr.components.domain.state.options;

import dapr.proto.components.v1.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

  @Test
  void sanityCheckWithKnownEquivalent() {
    final Order ascendingOrder = Order.ASC;
    assertThat(ascendingOrder.toProto())
        .isNotNull()
        .isEqualTo(State.Sorting.Order.ASC);
  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromSDK(Order sdkDomain) {
    final State.Sorting.Order proto = sdkDomain.toProto();
    assertThat(proto)
        .as("Instance and static conversion methods produce the same result")
        .isNotNull()
        .isEqualTo(Order.toProto(sdkDomain));
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part.
    assertThat(Order.fromProto(proto))
        .isNotNull()
        .isEqualTo(sdkDomain);

  }

  @ParameterizedTest
  @EnumSource
  void testLoopTransformationFromProto(State.Sorting.Order proto) {
    final Order sdkDomain = Order.fromProto(proto);
    assertThat(sdkDomain)
        .as("Conversion to SDK domain is defined")
        .isNotNull();
    assertThat(proto.toString())
        .as("SDK enums are always a substring of their Protocol Buffer model equivalent.")
        .contains(sdkDomain.toString());
    // Now the loop part: proto -> sdkDomain -> back to proto
    assertThat(Order.toProto(sdkDomain))
        .isNotNull()
        .isEqualTo(proto);
  }
}