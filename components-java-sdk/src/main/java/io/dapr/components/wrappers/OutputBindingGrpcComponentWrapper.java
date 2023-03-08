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
import dapr.proto.components.v1.OutputBindingGrpc;
import io.dapr.components.domain.bindings.InvokeRequest;
import io.dapr.components.domain.bindings.InvokeResponse;
import io.dapr.components.domain.bindings.OutputBinding;
import io.dapr.v1.ComponentProtos;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class OutputBindingGrpcComponentWrapper extends OutputBindingGrpc.OutputBindingImplBase {

  private final OutputBinding outputBinding;

  public OutputBindingGrpcComponentWrapper(OutputBinding outputBinding) {
    this.outputBinding = Objects.requireNonNull(outputBinding);
  }

  @Override
  public void init(Bindings.OutputBindingInitRequest request,
                   StreamObserver<Bindings.OutputBindingInitResponse> responseObserver) {
    final Bindings.OutputBindingInitResponse response = Mono.just(request)
        .flatMap(req -> outputBinding.init(req.getMetadata().getPropertiesMap()))
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .map(success -> Bindings.OutputBindingInitResponse.getDefaultInstance())
        .block();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void ping(ComponentProtos.PingRequest request, StreamObserver<ComponentProtos.PingResponse> responseObserver) {
    final ComponentProtos.PingResponse response = Mono.just(request)
        .flatMap(req -> outputBinding.ping())
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .map(successfulPing -> ComponentProtos.PingResponse.getDefaultInstance())
        .block();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void invoke(Bindings.InvokeRequest request, StreamObserver<Bindings.InvokeResponse> responseObserver) {
    final Bindings.InvokeResponse response = Mono.just(request)
        .map(InvokeRequest::new) // Convert to local domain/model
        .flatMap(outputBinding::invoke)
        .map(InvokeResponse::toProto) // convert back to gRPC model
        .block();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void listOperations(Bindings.ListOperationsRequest request,
                             StreamObserver<Bindings.ListOperationsResponse> responseObserver) {
    final Bindings.ListOperationsResponse response = Mono.just(request)
        // ListOperations is an empty message, just like PingRequest. Nothing to read from it.
        .flatMap(req -> outputBinding.listOperations())
        .map(operations -> Bindings.ListOperationsResponse.newBuilder()
            .addAllOperations(operations)
            .build())
        .block();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
