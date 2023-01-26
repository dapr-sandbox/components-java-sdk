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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * The SDK representation of a StateStore#get request.
 *
 * @param key The key that should be retrieved.
 * @param metadata Request associated metadata.
 * @param consistency The get consistency level.
 */
public record GetRequest(String key, Map<String, String> metadata, StateOptions.StateConsistency consistency) {

  /**
   * Constructor.
   *
   * @param key The key that should be retrieved.
   * @param metadata Request associated metadata.
   * @param consistency The get consistency level.
   */
  public GetRequest(String key, Map<String, String> metadata, StateOptions.StateConsistency consistency) {
    this.key = Objects.requireNonNull(key);
    this.metadata = Collections.unmodifiableMap(Objects.requireNonNull(metadata));
    this.consistency = Objects.requireNonNull(consistency);
  }

  /**
   * Conversion constructor.
   *
   * @param other The Protocol Buffer representation of a GetRequest
   */
  public GetRequest(dapr.proto.components.v1.State.GetRequest other) {
    this(other.getKey(),
        other.getMetadataMap(),
        StateOptions.StateConsistency.of(other.getConsistency()));
  }
}