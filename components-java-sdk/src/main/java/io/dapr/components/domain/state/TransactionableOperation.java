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

/**
 * This interface act as a marker for operations that can be used with Transactions.
 *
 * <p>This interface is being used as a type-safe alternative to representing the enum/oneof
 * used by {@link dapr.proto.components.v1.State.TransactionalStateOperation}</p>
 */
public interface TransactionableOperation {
}
