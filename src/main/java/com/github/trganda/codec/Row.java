package com.github.trganda.codec;

public interface Row {
  void accept(RowVisitor visitor);
}
