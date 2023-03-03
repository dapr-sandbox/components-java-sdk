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

import com.google.protobuf.ByteString;
import dapr.proto.components.v1.Bindings;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the response from a {@link OutputBinding#invoke(InvokeRequest)} RPC request.
 *
 * @param data The response payload.
 * @param metadata The response metadata.
 * @param contentType The response content-type.
 */
public record InvokeResponse(ByteString data, Map<String, String> metadata, String contentType) {
  /**
   * Canonical constructor.
   *
   * @param data The response payload.
   * @param metadata The response metadata.
   * @param contentType The response content-type.
   */
  public InvokeResponse(ByteString data, Map<String, String> metadata, String contentType) {
    this.data = Objects.requireNonNull(data);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
  }

  /**
   * Convert POJO to gRPC/Protocol Buffer object.
   *
   * @return an equivalent protocol buffer object.
   */
  public Bindings.InvokeResponse toProto() {
    return Bindings.InvokeResponse.newBuilder()
        .setData(this.data)
        .putAllMetadata(this.metadata)
        .setContentType(this.contentType)
        .build();
  }
}
