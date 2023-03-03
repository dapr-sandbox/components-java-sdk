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

import java.util.Optional;

/**
 * Just the acknowledgement related fields from a  {@link dapr.proto.components.v1.Pubsub.PullMessagesRequest}.
 *
 * <p>This class doesn't exist in the wire-format and it is meant to make easier to create PubSub
 * components.</p>
 *
 * @param ackMessageId The unique message ID.
 * @param ackErrorMessage Optional, should not be fulfilled when the message was successfully handled.
 */
public record PullMessageAcknowledgement(String ackMessageId, Optional<String> ackErrorMessage) {
  /**
   * Conversion constructor.
   *
   * <p>We ensure that the {@code topic} field is not set during this conversion.</p>
   *
   * @param other The Protocol Buffer representation of a PullMessageAcknowledgement.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static PullMessageAcknowledgement fromProto(dapr.proto.components.v1.Pubsub.PullMessagesRequest other) {
    if (other.hasTopic()) {
      throw new InvalidAcknowledgementMessageException();
    }
    return new PullMessageAcknowledgement(
        other.getAckMessageId(),
        other.hasAckError()
            ? Optional.of(other.getAckError().getMessage())
            : Optional.empty());
  }
}
