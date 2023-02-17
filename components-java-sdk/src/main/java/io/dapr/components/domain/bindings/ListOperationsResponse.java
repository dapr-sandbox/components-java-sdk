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

package io.dapr.components.domain.bindings;

import java.util.List;
import java.util.Objects;

/**
 * Response from a {@link OutputBinding#listOperations()} request.
 *
 * @param operations the list of all supported component operations.
 */
public record ListOperationsResponse(List<String> operations) {
  /**
   * Canonical consetructor.
   *
   * @param operations the list of all supported component operations.
   */
  public ListOperationsResponse(List<String> operations) {
    this.operations = List.copyOf(Objects.requireNonNull(operations));;
  }
}
