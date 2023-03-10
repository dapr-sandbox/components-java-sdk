package io.dapr.components;

import com.google.protobuf.ByteString;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TestData {
  public static final ByteString TEST_VALUE = ByteString.copyFrom("value🤞", StandardCharsets.UTF_8);
  public static final ByteString TEST_VALUE_2 = ByteString.copyFrom("value 2", StandardCharsets.UTF_8);
  public static final Map<String, String> METADATA_REQUEST_MAP = Map.of("ttlInSeconds", "120");
  public static final Map<String, String> METADATA_RESPONSE_MAP = Map.of("ttlInSeconds", "60");
  public static final String TEST_KEY = "set-key-1";
  public static final String TEST_KEY_2 = "set-key-2";
  public static final String ETAG_VALUE = "1";
  public static final String CONTENT_TYPE = "text/plain;charset=utf-8";
}
