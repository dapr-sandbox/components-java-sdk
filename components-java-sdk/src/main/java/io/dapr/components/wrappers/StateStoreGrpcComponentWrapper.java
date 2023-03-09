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

import com.google.protobuf.ByteString;
import dapr.proto.components.v1.State;
import dapr.proto.components.v1.State.BulkDeleteRequest;
import dapr.proto.components.v1.State.BulkGetRequest;
import dapr.proto.components.v1.State.BulkGetResponse;
import dapr.proto.components.v1.State.BulkSetRequest;
import dapr.proto.components.v1.State.BulkStateItem;
import dapr.proto.components.v1.State.DeleteRequest;
import dapr.proto.components.v1.State.Etag;
import dapr.proto.components.v1.State.GetRequest;
import dapr.proto.components.v1.StateStoreGrpc;
import io.dapr.components.domain.state.BulkGetError;
import io.dapr.components.domain.state.GetResponse;
import io.dapr.components.domain.state.SetRequest;
import io.dapr.components.domain.state.StateStore;
import io.dapr.v1.ComponentProtos;
import io.dapr.v1.ComponentProtos.FeaturesResponse;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A translation layer between a (local) StateStore implementation and Dapr's gRPC StateStore model.
 */
public class StateStoreGrpcComponentWrapper extends StateStoreGrpc.StateStoreImplBase {
  private static final Etag EMPTY_ETAG = Etag.newBuilder().setValue("").build();
  private static final State.GetResponse EMPTY_GET_RESPONSE = State.GetResponse.newBuilder()
      .setData(ByteString.EMPTY)
      .setEtag(EMPTY_ETAG)
      .build();

  private final StateStore stateStore;

  /**
   * Constructor.
   *
   * @param stateStore the state store that this component will expose as a service.
   */
  public StateStoreGrpcComponentWrapper(final StateStore stateStore) {
    this.stateStore = Objects.requireNonNull(stateStore);
  }

  @Override
  public void init(final State.InitRequest request, final StreamObserver<State.InitResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> stateStore.init(req.getMetadata().getPropertiesMap()))
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(State.InitResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }


  @Override
  public void features(final ComponentProtos.FeaturesRequest request,
                       final StreamObserver<FeaturesResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> stateStore.getFeatures())
        .map(features -> FeaturesResponse.newBuilder()
            .addAllFeatures(features)
            .build()
        )
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void ping(final ComponentProtos.PingRequest request,
                   final StreamObserver<ComponentProtos.PingResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> stateStore.ping())
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(ComponentProtos.PingResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void delete(final DeleteRequest request, final StreamObserver<State.DeleteResponse> responseObserver) {
    Mono.just(request)
        .map(io.dapr.components.domain.state.DeleteRequest::fromProto) //Convert to local domain
        .flatMap(stateStore::delete)
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(State.DeleteResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void bulkDelete(final BulkDeleteRequest request,
                         final StreamObserver<State.BulkDeleteResponse> responseObserver) {
    Mono.just(request)
        // Convert to local domain/model
        .flatMapIterable(BulkDeleteRequest::getItemsList)
        .map(io.dapr.components.domain.state.DeleteRequest::fromProto)
        .collectList()
        // Perform the bulk operation
        .flatMap(this.stateStore::bulkDelete)
        .thenReturn(State.BulkDeleteResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void get(final GetRequest request, final StreamObserver<State.GetResponse> responseObserver) {
    Mono.just(request)
        .map(io.dapr.components.domain.state.GetRequest::fromProto) // Convert to local domain/model
        .flatMap(stateStore::get)
        // If value is present, map it to an appropriate GetResponse object
        .map(GetResponse::toProto)
        // otherwise return an empty response
        .defaultIfEmpty(EMPTY_GET_RESPONSE)
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void bulkGet(final BulkGetRequest request,
                      final StreamObserver<BulkGetResponse> responseObserver) {
    Mono.just(request)
        // Convert to local domain/model
        .flatMapIterable(BulkGetRequest::getItemsList)
        .map(io.dapr.components.domain.state.GetRequest::fromProto)
        .collectList()
        // Perform the bulk operation
        .flatMapMany(this.stateStore::bulkGet)
        // Let's convert all requested items into BulkStateItems objects.
        .flatMap(requestedItem -> requestedItem.response()
            // If value is present, convert it to an appropriate BulkStateItem object
            .map(value -> BulkStateItem.newBuilder()
                .setKey(requestedItem.key())
                .setData(value.data())
                .setEtag(Etag.newBuilder()
                    .setValue(value.etag())
                    .build())
                .setError(BulkGetError.NONE)
                .build()
            )
            // otherwise return an empty BulkStateItem with corresponding error codes
            .defaultIfEmpty(
                BulkStateItem.newBuilder()
                    .setKey(requestedItem.key())
                    .setData(ByteString.EMPTY)
                    .setEtag(EMPTY_ETAG)
                    .setError(BulkGetError.KEY_DOES_NOT_EXIST)
                    .build()
            )
        )
        // Wrap them into a BulkGetResponse and send it away
        .collectList()
        .map(items -> BulkGetResponse.newBuilder()
            .addAllItems(items)
            .build())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void set(final State.SetRequest request, final StreamObserver<State.SetResponse> responseObserver) {
    Mono.just(request)
        .map(SetRequest::fromProto)  // Convert to local domain/model
        .flatMap(stateStore::set)
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(State.SetResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void bulkSet(final BulkSetRequest request, final StreamObserver<State.BulkSetResponse> responseObserver) {
    Mono.just(request)
        // Convert to local domain/model
        .flatMapIterable(BulkSetRequest::getItemsList)
        .map(SetRequest::fromProto)
        .collectList()
        // Perform the bulk operation
        .flatMap(this.stateStore::bulkSet)
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(State.BulkSetResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }
}
