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

import dapr.proto.components.v1.Bindings;
import dapr.proto.components.v1.InputBindingGrpc;
import io.dapr.components.domain.bindings.InputBinding;
import io.dapr.components.domain.bindings.ReadRequest;
import io.dapr.components.domain.bindings.ReadResponse;
import io.dapr.v1.ComponentProtos;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

public class InputBindingGrpcComponentWrapper extends InputBindingGrpc.InputBindingImplBase {

  private final InputBinding inputBinding;

  private final Scheduler scheduler = Schedulers.boundedElastic();

  public InputBindingGrpcComponentWrapper(InputBinding inputBinding) {
    this.inputBinding = Objects.requireNonNull(inputBinding);
  }

  @Override
  public void init(Bindings.InputBindingInitRequest request,
                   StreamObserver<Bindings.InputBindingInitResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> inputBinding.init(req.getMetadata().getPropertiesMap()))
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(Bindings.InputBindingInitResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void ping(ComponentProtos.PingRequest request, StreamObserver<ComponentProtos.PingResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> inputBinding.ping())
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(ComponentProtos.PingResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }


  @Override
  public StreamObserver<Bindings.ReadRequest> read(StreamObserver<Bindings.ReadResponse> responseObserver) {
    // First, convert the input requests to first request and acknowledgments Flux,
    // so we can feed it to our component
    final RequestStreamToFluxAdaptor<Bindings.ReadRequest> requestAdaptor = new RequestStreamToFluxAdaptor<>();
    // Alright... what to do when we start receiving requests?
    // First, let's convert those requests to the local domain.
    final Flux<ReadRequest> acksFlux = requestAdaptor.flux().map(ReadRequest::fromProto);

    // Wrap everything in a Flux. This will keep uniformity with other handlers and will allow for
    // delegating multithreading processing of requests to another thread by means of subscribeOn(scheduler)
    Flux.just(acksFlux)
        // Push these requests to the component.
        .flatMap(inputBinding::read)
        // Move processing to a different thread -- otherwise we would get stuck in the line above
        // and this method would not return. See
        // https://projectreactor.io/docs/core/release/reference/#producing.create
        .subscribeOn(scheduler, false)
        // Connect its response flux to the output stream from this RPC
        .map(ReadResponse::toProto)
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);

    // Finally, return the StreamObserver
    return requestAdaptor.requestStreamObserver();
  }
}
