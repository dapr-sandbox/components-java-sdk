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

public enum Order {
  ASC,
  DESC,
  UNRECOGNIZED;

  /**
   * Get the gRPC equivalent for this enum.
   *
   * @return The gRPC equivalent.
   */
  public State.Sorting.Order toProto() {
    return toProto(this);
  }

  /**
   * Get the gRPC equivalent of an enum.
   * @param sdkEnum the SDK enum to convert.
   * @return The gRPC equivalent
   */
  public static State.Sorting.Order toProto(Order sdkEnum) {
    return switch (sdkEnum) {
      case ASC -> State.Sorting.Order.ASC;
      case DESC -> State.Sorting.Order.DESC;
      case UNRECOGNIZED -> State.Sorting.Order.UNRECOGNIZED;
    };
  }

  /**
   * Convert a gRPC Enum to the SDK's equivalent.
   * @param value the gRPC enum
   * @return the equivalent SDK enum.
   */
  public static Order fromProto(State.Sorting.Order value) {
    return switch (value) {
      case ASC -> Order.ASC;
      case DESC -> Order.DESC;
      case UNRECOGNIZED -> Order.UNRECOGNIZED;
    };
  }
}
