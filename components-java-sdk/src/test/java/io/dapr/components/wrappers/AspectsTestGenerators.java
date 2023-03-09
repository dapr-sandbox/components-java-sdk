package io.dapr.components.wrappers;

import com.google.common.base.Function;
import io.dapr.components.aspects.AdvertisesFeatures;
import io.dapr.components.aspects.InitializableWithProperties;
import io.dapr.components.aspects.Pingable;
import io.dapr.v1.ComponentProtos;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.dapr.components.TestUtils.assertThrowsGrpcRuntimeException;
import static io.dapr.components.TestUtils.monoError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Generates dynamic tests for common aspects such as {@link io.dapr.components.aspects.Pingable},
 * {@link io.dapr.components.aspects.AdvertisesFeatures} and
 * {@link io.dapr.components.aspects.InitializableWithProperties}.
 */
public class AspectsTestGenerators {
  // The amount of contortions we had to do to make this test generic...
  static <TRequest, TResponse> List<DynamicTest> generateInitTests(
      InitializableWithProperties component,
      Function<TRequest, TResponse> clientInit,
      Function<Map<String, String>, TRequest> initWithFeaturesFactory,
      TResponse defaultResponseInstance) {
    return List.of(
        dynamicTest("init - HappyCase", () -> {
          final Map<String, String> expectedMetadata = Map.of("key", "value");

          when(component.init(anyMap())).thenReturn(Mono.empty());

          final TRequest initRequest = initWithFeaturesFactory.apply(expectedMetadata);
          final var response = clientInit.apply(initRequest);

          assertThat(response).as("init return just a default InitResponse")
              .isNotNull()
              .isEqualTo(defaultResponseInstance);
          var capturedMetadata = ArgumentCaptor.forClass(Map.class);
          verify(component).init(capturedMetadata.capture());
          assertThat(capturedMetadata.getValue())
              .as("An equivalent metadata map should be provided to the component")
              .isNotNull()
              .isEqualTo(expectedMetadata);
        }),
        dynamicTest("init - MonoErrorThrows", () -> {
          when(component.init(anyMap())).thenReturn(monoError());

          final TRequest initRequest = initWithFeaturesFactory.apply(Collections.emptyMap());

          assertThrowsGrpcRuntimeException(() -> clientInit.apply(initRequest));
          verify(component, times(1)).init(Collections.emptyMap());
        })
    );
  }

  static List<DynamicTest> generatePingTests(Pingable component, Function<ComponentProtos.PingRequest, ComponentProtos.PingResponse> clientPing) {
    return List.of(
        dynamicTest("ping happy case", () -> {
          reset(component); // Reset mock - this is not a regular @Test
          when(component.ping()).thenReturn(Mono.empty());

          final var response = clientPing.apply(ComponentProtos.PingRequest.getDefaultInstance());

          verify(component, times(1)).ping();
          assertThat(response).isNotNull();
        }),
        dynamicTest("ping returning a Mono.error becomes an exception on the client side", () -> {
          reset(component); // Reset mock - this is not a regular @Test
          when(component.ping()).thenReturn(monoError());

          assertThrowsGrpcRuntimeException(() -> clientPing.apply(ComponentProtos.PingRequest.getDefaultInstance()));

          verify(component, times(1)).ping();
        }),
        dynamicTest("ping throwing an exception also becomes an exception on the client side", () -> {
          reset(component); // Reset mock - this is not a regular @Test
          when(component.ping())
              .thenThrow(new RuntimeException("Ops! Ping was called and I didn't know what to do!"));

          assertThrowsGrpcRuntimeException(() -> clientPing.apply(ComponentProtos.PingRequest.getDefaultInstance()));

          verify(component, times(1)).ping();
        })
    );
  }

  @NotNull
  static List<DynamicTest> generateGetFeaturesTests(AdvertisesFeatures component, Function<ComponentProtos.FeaturesRequest, ComponentProtos.FeaturesResponse> clientFeatures) {
    return List.of(
        dynamicTest("features - happy case", () -> {
          final var expectedFeatures = List.of("feature1", "feature2");

          reset(component); // Reset mock - this is not a regular @Test
          when(component.getFeatures()).thenReturn(Mono.just(expectedFeatures));

          final var response = clientFeatures.apply(ComponentProtos.FeaturesRequest.newBuilder().build());

          verify(component, times(1)).getFeatures();
          assertThat(response)
              .isNotNull();
          assertThat(response.getFeaturesList())
              .as("getFeatures should return the same supported Features as returned by the component")
              .isEqualTo(expectedFeatures);
        }),
        dynamicTest("features - returning a Mono.error becomes an exception on the client side", () -> {
          reset(component); // Reset mock - this is not a regular @Test
          when(component.getFeatures()).thenReturn(monoError());

          assertThrowsGrpcRuntimeException(() ->
              clientFeatures.apply(ComponentProtos.FeaturesRequest.newBuilder().build()));
          verify(component, times(1)).getFeatures();
        }),
        dynamicTest("features - throwing an exception also becomes an exception on the client side", () -> {
          reset(component); // Reset mock - this is not a regular @Test
          when(component.getFeatures())
              .thenThrow(new RuntimeException("Ops! getFeatures was called and I didn't know what to do!"));

          assertThrowsGrpcRuntimeException(() ->
              clientFeatures.apply(ComponentProtos.FeaturesRequest.newBuilder().build()));
          verify(component, times(1)).getFeatures();
        })
    );
  }
}
