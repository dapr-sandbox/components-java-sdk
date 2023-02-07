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

/**
 * QueryItem is an object representing a single entry in query results.
 *
 * @param key The returned item Key.
 * @param data The returned item Data.
 * @param etag The returned item ETag.
 * @param error The returned error string.
 * @param contentType  The returned content-type.
 */
public record QueryResponseItem(String key, ByteString data, String etag, String error, String contentType) {
  /**
   * Alternative constructor.
   *
   * @param key The returned item Key.
   * @param data The returned item Data.
   * @param etag The returned item ETag.
   * @param error The returned error string.
   * @param contentType  The returned content-type.
   */
  public QueryResponseItem(String key, final byte[] data, String etag, String error, String contentType) {
    this(key, ByteString.copyFrom(data), etag, error, contentType);
  }
}
