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

/**
 * Interface for input bindings.
 */
public interface InputBinding extends InitializableWithProperties, Pingable {
  /**
   * Establishes a stream with the server, which sends messages down to the
   * client. The client streams acknowledgements back to the server. The server
   * will close the stream and return the status on any error. In case of closed
   * connection, the client should re-establish the stream.
   *
   * @param first The initial request object.
   * @param acks A flux representing stream of acknowledgements sent back by the client
   *            to the component.
   * @return A Flux containing the responses returned by the component.
   */
  Flux<ReadResponse> read(ReadRequest first, Flux<ReadRequest> acks);
}
