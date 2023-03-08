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

import java.util.Map;
import java.util.Objects;

/**
 * Represents the request to a {@link OutputBinding#invoke(InvokeRequest)} ()} RPC request.
 *
 * @param data The invoke request payload.
 * @param metadata The invoke request metadata.
 * @param operation The system supported operation.
 */
public record InvokeRequest(ByteString data, Map<String, String> metadata, String operation) {
  /**
   * Canonical constructor.
   *
   * @param data The invoke request payload.
   * @param metadata The invoke request metadata.
   * @param operation The system supported operation.
   */
  public InvokeRequest(ByteString data, Map<String, String> metadata, String operation) {
    this.data = Objects.requireNonNull(data);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.operation = Objects.requireNonNull(operation);
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a InvokeRequest.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static InvokeRequest fromProto(dapr.proto.components.v1.Bindings.InvokeRequest other) {
    return new InvokeRequest(other.getData(),
        other.getMetadataMap(),
        other.getOperation());
  }
}
