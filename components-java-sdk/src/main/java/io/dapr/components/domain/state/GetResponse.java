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
 * Response for a StateStore#get request.
 *
 * @param data The value of the GetRequest response.
 * @param etag The etag of the associated key.
 * @param metadata Metadata related to the response.
 * @param contentType The response value contenttype
 */
public record GetResponse(byte[] data, String etag, Map<String, String> metadata, String contentType) {

  /**
   * Constructor.
   *
   * @param data The value of the GetRequest response.
   * @param etag The etag of the associated key.
   * @param metadata Metadata related to the response.
   * @param contentType The response value contenttype
   */
  public GetResponse(byte[] data, String etag, Map<String, String> metadata, String contentType) {
    this.data = Objects.requireNonNull(data);
    this.etag = Objects.requireNonNull(etag);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Collections.unmodifiableMap(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
  }
}
