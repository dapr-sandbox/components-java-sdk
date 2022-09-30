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

package io.dapr.components.cli;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a GRPC service exposing a Dapr pluggable component into a fully functional executable.
 *
 * <p>Takes care of:
 * 1. CLI argument parsing,
 * 2. setting up Unix Domain Socket files,
 * 3. environment variable handling,
 * 4. setting up the required server machinery to expose the service and handle requests for it.
 *
 * <p>If you end up needing to extend functionality of this class it might be simpler re-implementing it.
 */
@Log
@RequiredArgsConstructor
public class PluggableComponentServer {
  public static final String DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE = "DAPR_COMPONENT_SOCKET_PATH";
  /**
   * Nane of the current CLI program. This will be used for constructing our --help messages.
   */
  @NonNull
  private String programName;

  /**
   * GRPC service for the Dapr component you want to have exposed.
   */
  @NonNull final BindableService exposedService;

  // Initialized by run, which is the only public method in this class.
  private Server server;

  /** Setups the machinery and wrappers to expose your service as a server.
   *
   * <p>Takes care of setting up a server and parsing command line to serve your
   * service.</p>
   *
   * <p>Ideally a server can only expose/serve a single service. If you don't
   * know what you are doing, do no try to expose more than once service / component
   * with the same server. While possible, this is not the model that Dapr itself
   * expects from components and it won't know how to handle multiple components
   * being served from the same UNIX socket.</p>
   *
   * @param args Command line arguments that needs to be parsed.
   *
   * @throws IOException Thrown if an error happened while running the service.
   * @throws InterruptedException Thrown if an error happened while running the service.
   */
  public void main(@NonNull final String[] args) throws IOException, InterruptedException {
    // There is no command line parsing - the only thing this program expects
    // is that env. var. referred by DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE is defined.
    final String unixSocketPath = getUnixSocketPathFromEnvOrDie();

    // Start server
    log.info("Starting server for " + programName + "...");
    server = buildUnixSocketServer(unixSocketPath, exposedService);

    start();
    // Ensure unix domain file is accessible by outside daprd
    fixFilePermissions(unixSocketPath);
    blockUntilShutdown();
  }

  /**
   * Ensure the created Unix Socket file is world-readable and world-writable.
   *
   * <p>Since the user under with the component run might be different from the one
   * used by `daprd`, the simplest way to unsure both can read and write to the same
   * Unix Socket Domain file is making those files world readable and world writable.
   * And that is what this method does.</p>
   *
   * <p>Another options is to set a `umask` value in the environment that runs this server.
   * But this approach here seems simpler for the component developer and user.
   * </p>
   *
   * <p>Make sure to call this method <b>after</b> the unix domain file has been created.
   * doing so before will be meaningless and might produce a warning.
   * </p>
   *
   * @param unixSocketPath the file path for the Unix Domain File we want to make world R/W'able.
   */
  private static void fixFilePermissions(String unixSocketPath) {
    final File unixSocketFile = new File(unixSocketPath);
    final boolean fixPermissions = unixSocketFile.setWritable(true, false)
            && unixSocketFile.setReadable(true, false);
    if (!fixPermissions) {
      log.warning("Could not configure R/W permissions for unix socked domain file " + unixSocketPath);
    }
  }

  private static Server buildUnixSocketServer(
      @NonNull final String unixSocketPath,
      @NonNull final BindableService exposedService) throws IOException {
    log.info("Configuring server to listen to unix socket domain on file " + unixSocketPath);
    // If file exists, remove it.
    final File unixSocketFile = new File(unixSocketPath);
    if (unixSocketFile.exists()) {
      log.warning("Unix Socket Descriptor in [" + unixSocketPath + "] already exists. "
          + "Removing it to recreate it.");
      Files.deleteIfExists(unixSocketFile.toPath());
    }

    // Regardless, delete this file on exit. It's just good manners. ;)
    unixSocketFile.deleteOnExit();

    final DomainSocketAddress unixSocket = new DomainSocketAddress(unixSocketPath);
    final EpollEventLoopGroup group = new EpollEventLoopGroup();

    return NettyServerBuilder.forAddress(unixSocket)
        .channelType(EpollServerDomainSocketChannel.class)
        .workerEventLoopGroup(group)
        .bossEventLoopGroup(group)
        .addService(exposedService)
        .build();
  }

  private void start() throws IOException {
    server.start();
    log.info("Server started.");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      try {
        PluggableComponentServer.this.stop();
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
      System.err.println("*** server shut down");
    }));
  }

  private void stop() throws InterruptedException {
    server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    server.awaitTermination();
  }

  /** Retrieves the path for Unix Socket Path or exit the program with an error status.
   *
   * <P>This path is read from the DAPR_COMPONENT_SOCKET_PATH environment variable, if that is defined.
   *
   * @return the path pointed by env. var. DAPR_COMPONENT_SOCKET_PATH.
   */
  public String getUnixSocketPathFromEnvOrDie() {
    log.info("Retrieving unix socket domain path from env. var. " + DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE);
    final Optional<String> maybeUnixSocketPath = Optional.ofNullable(
            System.getenv(PluggableComponentServer.DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE));
    if (!maybeUnixSocketPath.isPresent()) {
      System.err.println("ERROR: You MUST provide a UNIX socket path using env. var. "
              + DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE);
      System.exit(1);
      // code ends here! no return
    } else {
      log.info(DAPR_SOCKET_PATH_ENVIRONMENT_VARIABLE + "=" + maybeUnixSocketPath.get());
    }

    return maybeUnixSocketPath.get();
  }
}
