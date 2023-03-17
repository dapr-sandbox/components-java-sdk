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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a Query.
 *
 * @param filter Filters that should be applied.
 * @param sort The sort order.
 * @param pagination The query pagination params.
 */
public record Query(Map<String, Object> filter, List<Sorting> sort, @Nullable Pagination pagination) {

  /**
   * Canonical constructor.
   *
   * @param filter Filters that should be applied.
   * @param sort The sort order.
   * @param pagination The query pagination params.
   */
  public Query(Map<String, Object> filter, List<Sorting> sort, Pagination pagination) {
    this.filter = Map.copyOf(Objects.requireNonNull(filter));
    this.sort = List.copyOf(Objects.requireNonNull(sort));
    this.pagination = pagination;
  }

  /**
   * Converts from protocol buffer types to local domain types.
   *
   * @param other The Protocol Buffer representation of a Query.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static Query fromProto(State.Query other) {
    final Map<String, Object> filter = other.getFilterMap()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> (Object) entry.getValue()));
    return new Query(
        filter,
        other.getSortList().stream().map(Sorting::fromProto).toList(),
        Pagination.fromProto(other.getPagination()));
  }
}
