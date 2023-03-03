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

package io.dapr.components.wrappers;

import dapr.proto.components.v1.PubSubGrpc;
import dapr.proto.components.v1.Pubsub;
import io.dapr.components.domain.pubsub.PubSub;
import io.dapr.components.domain.pubsub.PublishRequest;
import io.dapr.components.domain.pubsub.PullMessageAcknowledgement;
import io.dapr.components.domain.pubsub.PullMessagesResponse;
import io.dapr.components.domain.pubsub.Topic;
import io.dapr.v1.ComponentProtos;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

public class PubSubGrpcComponentWrapper extends PubSubGrpc.PubSubImplBase {

  private final PubSub pubSub;

  private final Scheduler scheduler = Schedulers.boundedElastic();

  /**
   * Constructor.
   *
   * @param pubSub the pubsub that this component will expose as a service.
   */
  public PubSubGrpcComponentWrapper(PubSub pubSub) {
    this.pubSub = Objects.requireNonNull(pubSub);
  }

  @Override
  public void init(Pubsub.PubSubInitRequest request, StreamObserver<Pubsub.PubSubInitResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> pubSub.init(req.getMetadata().getPropertiesMap()))
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .map(response -> Pubsub.PubSubInitResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void features(ComponentProtos.FeaturesRequest request,
                       StreamObserver<ComponentProtos.FeaturesResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> pubSub.getFeatures())
        .map(features -> ComponentProtos.FeaturesResponse.newBuilder()
            .addAllFeatures(features)
            .build()
        )
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void ping(ComponentProtos.PingRequest request, StreamObserver<ComponentProtos.PingResponse> responseObserver) {
    Mono.just(request)
        .flatMap(req -> pubSub.ping())
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .map(successfulPing -> ComponentProtos.PingResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public void publish(Pubsub.PublishRequest request, StreamObserver<Pubsub.PublishResponse> responseObserver) {
    Mono.just(request)
        .map(PublishRequest::fromProto)
        .flatMap(pubSub::publish)
        // Response is functionally and structurally equivalent to Empty, nothing to fill.
        .map(success -> Pubsub.PublishResponse.getDefaultInstance())
        .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
  }

  @Override
  public StreamObserver<Pubsub.PullMessagesRequest> pullMessages(
      StreamObserver<Pubsub.PullMessagesResponse> responseObserver) {
    // First, convert the input requests to first request and acknowledgments Flux,
    // so we can feed it to our component
    final FirstAndRestRequestStreamToFluxAdaptor<Pubsub.PullMessagesRequest> requestAdaptor;
    requestAdaptor = new FirstAndRestRequestStreamToFluxAdaptor<>((firstProto, acksProtoFlux) -> {
      // Alright... what to do when we start receiving requests?
      // First, let's convert those requests to the local domain.
      final Topic topic = Topic.fromProto(firstProto);
      final Flux<PullMessageAcknowledgement> acksFlux = acksProtoFlux.map(PullMessageAcknowledgement::fromProto);

      // Wrap everything in a Flux. This will keep uniformity with other handlers and will allow for
      // delegating multithreading processing of requests to another thread by means of subscribeOn(scheduler)
      Flux.just(acksFlux)
          // Push these requests to the component.
          .flatMap(acks -> pubSub.pullMessages(topic, acks))
          // Move processing to a different thread -- otherwise we would get stuck in the line above
          // and this method would not return. See
          // https://projectreactor.io/docs/core/release/reference/#producing.create
          .subscribeOn(scheduler, false)
          // ... connect its response flux to the output stream from this RPC
          .map(PullMessagesResponse::toProto)
          .subscribe(responseObserver::onNext, responseObserver::onError, responseObserver::onCompleted);
    });

    // Finally, return the StreamObserver
    return requestAdaptor.requestStreamObserver();
  }

}
