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
import java.util.function.Function;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * Creates a StreamObserver that adapts and feeds its received requests to a Flux.
 *
 * <p>It also does peaks the very first response and stores it aside becasue both
 * {@link io.dapr.components.domain.bindings.InputBinding#read(ReadRequest, Flux)} and
 * {@link io.dapr.components.domain.pubsub.PubSub#publish(PublishRequest)} have
 * this weird thing in that the very first message in the request/input stream is special.
 * </p>
 *
 * <p>>gRPC is wierd in that a bi-directional stream receive its input requests by
 * means of a {@link StreamObserver} its an RPC message handler return. Think about it:
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

  boolean isThisTheFirstItem = true;

  /**
   * Constructor.
   *
   * @param onFirstCallback the callback we invoke upon receiving the very first request.
   *                       Its first argument is the very first request received and the second
   *                       the remaining requests as a flux.
   */
  public RequestStreamToFluxAdaptor(BiConsumer<StreamT, Flux<StreamT>> onFirstCallback) {
    final Sinks.Many<StreamT> sink = Sinks.many().unicast().onBackpressureBuffer();
    requestStreamObserver = adaptRequestStreamToSink(sink, onFirstCallback);
  }

  // TODO(tmacam) perhaps this whole thing could be replaced by a single static method that receives the bi-func
  //              and returns the stream. Most of this code would stay as-is but exposed interface would be cleaner.


  public StreamObserver<StreamT> requestStreamObserver() {
    return requestStreamObserver;
  }

  /**
   * Creates a {@link StreamObserver} that forwards all its received events to a {@link Sinks}.
   *
   * @param sink the sink we will forward events (onNext, onError and onComplete) to.
   * @param onFirstCallback the callback we invoke upon receiving the very first request.
   *                       Its first argument is the very first request received and the second
   *                       the remaining requests as a flux.
   * @return the adaptor {@link StreamObserver} that forwards all its received events to sink.
   */
  private StreamObserver<StreamT> adaptRequestStreamToSink(final Sinks.Many<StreamT> sink,
                                                           final BiConsumer<StreamT, Flux<StreamT>> onFirstCallback) {
    return new StreamObserver<StreamT>() {
      @Override
      public void onNext(StreamT request) {
        if (isThisTheFirstItem) {
          isThisTheFirstItem = false; // We have seen the first item :)
          onFirstCallback.accept(request, sink.asFlux());
        } else {
          sink.emitNext(request, FAIL_FAST);
        }
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
