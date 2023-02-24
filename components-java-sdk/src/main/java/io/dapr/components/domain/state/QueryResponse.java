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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * QueryResponse is the query response.
 *
 * @param items The query response items.
 * @param token The response token.
 * @param metadata Response associated metadata.
 */
public record QueryResponse(List<QueryResponseItem> items, String token, Map<String, String> metadata) {
  /**
   * Canonical constructor.
   *
   * @param items The query response items.
   * @param token The response token.
   * @param metadata Response associated metadata.
   */
  public QueryResponse(List<QueryResponseItem> items, String token, Map<String, String> metadata) {
    this.items = List.copyOf(Objects.requireNonNull(items));
    this.token = Objects.requireNonNull(token);
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
  }

  /**
   * Conversion method.
   *
   * @return The provided object converted to its protocol buffer equivalent.
   */
  public State.QueryResponse toProto() {
    return State.QueryResponse.newBuilder()
        .addAllItems(items.stream().map(QueryResponseItem::toProto).toList())
        .setToken(token)
        .putAllMetadata(metadata)
        .build();
  }
}
