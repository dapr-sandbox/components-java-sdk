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

package io.dapr.components.server;

import com.google.common.collect.ImmutableList;
import io.dapr.components.domain.state.StateStore;
import io.dapr.components.wrappers.StateStoreGrpcComponentWrapper;
import io.grpc.BindableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single pluggable component.
 *
 * <p>A pluggable component can be accessed by a single Unix Domain Socket file.
 * The name of this component is used to generate this UDS filename.
 *
 * <p>Additionally, a single pluggable component can host different components APIs.
 * No, that is not the normal use-case, but it is a supported one. For further
 * information see
 * <a href="https://docs.dapr.io/operations/components/pluggable-components/pluggable-components-registration/">
 * How-To: Register a pluggable component</a>
 *
 * <p>Technically, this class represents a named collection of gRPC <b>services</b> implementing
 * distinct Components API that will, when materialized by a {@link PluggableComponentServer},
 * have a 1:1 mapping with a Unix Domain Socket file</p>
 *
 * <p>A PluggableComponent instance by itself is not capable of receiving and handling requests:
 * you need to add it to a {@link PluggableComponentServer}.</p>
 */
public final class PluggableComponent {
  private final String name;

  private final List<BindableService> exposedServices = new ArrayList<>();

  // A given component API (state store, pubusb, binding etc) can be registered only once.
  private boolean alreadyAddedStaStore = false;

  /**
   * Creates a new pluggable component.
   *
   * @param componentName this pluggable component name.
   */
  public PluggableComponent(String componentName) {
    this.name = Objects.requireNonNull(componentName);
  }

  /**
   * Static builder for PluggableComponents.
   *
   * @param componentName name for the new pluggable component.
   * @return a PluggableComponent instance.
   */
  public static PluggableComponent withName(String componentName) {
    return new PluggableComponent(componentName);
  }

  /**
   * Register a new StateStore with this pluggable component.
   *
   * @param stateStore The stateStore instance we want to expose through this pluggable component.
   * @return The current {@link PluggableComponent} instance, so calls can be chained.
   */
  public PluggableComponent withStateStore(StateStore stateStore) {
    assert !alreadyAddedStaStore; // No, you cannot add multiple stateStores

    exposedServices.add(new StateStoreGrpcComponentWrapper(stateStore));

    // Register other facets of a stateStore like QueriableStateStore and TransactionalStateStore
    // IFF the current stateStore object supports those facets.
    //
    // if (stateStore instanceof QueriableStateStore) {
    //   exposedServices.add(new QueriableStateStoreGrpcComponentWrapper(stateStore));
    // }
    // if (stateStore instanceof TransactionalStateStore) {
    //   exposedServices.add(new TransactionalStateStoreGrpcComponentWrapper(stateStore));
    // }

    // StateStore added and nothing else can be added
    alreadyAddedStaStore = true;
    return this;
  }

  //
  // Getters are package private
  //

  String getName() {
    return name;
  }

  List<BindableService> getExposedServices() {
    return ImmutableList.copyOf(exposedServices);
  }
}
