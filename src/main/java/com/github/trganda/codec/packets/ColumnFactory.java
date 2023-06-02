package com.github.trganda.codec.packets;

import com.github.trganda.codec.constants.ColumnFlag;
import com.github.trganda.codec.constants.ColumnType;
import com.github.trganda.codec.constants.MySQLCharacterSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColumnFactory extends AbstractMySQLPacket implements MySQLServerPacket {
    private final List<ColumnDefinition> columnDefinitions = new ArrayList<>();

    private final String catalog;

    private final String schema;

    private final String table;

    public ColumnFactory(int sequenceId, String catalog, String schema, String table) {
        super(sequenceId);
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
    }

    public void addColumnDefinition(ColumnDefinition... columnDefinitions) {
        Collections.addAll(this.columnDefinitions, columnDefinitions);
    }

    public void addColumnDefinition(
            String name, MySQLCharacterSet characterSet, ColumnType type, ColumnFlag... flags) {
        this.columnDefinitions.add(
                ColumnDefinition.builder()
                        .sequenceId(this.sequenceId)
                        .catalog(this.catalog)
                        .schema(this.schema)
                        .table(this.table)
                        .orgTable(this.table)
                        .name(name)
                        .orgName(name)
                        .columnLength(ColumnType.getTypeMaxLength(type))
                        .characterSet(characterSet)
                        .type(type)
                        .addFlags(flags)
                        .build());
    }

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
}
