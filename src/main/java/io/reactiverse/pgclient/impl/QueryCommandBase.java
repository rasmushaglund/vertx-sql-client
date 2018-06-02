/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;

import java.util.Collections;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class QueryCommandBase<T> extends CommandBase<Boolean> {

  public RowResultDecoder<?, T> decoder;
  final QueryResultHandler<T> resultHandler;
  final Collector<Row, ?, T> collector;

  QueryCommandBase(Collector<Row, ?, T> collector, QueryResultHandler<T> handler) {
    super(handler);
    this.resultHandler = handler;
    this.collector = collector;
  }

  abstract String sql();

  @Override
  public void handleCommandComplete(int updated) {
    this.result = false;
    PgResult<T> result;
    if (decoder != null) {
      result = decoder.complete(updated);
    } else {
      result = new PgResultImpl<>(updated, Collections.emptyList(), emptyResult(collector), 0);
    }
    resultHandler.handleResult(result);
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = new PgException(errorResponse);
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }
}
