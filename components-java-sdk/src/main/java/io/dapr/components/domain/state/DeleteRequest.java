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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Delete request.
 *
 * @param key The key that should be deleted.
 * @param etag The etag is used as a If-Match header, to allow certain levels of consistency.
 * @param metadata The request metadata.
 * @param options Consistency and concurrency options.
 *
 * {@see TransactionableOperation}
 */
public record DeleteRequest(
    String key,
    String etag,
    Map<String, String> metadata,
    StateOptions options) implements TransactionableOperation {

  /**
   * Constructor.
   *
   * @param key The key that should be deleted.
   * @param etag The etag is used as a If-Match header, to allow certain levels of consistency.
   * @param metadata The request metadata.
   * @param options Consistency and concurrency options.
   */
  public DeleteRequest(String key, String etag, Map<String, String> metadata, StateOptions options) {
    this.key = Objects.requireNonNull(key);
    this.etag = Objects.requireNonNull(etag);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.options = Objects.requireNonNull(options);
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a SetRequest.
   */
  public DeleteRequest(dapr.proto.components.v1.State.DeleteRequest other) {
    this(other.getKey(),
        other.getEtag().getValue(),
        other.getMetadataMap(),
        new StateOptions(other.getOptions()));
  }
}