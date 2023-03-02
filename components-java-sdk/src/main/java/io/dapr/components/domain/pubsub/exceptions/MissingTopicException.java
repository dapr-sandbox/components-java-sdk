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

package io.dapr.components.domain.pubsub.exceptions;

import io.dapr.components.domain.pubsub.Topic;

/**
 * Signals that an error was found while trying to parse a
 * {@link dapr.proto.components.v1.Pubsub.PullMessagesRequest} into
 * a {@link Topic} because it came with a topic field missing.
 */
public class MissingTopicException extends IllegalArgumentException {
}
