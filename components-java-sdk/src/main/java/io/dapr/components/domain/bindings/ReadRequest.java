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

import java.util.Optional;

/**
 * Represents a read request.
 *
 * @param responseData The handle response.
 * @param messageId message_id he unique message ID.
 * @param responseErrorMessage Optional, should not be fulfilled when the message was successfully handled.
 */
public record ReadRequest(ByteString responseData, String messageId, Optional<String> responseErrorMessage) {
  /**
   * Conversion method.
   *
   * @param other The Protocol Buffer representation of a ReadRequest.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static ReadRequest fromProto(dapr.proto.components.v1.Bindings.ReadRequest other) {
    return new ReadRequest(other.getResponseData(),
        other.getMessageId(),
        other.hasResponseError()
            ? Optional.of(other.getResponseError().getMessage())
            : Optional.empty());
  }
}
