package com.github.trganda.codec.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResultSetRow extends AbstractMySQLPacket implements MySQLServerPacket {
  private final List<String> values = new ArrayList<>();

  public ResultSetRow(int sequenceId, String... values) {
    super(sequenceId);
    Collections.addAll(this.values, values);
  }

  public ResultSetRow(int sequenceId, Collection<String> values) {
    super(sequenceId);
    this.values.addAll(values);
  }

  public List<String> getValues() {
    return values;
  }
}
