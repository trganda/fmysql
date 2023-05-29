package com.github.trganda.codec;

// TODO add parameters
public interface RowsChangedVisitor {
  void columnAddedRow();

  void endAddedRow();

  void columnDeletedRow();

  void endDeletedRow();

  void columnUpdatedRow();

  void endUpdatedRow();
}
