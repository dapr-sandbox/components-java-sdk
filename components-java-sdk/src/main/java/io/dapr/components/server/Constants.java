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

/**
 * Constant definitions.
 */
public final class Constants {
  public static final class Defaults {
    /**
     * Default location where Dapr looks for Pluggable Components Unix Domain Socket files.
     * If env. var named by {@link EnvironmentVariable#DAPR_COMPONENTS_SOCKETS_PATH} is not set, this value is used.
     */
    public static final String DAPR_COMPONENTS_SOCKETS_PATH = "/tmp/dapr-components-sockets";

    /**
     * The default suffix for socket files created by Dapr Pluggable Components.
     *
     * <p>Can be overridden by  {@link EnvironmentVariable#DAPR_COMPONENTS_SOCKET_EXTENSION}.
     */
    public static final String DAPR_COMPONENTS_SOCKET_EXTENSION = "sock";


  }

  public static final class EnvironmentVariable {

    /**
     * Environment Variable specifying where Dapr will look for Pluggable Components Unix Domain Socket files.
     * If specified, defines were this application will write the UDS files it creates.
     * If unset, the value from {@link Defaults#DAPR_COMPONENTS_SOCKETS_PATH} is used instead.
     *
     */
    public static final String DAPR_COMPONENTS_SOCKETS_PATH = "DAPR_COMPONENTS_SOCKETS_FOLDER";

    /**
     * The environment variable name that defines the extension for socket files created by Dapr Pluggable Components.
     *
     * <p>If unset, the value from {@link Defaults#DAPR_COMPONENTS_SOCKET_EXTENSION} is used instead.
     * If set, the unix domain socket file for components will be built by joining a component name,
     * a single "." character and the contents of this variable.
     */
    public static final String DAPR_COMPONENTS_SOCKET_EXTENSION = "DAPR_COMPONENTS_SOCKETS_EXTENSION";
  }

}
