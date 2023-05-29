package com.github.trganda.codec;

public interface RowsChangedVisitable {
  default void accept(RowsChangedVisitor visitor) {
    throw new IllegalStateException("Failed to accept");
  }
}
