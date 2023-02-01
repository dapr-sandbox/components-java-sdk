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

import io.dapr.components.aspects.AdvertisesFeatures;
import io.dapr.components.aspects.InitializableWithProperties;
import io.dapr.components.aspects.Pingable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Represents a state store.
 *
 * <p>Default implementations are provided for bulk operations. They relly on their
 * single-item variants and can be made more efficient if your underlying state store
 * provides native support for bulk operations.</p>
 */
public interface StateStore extends InitializableWithProperties, AdvertisesFeatures, Pingable {
  /**
   * Get data from the given key.
   *
   * @param getRequest Request specifying what to retrieve.
   * @return A {@link GetResponse} Mono containing the contents associated with the key or
   *         representing an error.
   */
  Mono<GetResponse> get(GetRequest getRequest);

  /**
   * Deletes the specified key from the state store.
   * @param deleteRequest Request specifying what to delete.
   * @return An empty Mono representing success or error.
   */
  Mono<Void> delete(DeleteRequest deleteRequest);

  /**
   * Sets the value of the specified key.
   *
   * @param setRequest Request specifying what key to set and its contents.
   * @return An empty Mono representing success or error.
   */
  Mono<Void> set(SetRequest setRequest);

  /**
   * Retrieves many keys at once.
   *
   * @param getRequests list of GetRequest to be performed in bulk.
   * @return A Flux of GetResponses, one for each requested key.
   */
  default Flux<BulkGetStateItem> bulkGet(List<GetRequest> getRequests) {
    return Flux.fromIterable(getRequests)
        .map(item -> new BulkGetStateItem(item.key(), this.get(item)));
  }

  /**
   * Deletes many keys at once.
   *
   * @param deleteRequests list of DeleteRequest to be performed in bulk.
   *
   * @return A Mono representing success of failure of the collective operation.
   */
  default Mono<Void> bulkDelete(List<DeleteRequest> deleteRequests) {
    return Flux.fromIterable(deleteRequests)
        .flatMap(this::delete)
        .then(); // convert this Flux to a Mono
  }

  /**
   * Set the value of many keys at once.
   *
   * @param setRequests list of SetRequests to be performed in bulk.
   *
   * @return A Mono representing success of failure of the collective operation.
   */
  default Mono<Void> bulkSet(List<SetRequest> setRequests) {
    return Flux.fromIterable(setRequests)
        .flatMap(this::set)
        .then(); // convert this Flux to a Mono
  }
}
