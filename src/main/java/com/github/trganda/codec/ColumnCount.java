package com.github.trganda.codec;

public class ColumnCount extends AbstractMySQLPacket implements MySQLServerPacket {

  final int fieldCount;

  public ColumnCount(int sequenceId, int fieldCount) {
    super(sequenceId);
    this.fieldCount = fieldCount;
  }

  public int getFieldCount() {
    return fieldCount;
  }
}
