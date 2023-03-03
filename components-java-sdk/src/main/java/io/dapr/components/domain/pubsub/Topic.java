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

import dapr.proto.components.v1.Pubsub;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;

/**
 * Topic.
 *
 * @param name The name of the topic.
 * @param metadata Metadata associated with this topic.
 */
public record Topic(String name, Map<String, String> metadata) {
  /**
   * Canonical Constructor.
   *
   * @param name The name of the topic.
   * @param metadata Metadata associated with this topic.
   */
  public Topic(String name, Map<String, String> metadata) {
    this.name = Objects.requireNonNull(name);
    // All this constructor just so we can make this Map unmodifiable and this class immutable ;)
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a Topic.
   */
  public Topic(Pubsub.Topic other) {
    this(other.getName(),
        other.getMetadataMap());
  }

  /**
   * Conversion method.
   *
   * @param other The  {@link dapr.proto.components.v1.Pubsub.PullMessagesRequest} from where
   *              we will extract only its topic field. See {@link PubSub#pullMessages(Topic, Flux)}
   *              for details.
   * @return the topic field from the {@link dapr.proto.components.v1.Pubsub.PullMessagesRequest}
   * @throws MissingTopicException if the {@link dapr.proto.components.v1.Pubsub.PullMessagesRequest} does not
   *         have a topic set.
   */
  public static Topic fromProto(dapr.proto.components.v1.Pubsub.PullMessagesRequest other) {
    if (!other.hasTopic()) {
      throw new MissingTopicException();
    }

    return new Topic(other.getTopic());
  }
}
