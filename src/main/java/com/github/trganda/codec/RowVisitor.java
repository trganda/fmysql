package com.github.trganda.codec;

public interface RowVisitor {
  void visit(int idx, ColumnType type);
  // FIXME add methods for every value type
}
