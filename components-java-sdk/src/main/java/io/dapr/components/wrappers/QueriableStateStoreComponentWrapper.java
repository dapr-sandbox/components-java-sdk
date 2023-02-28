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

import dapr.proto.components.v1.QueriableStateStoreGrpc;
import dapr.proto.components.v1.State;
import io.dapr.components.domain.state.QueriableStateStore;
import io.dapr.components.domain.state.QueryRequest;
import io.dapr.components.domain.state.QueryResponse;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;

public class QueriableStateStoreComponentWrapper extends QueriableStateStoreGrpc.QueriableStateStoreImplBase {

  private final QueriableStateStore queriableStateStore;

  public QueriableStateStoreComponentWrapper(QueriableStateStore queriableStateStore) {
    this.queriableStateStore = queriableStateStore;
  }

  @Override
  public void query(State.QueryRequest request, StreamObserver<State.QueryResponse> responseObserver) {
    Mono.just(request)
        .map(QueryRequest::fromProto)
        .flatMap(queriableStateStore::query)
        .map(QueryResponse::toProto)
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }
}
