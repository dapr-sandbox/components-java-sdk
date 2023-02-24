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

/**
 * Representation of pagination parameters.
 *
 * @param limit Maximum of results that should be returned.
 * @param token The pagination token.
 */
public record Pagination(long limit, String token) {

  /**
   * Converts from protocol buffer types to local domain types.
   *
   * @param other The Protocol Buffer representation of Pagination.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static Pagination fromProto(State.Pagination other) {
    return new Pagination(
        other.getLimit(),
        other.getToken()
    );
  }
}
