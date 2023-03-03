/*
 * Copyright 2023 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dapr.components.wrappers;

import io.dapr.components.domain.bindings.ReadRequest;
import io.dapr.components.domain.pubsub.PublishRequest;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * Creates a StreamObserver that adapts and feeds its received requests to a Flux.
 *
 * <p>>gRPC is wierd in that a bi-directional stream receive its input requests by
 * means of a {@link StreamObserver} its RPC message handler returns. Think about it:
 * we return responses on a StreamObserver we receive as argument but we receive requests in a
 * StreamObserver we return as result. Mind bogging, right? </p>
 *
 * <p>Well, the process of adapting this "returned" {@link StreamObserver} to what
 * will turn into an input {@link Flux} by means of an intermediary {@link Sinks.Many} is
 * quite repetitive. This adaptor class simplifies this process. Rejoice.</p>
 *
 * @param <StreamT> The input stream type as per the gRPC interface.
 */
public final class RequestStreamToFluxAdaptor<StreamT> {
  private final StreamObserver<StreamT> requestStreamObserver;
  private final Flux<StreamT> flux;

  /**
   * Constructor.
   *
   */
  public RequestStreamToFluxAdaptor() {
    final Sinks.Many<StreamT> sink = Sinks.many().unicast().onBackpressureBuffer();
    requestStreamObserver = adaptRequestStreamToSink(sink);
    flux = sink.asFlux();
  }

  public StreamObserver<StreamT> requestStreamObserver() {
    return requestStreamObserver;
  }

  public Flux<StreamT> flux() {
    return flux;
  }

  /**
   * Creates a {@link StreamObserver} that forwards all its received events to a {@link Sinks.Many}.
   *
   * @param sink the sink we will forward events (onNext, onError and onComplete) to.
   * @return the adaptor {@link StreamObserver} that forwards all its received events to sink.
   */
  private StreamObserver<StreamT> adaptRequestStreamToSink(final Sinks.Many<StreamT> sink) {
    return new StreamObserver<StreamT>() {
      @Override
      public void onNext(StreamT request) {
        sink.emitNext(request, FAIL_FAST);
      }

      @Override
      public void onError(Throwable throwable) {
        sink.emitError(throwable, FAIL_FAST);
      }

      @Override
      public void onCompleted() {
        sink.emitComplete(FAIL_FAST);
      }
    };
  }

}
