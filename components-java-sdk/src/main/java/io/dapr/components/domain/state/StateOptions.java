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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public record StateOptions(StateConcurrency concurrency, StateConsistency consistency) {

  public StateOptions {
    Objects.requireNonNull(concurrency);
    Objects.requireNonNull(consistency);
  }

  public StateOptions(State.StateOptions other) {
    this(StateConcurrency.of(other.getConcurrency()), StateConsistency.of(other.getConsistency()));
  }

  /**
   * Enum describing the supported concurrency for state.
   */
  public enum StateConcurrency {
    UNSPECIFIED(State.StateOptions.StateConcurrency.CONCURRENCY_UNSPECIFIED),

    FIRST_WRITE(State.StateOptions.StateConcurrency.CONCURRENCY_FIRST_WRITE),

    LAST_WRITE(State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE);

    // The gRPC equivalent for this enum
    private final State.StateOptions.StateConcurrency equivalent;

    // Convert Protocol Buffer model to this SDK internal Model
    static final Map<State.StateOptions.StateConcurrency, StateConcurrency> toSdkModel =
        Arrays.stream(StateConcurrency.values())
            .collect(Collectors.toMap(StateConcurrency::getValue, Function.identity()));


    StateConcurrency(State.StateOptions.StateConcurrency value) {
      this.equivalent = value;
    }

    public State.StateOptions.StateConcurrency getValue() {
      return this.equivalent;
    }

    public static StateConcurrency of(State.StateOptions.StateConcurrency value) {
      return toSdkModel.get(value);
    }
  }

  /**
   * Enum describing the supported consistency for state.
   */
  enum StateConsistency {
    UNSPECIFIED(State.StateOptions.StateConsistency.CONSISTENCY_UNSPECIFIED),
    EVENTUAL(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL),
    STRONG(State.StateOptions.StateConsistency.CONSISTENCY_STRONG);

    private final State.StateOptions.StateConsistency equivalent;

    // Convert Protocol Buffer model to this SDK internal Model
    static final Map<State.StateOptions.StateConsistency, StateConsistency> toSdkModel =
        Arrays.stream(StateConsistency.values())
            .collect(Collectors.toMap(StateConsistency::getValue, Function.identity()));

    StateConsistency(State.StateOptions.StateConsistency value) {
      this.equivalent = value;
    }

    public State.StateOptions.StateConsistency getValue() {
      return this.equivalent;
    }

    public static StateConsistency of(State.StateOptions.StateConsistency value) {
      return toSdkModel.get(value);
    }
  }
}
