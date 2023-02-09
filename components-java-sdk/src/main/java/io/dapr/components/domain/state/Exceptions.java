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

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.ErrorInfo;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;

import java.util.Map;

/**
 * Utility functions to wrap gRPC error Status into exceptions.
 *
 * <p>You might use these functions with {@code Mono.error} and {@code Flux.error} methods.</p>
 */
public class Exceptions {

  private static final String AFFECTED = "affected";
  private static final String EXPECTED = "expected";
  private static final String ETAG_FIELD_NAME = "etag";

  /**
   * The default description for Etag Mismatch errors.
   */
  public static final String ETAG_MISMATCH_DESCRIPTION = "The ETag field provided does not match the one in the store";

  /**
   * Builds an exception wrapping an gRPC status error for an Etag Mismatch Error.
   *
   * @param message a message-code for this error.
   * @param description A message describing how/why this etag mismatched
   * @return A gRPC compatible exception.
   */
  public static Throwable getEtagMismatchException(final String message, final String description) {
    final Status status = Status.newBuilder()
        .setCode(io.grpc.Status.Code.FAILED_PRECONDITION.value())
        .setMessage(message)
        .addDetails(Any.pack(BadRequest.FieldViolation.newBuilder()
            .setField(ETAG_FIELD_NAME)
            .setDescription(description)
            .build()))
        .build();
    return StatusProto.toStatusException(status);
  }

  /**
   * Builds an exception wrapping an gRPC status error for an Etag Mismatch Error with
   * a default description.
   *
   * @param message a message-code for this error.
   * @return A gRPC compatible exception.
   */
  public static Throwable getEtagMismatchException(final String message) {
    final Status status = Status.newBuilder()
        .setCode(io.grpc.Status.Code.FAILED_PRECONDITION.value())
        .setMessage(message)
        .addDetails(Any.pack(BadRequest.FieldViolation.newBuilder()
            .setField(ETAG_FIELD_NAME)
            .setDescription(ETAG_MISMATCH_DESCRIPTION)
            .build()))
        .build();
    return StatusProto.toStatusException(status);
  }

  /**
   * Builds an exception wrapping an gRPC status error for an Invalid Etag.
   *
   * @param message a message-code for this error.
   * @param description A message describing why this etag is invalid.
   * @return A gRPC compatible exception.
   */
  public static Throwable getInvalidETagException(final String message, final String description) {
    final Status status = Status.newBuilder()
        .setCode(io.grpc.Status.Code.INVALID_ARGUMENT.value())
        .setMessage(message)
        .addDetails(Any.pack(BadRequest.FieldViolation.newBuilder()
            .setField(ETAG_FIELD_NAME)
            .setDescription(description)
            .build()))
        .build();
    return StatusProto.toStatusException(status);
  }

  /**
   * Builds an exception wrapping an gRPC status error when the number of items affected
   * by a bulk operation does not match the number of requested items in the bulk request.
   *
   * @param message a message-code for this error.
   * @param affected Number of items that were ultimately affected by the bulk operation.
   * @param expected Number of items that were originally expected to be operated by bulk operation.
   * @return A gRPC compatible exception.
   */
  public static Throwable getBulkDeleteRowMismatchException(final String message,
                                                            final int affected,
                                                            final int expected) {
    final Status status = Status.newBuilder()
        .setCode(io.grpc.Status.Code.INTERNAL.value())
        .setMessage(message)
        .addDetails(Any.pack(ErrorInfo.newBuilder()
            .putAllMetadata(Map.ofEntries(
                Map.entry(AFFECTED, Integer.toString(affected)),
                Map.entry(EXPECTED, Integer.toString(expected))
            ))
            .build()))
        .build();
    return StatusProto.toStatusException(status);
  }
}
