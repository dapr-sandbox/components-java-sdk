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

import com.google.protobuf.ByteString;

import java.util.Map;
import java.util.Objects;

/**
 * Model for a stateStore.set request.
 *
 * @param key The key that should be set.
 * @param value Value is the desired content of the given key.
 * @param etag The etag is used as a If-Match header, to allow certain levels of consistency.
 * @param metadata The request metadata.
 * @param options The Set request options.
 * @param contentType The value contenttype.
 *
 * {@see TransactionableOperation}
 */
public record SetRequest(
    String key,
    ByteString value,
    String etag,
    Map<String, String> metadata,
    StateOptions options,
    String contentType) implements TransactionableOperation {
  /**
   * Canonical Constructor.
   *
   * @param key The key that should be set.
   * @param value Value is the desired content of the given key.
   * @param etag The etag is used as a If-Match header, to allow certain levels of consistency.
   * @param metadata The request metadata.
   * @param options The Set request options.
   * @param contentType The value contenttype.
   */
  public SetRequest(String key, ByteString value, String etag, Map<String, String> metadata, StateOptions options,
                    String contentType) {
    this.key = Objects.requireNonNull(key);
    this.value = Objects.requireNonNull(value);
    this.etag = Objects.requireNonNull(etag);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.options = Objects.requireNonNull(options);
    this.contentType = Objects.requireNonNull(contentType);
  }

  /**
   * Constructor.
   *
   * @param key The key that should be set.
   * @param value Value is the desired content of the given key.
   * @param etag The etag is used as a If-Match header, to allow certain levels of consistency.
   * @param metadata The request metadata.
   * @param options The Set request options.
   * @param contentType The value contenttype.
   */
  public SetRequest(String key, byte[] value, String etag, Map<String, String> metadata, StateOptions options,
                    String contentType) {
    this(Objects.requireNonNull(key),
        ByteString.copyFrom(Objects.requireNonNull(value)),
        Objects.requireNonNull(etag),
        // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
        Objects.requireNonNull(metadata),
        Objects.requireNonNull(options),
        Objects.requireNonNull(contentType));
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a SetRequest.
   */
  public SetRequest(dapr.proto.components.v1.State.SetRequest other) {
    this(other.getKey(),
        other.getValue(),
        other.getEtag().getValue(),
        other.getMetadataMap(),
        new StateOptions(other.getOptions()),
        other.getContentType());
  }
}