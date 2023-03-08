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

import dapr.proto.components.v1.State;
import dapr.proto.components.v1.TransactionalStateStoreGrpc;
import io.dapr.components.domain.state.TransactionalStateRequest;
import io.dapr.components.domain.state.TransactionalStateStore;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Mono;

public class TransactionalStateStoreComponentWrapper
    extends TransactionalStateStoreGrpc.TransactionalStateStoreImplBase {

  private final TransactionalStateStore transactionalStateStore;

  public TransactionalStateStoreComponentWrapper(TransactionalStateStore transactionalStateStore) {
    this.transactionalStateStore = transactionalStateStore;
  }

  @Override
  public void transact(State.TransactionalStateRequest request,
                       StreamObserver<State.TransactionalStateResponse> responseObserver) {
    Mono.just(request)
        .map(TransactionalStateRequest::fromProto)
        .flatMap(transactionalStateStore::transact)
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .thenReturn(State.TransactionalStateResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }
}
