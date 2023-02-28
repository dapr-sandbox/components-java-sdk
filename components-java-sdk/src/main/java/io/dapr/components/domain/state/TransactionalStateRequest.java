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

import dapr.proto.components.v1.State;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public record TransactionalStateRequest(List<TransactionableOperation> operations, Map<String, String> metadata) {

  public TransactionalStateRequest(final List<TransactionableOperation> operations,
                                   final Map<String, String> metadata) {
    this.operations = List.copyOf(Objects.requireNonNull(operations));
    this.metadata = Map.copyOf(Objects.requireNonNull(metadata));
  }

  /**
   * Iterates over each item in the operations list invoking the appropriate visitor
   * in the same order those operations are found in the list.
   *
   * @param deleteVisitor Visitor that will be invoked for delete requests in the list.
   * @param setVisitor  Visitor that will be invoked for delete requests in the operations
   *                    list.
   */
  public void forEachOperation(Consumer<DeleteRequest> deleteVisitor,
                               Consumer<SetRequest> setVisitor) {
    for (var operation : this.operations()) {
      if (operation instanceof DeleteRequest deleteRequest) {
        deleteVisitor.accept(deleteRequest);
      } else if (operation instanceof SetRequest setRequest) {
        setVisitor.accept(setRequest);
      } else {
        throw new UnsupportedOperationException("The provided operation of type "
            + operation.getClass().toString() + " is not a valid TransactionableOperation");
      }
    }
  }

  static TransactionableOperation operationFromProto(final State.TransactionalStateOperation op) {
    Objects.requireNonNull(op);
    if (op.hasSet()) {
      return SetRequest.fromProto(op.getSet());
    } else if (op.hasDelete()) {
      return DeleteRequest.fromProto(op.getDelete());
    } else {
      // TODO use a better exception
      throw new UnsupportedOperationException("Unsupported Transactionable Operation " + op);
    }
  }

  /**
   * Conversion from protocol buffers.
   *
   * @param other The Protocol Buffer representation of a TransactionalStateRequest.
   * @return The provided protocol buffer object converted into the local domain.
   */
  public static TransactionalStateRequest fromProto(State.TransactionalStateRequest other) {
    final List<TransactionableOperation> operations = other.getOperationsList()
        .stream()
        .map(TransactionalStateRequest::operationFromProto)
        .toList();

    return new TransactionalStateRequest(operations, other.getMetadataMap());
  }
}
