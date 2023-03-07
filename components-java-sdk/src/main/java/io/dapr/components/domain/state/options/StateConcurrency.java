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

package io.dapr.components.domain.state.options;

import dapr.proto.components.v1.State;

/**
 * Enum describing the supported concurrency for state.
 */
public enum StateConcurrency {
  UNSPECIFIED,
  FIRST_WRITE,
  LAST_WRITE,
  UNRECOGNIZED;

  /**
   * Get the gRPC equivalent for this enum.
   *
   * @return The gRPC equivalent.
   */
  public State.StateOptions.StateConcurrency toProto() {
    return toProto(this);
  }

  /**
   * Get the gRPC equivalent of an enum.
   * @param sdkEnum the SDK enum to convert.
   * @return The gRPC equivalent
   */
  public static State.StateOptions.StateConcurrency toProto(StateConcurrency sdkEnum) {
    return switch (sdkEnum) {
      case UNSPECIFIED -> State.StateOptions.StateConcurrency.CONCURRENCY_UNSPECIFIED;
      case FIRST_WRITE -> State.StateOptions.StateConcurrency.CONCURRENCY_FIRST_WRITE;
      case LAST_WRITE -> State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE;
      case UNRECOGNIZED -> State.StateOptions.StateConcurrency.UNRECOGNIZED;
    };
  }

  /**
   * Convert a gRPC Enum to the SDK's equivalent.
   * @param value the gRPC enum
   * @return the equivalent SDK enum.
   */
  public static StateConcurrency fromProto(State.StateOptions.StateConcurrency value) {
    return switch (value) {
      case CONCURRENCY_UNSPECIFIED -> UNSPECIFIED;
      case CONCURRENCY_FIRST_WRITE -> FIRST_WRITE;
      case CONCURRENCY_LAST_WRITE -> LAST_WRITE;
      case UNRECOGNIZED -> UNRECOGNIZED;
    };
  }
}
