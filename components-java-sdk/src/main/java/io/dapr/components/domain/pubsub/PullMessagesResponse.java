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

package io.dapr.components.domain.pubsub;

import com.google.protobuf.ByteString;

import java.util.Map;
import java.util.Objects;

/**
 * PullMessagesResponse.
 *
 * @param data The message content.
 * @param topicName The topicName where the message come from.
 * @param metadata Message metadata.
 * @param contentType The data content type.
 * @param id The message {transient} ID. Its used for ack'ing it later.
 */
public record PullMessagesResponse(ByteString data, String topicName, Map<String, String> metadata, String contentType,
                                   String id) {
  /**
   * Canonical constructor.
   *
   * @param data The message content.
   * @param topicName The topicName where the message come from.
   * @param metadata Message metadata.
   * @param contentType The data content type.
   * @param id The message {transient} ID. Its used for ack'ing it later.
   */
  public PullMessagesResponse(ByteString data, String topicName, Map<String, String> metadata, String contentType,
                              String id) {
    this.data = Objects.requireNonNull(data);
    this.topicName = Objects.requireNonNull(topicName);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
    this.id = Objects.requireNonNull(id);
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a PublishRequest.
   */
  public PullMessagesResponse(dapr.proto.components.v1.Pubsub.PullMessagesResponse other) {
    this(other.getData(),
        other.getTopicName(),
        other.getMetadataMap(),
        other.getContentType(),
        other.getId());
  }
}
