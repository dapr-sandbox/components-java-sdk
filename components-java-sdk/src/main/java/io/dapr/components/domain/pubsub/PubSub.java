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

import io.dapr.components.aspects.AdvertisesFeatures;
import io.dapr.components.aspects.InitializableWithProperties;
import io.dapr.components.aspects.Pingable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents a PubSub components.
 */
public interface PubSub extends InitializableWithProperties, AdvertisesFeatures, Pingable {

  /** Publish publishes a new message for the given topicName.
   *
   * @param request A request to publish something to a PubSub.
   * @return An empty Mono representing success or error.
   */
  Mono<Void> publish(PublishRequest request);

  /**
   * Establishes a stream with the server (PubSub component), which sends
   * messages down to the client (daprd). The client streams acknowledgements
   * back to the server. The server will close the stream and return the status
   * on any error. In case of closed connection, the client should re-establish
   * the stream. The first message MUST contain a `topicName` attribute on it that
   * should be used for the entire streaming pull.
   *
   * @param topic A Topic object whose {@code name} attribute should be used for
   *               the entire streaming pull.
   * @param acks All remaining  messages sent to this flux, which will be used to
   *                acknowledge received messages.
   * @return A Flux with all the acknowledgements we are sending to a server.
   */
  Flux<PullMessagesResponse> pullMessages(Topic topic, Flux<PullMessageAcknowledgement> acks);

}
