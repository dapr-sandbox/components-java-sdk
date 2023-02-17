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
 * ReadResponse message.
 *
 * @param data The Read binding Data.
 * @param metadata  The message metadata.
 * @param contentType The message content type.
 * @param messageId The {transient} message ID used for ACK-ing it later.
 */
public record ReadResponse(ByteString data, Map<String, String> metadata, String contentType,
                           String messageId) {
  /**
   * Canonical Constructor.
   *
   * @param data The Read binding Data.
   * @param metadata  The message metadata.
   * @param contentType The message content type.
   * @param messageId The {transient} message ID used for ACK-ing it later.
   */
  public ReadResponse(ByteString data, Map<String, String> metadata, String contentType, String messageId) {
    this.data = Objects.requireNonNull(data);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
    this.messageId = Objects.requireNonNull(messageId);
  }
}
