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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Describes a sorting order to be performed by a Query.
 *
 * @param key The key that should be used for sorting.
 * @param order The order that should be used.
 */
public record Sorting(String key, Order order) {

  enum Order {
    ASC(dapr.proto.components.v1.State.Sorting.Order.ASC),
    DESC(dapr.proto.components.v1.State.Sorting.Order.DESC);

    private final dapr.proto.components.v1.State.Sorting.Order equivalent;

    Order(dapr.proto.components.v1.State.Sorting.Order equivalent) {
      this.equivalent = equivalent;
    }

    // Convert Protocol Buffer model to this SDK internal Model
    static final Map<dapr.proto.components.v1.State.Sorting.Order, Sorting.Order> toSdkModel =
        Arrays.stream(Sorting.Order.values())
            .collect(Collectors.toMap(Sorting.Order::getValue, Function.identity()));

    public State.Sorting.Order getValue() {
      return equivalent;
    }

    public static Sorting.Order of(dapr.proto.components.v1.State.Sorting.Order value) {
      return toSdkModel.get(value);
    }
  }
}
