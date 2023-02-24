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
 * Represents a request to publish data to a given PubSub and topicName.
 *
 * @param data The data to be published.
 * @param pubSubName The pubsub name.
 * @param topic The publishing topicName.
 * @param metadata Message metadata.
 * @param contentType The data content type.
 */
public record PublishRequest(ByteString data, String pubSubName, String topic, Map<String, String> metadata,
                             String contentType) {
  /**
   * Canonical constructor.
   *
   * @param data The data to be published.
   * @param pubSubName The pubsub name.
   * @param topic The publishing topicName.
   * @param metadata Message metadata.
   * @param contentType The data content type.
   */
  public PublishRequest(ByteString data, String pubSubName, String topic, Map<String, String> metadata,
                        String contentType) {
    this.data = Objects.requireNonNull(data);
    this.pubSubName = Objects.requireNonNull(pubSubName);
    this.topic = Objects.requireNonNull(topic);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
    this.contentType = Objects.requireNonNull(contentType);
  }

  /**
   * Converts from protocol buffer types to local domain types.
   *
   * @param other The Protocol Buffer representation of a PublishRequest.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static PublishRequest fromProto(dapr.proto.components.v1.Pubsub.PublishRequest other) {
    return new PublishRequest(other.getData(),
        other.getPubsubName(),
        other.getTopic(),
        other.getMetadataMap(),
        other.getContentType());
  }
}
