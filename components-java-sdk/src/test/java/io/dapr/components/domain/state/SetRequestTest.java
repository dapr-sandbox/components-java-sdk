package io.dapr.components.domain.state;

import com.google.protobuf.ByteString;
import dapr.proto.components.v1.State;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SetRequestTest {

  public static final String KEY = "set-key";
  public static final String VALUE = "value🤞";
  public static final String ETAG = "1";
  public static final String CONTENT_TYPE = "text/plain;charset=utf-8";

  @Test
  void fromProto() {
    var proto = State.SetRequest.newBuilder()
        .setKey(KEY)
        .setValue(ByteString.copyFrom(VALUE, StandardCharsets.UTF_8))
        .putAllMetadata(Map.of("ttlInSeconds", "120"))
        .setOptions(State.StateOptions.newBuilder()
            .setConcurrency(State.StateOptions.StateConcurrency.CONCURRENCY_LAST_WRITE)
            .setConsistency(State.StateOptions.StateConsistency.CONSISTENCY_EVENTUAL)
            .build())
        .setEtag(State.Etag.newBuilder()
            .setValue(ETAG).build())
        .setContentType(CONTENT_TYPE)
        .build();

    var result = SetRequest.fromProto(proto);
    assertThat(result).isNotNull();
    assertThat(result.key()).isEqualTo(KEY);
    assertThat(result.value().toString(StandardCharsets.UTF_8)).isEqualTo(VALUE);
    assertThat(result.etag()).isEqualTo(ETAG);
    assertThat(result.contentType()).isEqualTo(CONTENT_TYPE);
  }

  @Test
  void key() {
  }

  @Test
  void value() {
  }

  @Test
  void etag() {
  }

  @Test
  void metadata() {
  }

  @Test
  void options() {
  }

  @Test
  void contentType() {
  }
}