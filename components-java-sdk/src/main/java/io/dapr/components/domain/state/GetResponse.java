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
import dapr.proto.components.v1.State;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.dapr.components.domain.state.Constants.DEFAULT_CONTENT_TYPE;

/**
 * Response for a StateStore#get request.
 *
 * @param data The value of the GetRequest response.
 * @param etag The etag of the associated key.
 * @param metadata Metadata related to the response.
 * @param contentType The response value contenttype
 */
public record GetResponse(ByteString data, String etag, Map<String, String> metadata, String contentType) {

  /**
   * Canonical Constructor.
   *
   * @param data The value of the GetRequest response.
   * @param etag The etag of the associated key.
   * @param metadata Metadata related to the response.
   * @param contentType The response value contenttype
   */
  public GetResponse(final ByteString data, final String etag, final Map<String, String> metadata,
                     final String contentType) {
    // Java lacks a solution to "immutable byte arrays", so we wrap them up in a ByteString.
    // FindBugs isn't really happy with this setup so we exclude EI_EXPOSE_REP* errors for this class
    this.data = Objects.requireNonNull(data);
    this.etag = Objects.requireNonNull(etag);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
  }

  /**
   * Constructor.
   *
   * @param data The value of the GetRequest response.
   * @param etag The etag of the associated key.
   * @param metadata Metadata related to the response.
   * @param contentType The response value contenttype
   */
  public GetResponse(final byte[] data, final String etag, final Map<String, String> metadata,
                     final String contentType) {
    this(ByteString.copyFrom(Objects.requireNonNull(data)),
        Objects.requireNonNull(etag),
        Objects.requireNonNull(metadata),
        Objects.requireNonNull(contentType));
  }

  /**
   * Constructor with no metadata and using DEFAULT_CONTENT_TYPE for content type.
   *
   * @param data The value of the GetRequest response.
   * @param etag The etag of the associated key.
   */
  public GetResponse(final byte[] data, final String etag) {
    this(data, etag, Collections.emptyMap(), DEFAULT_CONTENT_TYPE);
  }

  /**
   * Conversion method.
   *
   * @return The provided object converted to its protocol buffer equivalent.
   */
  public State.GetResponse toProto() {
    return State.GetResponse.newBuilder()
        .setData(data)
        .setEtag(State.Etag.newBuilder()
            .setValue(etag)
            .build())
        .putAllMetadata(metadata)
        .setContentType(contentType)
        .build();
  }

  /**
   * Conversion method.
   *
   * @param requestedItemKey The key that associated with this item in the BulkGetRequest
   *
   * @return The provided object converted to a State.BulkStateItem protocol buffer equivalent.
   */
  public State.BulkStateItem toBulkGetItemProto(final String requestedItemKey) {
    return State.BulkStateItem.newBuilder()
        // bulkGet specific fields
        .setKey(requestedItemKey)
        .setError(BulkGetError.NONE)
        // Fields shared with GetResponse
        .setData(data)
        .setEtag(State.Etag.newBuilder()
            .setValue(etag)
            .build())
        .putAllMetadata(metadata)
        .setContentType(contentType)
        .build();
  }
}
