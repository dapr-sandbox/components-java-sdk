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

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Container for pluggable components, responsible for hosting and turning them into a server.
 *
 * <p>Takes care of:
 * <ol>
 *   <li>setting up Unix Domain Socket files for each pluggable component,</li>
 *   <li>environment variable handling,</li>
 *   <li>setting up the required server machinery to expose the service and handle requests for it.</li>
 * </ol>
 *
 * <p>If you end up needing to extend functionality of this class it might be simpler re-implementing it.
 */
public class PluggableComponentServer {
  private static final Logger log = Logger.getLogger(PluggableComponentServer.class.getName());

  /**
   * The folder where UDS files will be created.
   */
  private final String unixDomainSocketFolder;

  // Our "list" or registered components. We use a Map to avoid duplicated registrations.
  private final Map<String, Server> servers = new HashMap<>();

  /**
   * Default Constructor.
   */
  public PluggableComponentServer() {
    this.unixDomainSocketFolder = getUnixSocketFolderOrAbort();
  }

  /**
   * Add a new pluggable component to this server.
   *
   * @param component the pluggable component to be added.
   * @return A reference to this {@link PluggableComponentServer}.
   * @throws IOException if we face problems creating the Unix Domain Socket file for this component.
   */
  public PluggableComponentServer registerComponent(final PluggableComponent component) throws IOException {
    final String componentName = component.getName();
    if (servers.containsKey(componentName)) {
      logFatalAndAbort("A pluggable component named '" + componentName
          + "' was already registered. Aborting.");
    }
    final Server componentServer = buildServerForComponent(component);

    this.servers.put(componentName, componentServer);

    return this;
  }

  /**
   * Starts all pluggable component servers and wait for them to complete.
   *
   * @throws IOException if we face errors starting the servers for each component.
   */
  public void run() throws IOException {
    startServers();
    blockUntilShutdown();
  }

  //
  // Component Registration
  //

  private Server buildServerForComponent(final PluggableComponent component) throws IOException {
    final String componentName = component.getName();
    log.info("Creating server for component " + componentName);

    // Sanity check: we can't do anything with a pluggable component if it isn't exposing any service.
    final List<BindableService> exposedServices = component.getExposedServices();
    if (exposedServices.isEmpty()) {
      logFatalAndAbort("Pluggable component " + componentName
          + " is not exposing any service or Dapr API. Aborting.");
    }

    // Unix Domain Socket setup
    final Path componentUdsPath = buildPathForComponentUnixDomainSocket(componentName);
    log.info("Configuring server to listen to unix socket domain on file " + componentUdsPath.toAbsolutePath());
    // If file exists, remove it.
    final File componentUdsFile = componentUdsPath.toFile();
    if (Files.deleteIfExists(componentUdsPath)) {
      log.info("Removed previous Unix Socket Descriptor in [" + componentUdsFile + "].");
    }
    // Regardless, delete this file on exist. Just good hygiene ;)
    componentUdsFile.deleteOnExit();

    // Setup the server for handling requests on the UDS
    final DomainSocketAddress unixSocket = new DomainSocketAddress(componentUdsFile);
    final EventLoopGroup eventLoopGroup;
    final Class<? extends ServerChannel> serverChannelClass;
    if (KQueue.isAvailable()) {
      log.info("Using KQueue");
      eventLoopGroup = new KQueueEventLoopGroup();
      serverChannelClass = KQueueServerDomainSocketChannel.class;
    } else {
      log.info("Using Epoll");
      eventLoopGroup = new EpollEventLoopGroup();
      serverChannelClass = EpollServerDomainSocketChannel.class;
    }
    final NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(unixSocket)
        .channelType(serverChannelClass)
        .workerEventLoopGroup(eventLoopGroup)
        .bossEventLoopGroup(eventLoopGroup);

    // Add the services exposed by this pluggable component
    exposedServices.forEach(serverBuilder::addService);

    return serverBuilder.build();
  }

  //
  // Unix Domain Socket - path handling, creation and auxiliary methods
  //

  private static String getUnixSocketFolderOrAbort() {
    // Environment variable "parsing"
    final String unixSocketPath = getEnvVarOrDefault(
        Constants.EnvironmentVariable.DAPR_COMPONENTS_SOCKETS_PATH,
        Constants.Defaults.DAPR_COMPONENTS_SOCKETS_PATH);
    // Validate directory exists and is a writable folder
    final File targetFolder = new File(unixSocketPath);
    final boolean isValidPath = targetFolder.exists()
        && targetFolder.isDirectory()
        && targetFolder.canWrite();
    if (!isValidPath) {
      final String errMsg = "ERROR: path '" + unixSocketPath + "' is not an existing writable directory. Aborting";
      logFatalAndAbort(errMsg);
    }

    log.info("Unix Domain Socket files will be created on " + targetFolder.toPath().toAbsolutePath());

    return unixSocketPath;
  }

  // While this does what its name says, it is not really testable.
  // We should consider if migrating this to exceptions would be better.
  // Created components-java-sdk#21 to track it.
  private static void logFatalAndAbort(String errMsg) {
    log.severe(errMsg);
    System.err.println(errMsg);
    System.exit(1);
  }

  private static String getEnvVarOrDefault(final String envVarName, final String defaultValue) {
    String value = System.getenv(envVarName);
    if (value == null) {
      log.info("Environment variable " + envVarName + " is not defined. "
          + " Assuming default value of " + defaultValue);
      return defaultValue;
    }
    return value;
  }

  private Path buildPathForComponentUnixDomainSocket(final String componentName) {
    final String suffix = getEnvVarOrDefault(
        Constants.EnvironmentVariable.DAPR_COMPONENTS_SOCKET_EXTENSION,
        Constants.Defaults.DAPR_COMPONENTS_SOCKET_EXTENSION);
    final String udsFilename = componentName + '.' + suffix;
    final Path targetFolder = Paths.get(unixDomainSocketFolder);
    return targetFolder.resolve(udsFilename);
  }


  //
  // Server lifecycle management methods
  //


  private void startServers() throws IOException {
    // For each registered server, start it and set up add it to a compound shutdown hook.
    for (var componentNameAndServer : servers.entrySet()) {
      final var componentName = componentNameAndServer.getKey();
      final var server = componentNameAndServer.getValue();

      server.start();
      fixUnixDomainSocketFilePermissions(componentName);
      log.info("Started server for " + componentName);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      servers.forEach((componentName, server) -> {
        try {
          System.err.println("    *** shutting doing server instance " + server.toString());
          server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
      });
      System.err.println("*** server shut down");
    }));
  }

  /*
   * In other languages/runtimes, such as Go and .Net, we can invoke the syscall `umask` to ensure any UDS this
   * server creates are world-Read/Writable. It is a simple and elegant way to solve the "permission issue".
   * This is not the case for Java: the std. SDK doesn't expose such syscall. To address this we perform the
   * equivalent of `chmod ugo=rwx` on each unix domain socket file we create.
   */
  private void fixUnixDomainSocketFilePermissions(final String componentName) {
    final File unixDomainSocketFile = buildPathForComponentUnixDomainSocket(componentName).toFile();
    // We assume that the UDS file is created after server.start(). If this assumption proves to be false (for instance,
    // because this creation happens in another thread) we will be notified.
    assert unixDomainSocketFile.exists();
    final boolean notOwnerOnly = false; // Fix permissions for other users too, not only for its owner.
    // chmod ugo=rwx
    final boolean success = unixDomainSocketFile.setExecutable(true, notOwnerOnly)
        && unixDomainSocketFile.setReadable(true, notOwnerOnly)
        && unixDomainSocketFile.setWritable(true, notOwnerOnly);
    if (!success) {
      logFatalAndAbort("Failed to fix permissions for file " + unixDomainSocketFile);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() {
    // There probably is a more elegant way of doing this but, for the time being, this should do.
    servers.values()
        .parallelStream()
        .map(server -> {
          try {
            server.awaitTermination();
            return true;
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
  }
}