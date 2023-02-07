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

import dapr.proto.components.v1.State;
import reactor.core.publisher.Mono;

/**
 * QueriableStateStore service provides a gRPC interface for querier state store
 * components. It was designed to embed query features to the StateStore Service
 * as a complementary service.
 */
public interface QueriableStateStore  {
  /**
   * Query performs a query request on the statestore..
   *
   * @param request the query.
   * @return A reponse encapsulated in a QueryReponse object.
   */
  Mono<QueryResponse> query(QueryRequest request);
}
