package io.dapr.components;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TestUtils {
  public static AbstractThrowableAssert<?, ? extends Throwable> assertThrowsGrpcRuntimeException(ThrowableAssert.ThrowingCallable throwingCallable) {
    return assertThatThrownBy(throwingCallable)
        .isInstanceOf(io.grpc.StatusRuntimeException.class);
  }

  public static <T> Mono<T> monoError() {
    return Mono.error(new RuntimeException("Oops"));
  }
}
