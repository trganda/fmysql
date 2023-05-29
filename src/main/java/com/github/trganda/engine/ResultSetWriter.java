package com.github.trganda.engine;

import java.util.List;

/**
 * Response writer.
 *
 * <p>A normal result must call a writeColumns() before all invoking of writeRow() and call finish()
 * finally
 */
public interface ResultSetWriter {

  /**
   * Write columns. It must be called before any invoking of writeRow()
   *
   * @param columns columns
   */
  void writeColumns(List<QueryResultColumn> columns);

  /**
   * Write a row. finish() must be called after writing all rows
   *
   * @param row row of cells ordered by columns
   */
  void writeRow(List<String> row);

  /** Finish the response */
  void finish();
}
