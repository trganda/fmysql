package com.github.trganda.codec;

import com.github.trganda.codec.constants.ColumnType;

public interface RowVisitor {
    void visit(int idx, ColumnType type);
    // FIXME add methods for every value type
}
