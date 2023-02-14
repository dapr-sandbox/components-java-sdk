/*
 * Copyright 2022 The Dapr Authors
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

package io.dapr.examples.statestore;


import io.dapr.components.domain.state.DeleteRequest;
import io.dapr.components.domain.state.Exceptions;
import io.dapr.components.domain.state.GetRequest;
import io.dapr.components.domain.state.GetResponse;
import io.dapr.components.domain.state.SetRequest;
import io.dapr.components.domain.state.StateStore;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class InMemoryStateStore implements StateStore {

  private final Map<String, GetResponse> entries = new HashMap<>();

  @Override
  public Mono<Void> init(Map<String, String> properties) {
    return Mono.empty();
  }

  @Override
  public Mono<GetResponse> get(GetRequest getRequest) {
    return Mono.justOrEmpty(entries.get(getRequest.key()));
  }

  @Override
  public Mono<Void> delete(DeleteRequest deleteRequest) {
    final String key = deleteRequest.key();
    final GetResponse existingData = entries.get(key);
    if (existingData == null) {
      // Key not found: nothing to remove, nothing else to do.
      return Mono.empty();
    } else if (existingData.etag().equals(deleteRequest.etag())) {
      // Valid remove request
      entries.remove(key);
      return Mono.empty();
    } else {
      // Data exists but etag mismatch
      return Mono.error(Exceptions.getEtagMismatchException("in-memory-etag-mismatch-on-delete"));
    }
  }

  @Override
  public Mono<Void> set(SetRequest setRequest) {
    var upsertEntry = new GetResponse(
        setRequest.value(),
        setRequest.etag(),
        setRequest.metadata(),
        setRequest.contentType()
    );
    entries.put(setRequest.key(), upsertEntry);
    // Nothing else to do but return with no errors.
    return Mono.empty();
  }
}
