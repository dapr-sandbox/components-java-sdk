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
 * Enum describing the supported consistency for state.
 */
public enum StateConsistency {
  UNSPECIFIED,
  EVENTUAL,
  STRONG,
  UNRECOGNIZED;

  // Convert Protocol Buffer model to this SDK internal Model
  /**
   * Get the gRPC equivalent for this enum.
   *
   * @return The gRPC equivalent.
   */
  public State.StateOptions.StateConsistency toProto() {
    return toProto(this);
  }

  /**
   * Get the gRPC equivalent of an enum.
   * @param sdkEnum the SDK enum to convert.
   * @return The gRPC equivalent
   */
  public static State.StateOptions.StateConsistency toProto(StateConsistency sdkEnum) {
    return switch (sdkEnum) {
      case UNSPECIFIED -> State.StateOptions.StateConsistency.CONSISTENCY_UNSPECIFIED;
      case EVENTUAL -> State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL;
      case STRONG -> State.StateOptions.StateConsistency.CONSISTENCY_STRONG;
      case UNRECOGNIZED -> State.StateOptions.StateConsistency.UNRECOGNIZED;
    };
  }

  /**
   * Convert a gRPC Enum to the SDK's equivalent.
   * @param value the gRPC enum
   * @return the equivalent SDK enum.
   */
  public static StateConsistency fromProto(State.StateOptions.StateConsistency value) {
    return switch (value) {
      case CONSISTENCY_UNSPECIFIED -> UNSPECIFIED;
      case CONSISTENCY_EVENTUAL -> EVENTUAL;
      case CONSISTENCY_STRONG -> STRONG;
      case UNRECOGNIZED -> UNRECOGNIZED;
    };
  }
}
