package com.github.trganda.codec.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResultSetRow extends AbstractMySQLPacket implements MySQLServerPacket {
    private final List<Object> values = new ArrayList<>();

    public ResultSetRow(int sequenceId, Object... values) {
        super(sequenceId);
        Collections.addAll(this.values, values);
    }

    public ResultSetRow(int sequenceId, Collection<Object> values) {
        super(sequenceId);
        this.values.addAll(values);
    }

    public List<Object> getValues() {
        return values;
    }
}
