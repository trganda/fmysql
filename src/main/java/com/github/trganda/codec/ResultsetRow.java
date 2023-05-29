package com.github.trganda.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResultsetRow extends AbstractMySQLPacket implements MySQLServerPacket {
  private final List<String> values = new ArrayList<>();

  public ResultsetRow(int sequenceId, String... values) {
    super(sequenceId);
    Collections.addAll(this.values, values);
  }

  public ResultsetRow(int sequenceId, Collection<String> values) {
    super(sequenceId);
    this.values.addAll(values);
  }

  public List<String> getValues() {
    return values;
  }
}
