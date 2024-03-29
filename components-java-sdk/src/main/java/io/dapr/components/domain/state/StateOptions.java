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

package io.dapr.components.domain.state;

import dapr.proto.components.v1.State;
import io.dapr.components.domain.state.options.StateConcurrency;
import io.dapr.components.domain.state.options.StateConsistency;

import java.util.Objects;

public record StateOptions(StateConcurrency concurrency, StateConsistency consistency) {

  public StateOptions {
    Objects.requireNonNull(concurrency);
    Objects.requireNonNull(consistency);
  }

  public StateOptions(State.StateOptions other) {
    this(StateConcurrency.fromProto(other.getConcurrency()),
        StateConsistency.fromProto(other.getConsistency()));
  }
}
