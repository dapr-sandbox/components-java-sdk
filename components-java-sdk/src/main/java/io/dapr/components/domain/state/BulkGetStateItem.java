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

import java.util.List;

/**
 * Represents a single item in a response for a {@link StateStore#bulkGet(List)}.
 *
 * <p>This is the SDK equivalent to {@link dapr.proto.components.v1.State.BulkStateItem}</p>
 *
 * @param key Key this bulk response item refers to.
 * @param response data returned for the requested key, wrapped in a {@link Mono#empty()} to
 *                 return a non-existing key.
 */
public record BulkGetStateItem(String key, Mono<GetResponse> response) {
}
