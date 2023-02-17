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

package io.dapr.components.domain.bindings;

import io.dapr.components.aspects.InitializableWithProperties;
import io.dapr.components.aspects.Pingable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for output bindings.
 */
public interface OutputBinding extends InitializableWithProperties, Pingable {
  /**
   * Invoke remote systems with optional payloads.
   *
   * @param request The request to send to this output binding component.
   *
   * @return Mono holding a {@link InvokeResponse} with the result of this invoke call.
   */
  Mono<InvokeResponse> invoke(InvokeRequest request);

  /**
   * ListOperations list system supported operations.
   *
   * @return A Mono with the List of operations suported by this output binding component.
   */
  Mono<ListOperationsResponse> listOperations();
}
