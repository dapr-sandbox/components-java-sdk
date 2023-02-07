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

package io.dapr.components.domain.state;

import reactor.core.publisher.Mono;

/**
 * TransactionalStateStore service provides a gRPC interface for transactional
 * state store components. It was designed to embed transactional features to
 * the StateStore Service as a complementary service.
 */
public interface TransactionalStateStore {
  /**
   * Transact executes multiples operation in a transactional environment.
   *
   * @param request The transactional request.
   * @return A Mono representing the success (or failure) of the operation.
   */
  Mono<Void> transact(TransactionalStateRequest request);
}
