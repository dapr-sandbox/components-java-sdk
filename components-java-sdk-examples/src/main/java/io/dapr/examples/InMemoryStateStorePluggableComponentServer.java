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

package io.dapr.examples;

import io.dapr.components.server.PluggableComponent;
import io.dapr.components.server.PluggableComponentServer;
import io.dapr.examples.statestore.InMemoryStateStore;

import java.io.IOException;

public class InMemoryStateStorePluggableComponentServer {

  /**
   * Main body for our example.
   *
   * @param args Command line arguments that we ignore.
   * @throws IOException if we face errors starting this server.
   */
  public static void main(String[] args) throws IOException {

    final PluggableComponentServer server = new PluggableComponentServer();
    server.registerComponent(PluggableComponent
            .withName("my-in-memory-state-store")
            .withStateStore(new InMemoryStateStore())
        )
        .run();
  }
}