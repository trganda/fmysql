package com.github.trganda.codec.packets;

import com.github.trganda.codec.constants.ColumnFlag;
import com.github.trganda.codec.constants.ColumnType;
import com.github.trganda.codec.constants.MySQLCharacterSet;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ColumnDefinition extends AbstractMySQLPacket implements MySQLServerPacket {
    /**
     * Catalog used, always "def"
     */
    private final String catalog;
    /**
     * Schema (e.g. database) name
     */
    private final String schema;
    /**
     * Virtual Table name
     */
    private final String table;
    /**
     * Physical Table name
     */
    private final String orgTable;
    /**
     * Virtual Column name
     */
    private final String name;
    /**
     * Physical Column name
     */
    private final String orgName;
    /**
     * Column character as defined in
     *
     * @see MySQLCharacterSet
     */
    private final MySQLCharacterSet characterSet;
    /**
     * Maximum length of this field type
     */
    private final long columnLength;
    /**
     * Type of the Column as defined in ColumnType
     *
     * @see ColumnType
     */
    private final ColumnType type;
    /**
     * Flags as defined in ColumnFlag
     *
     * @see ColumnFlag
     */
    private final Set<ColumnFlag> flags = EnumSet.noneOf(ColumnFlag.class);
    /**
     * Max shown decimal digits for this Column type: - 0x00 for integers and static strings - 0x1f
     * for dynamic strings, double, float - 0x00 to 0x51 for decimals
     */
    private final int decimals;

    private ColumnDefinition(Builder builder) {
        super(builder.sequenceId);
        this.catalog = builder.catalog;
        this.schema = builder.schema;
        this.table = builder.table;
        this.orgTable = builder.orgTable;
        this.name = builder.name;
        this.orgName = builder.orgName;
        this.characterSet = builder.characterSet;
        this.columnLength = builder.columnLength;
        this.type = builder.type;
        this.flags.addAll(builder.flags);
        this.decimals = builder.decimals;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public String getOrgTable() {
        return orgTable;
    }

    public String getName() {
        return name;
    }

    public String getOrgName() {
        return orgName;
    }

    public MySQLCharacterSet getCharacterSet() {
        return characterSet;
    }

    public long getColumnLength() {
        return columnLength;
    }

    public ColumnType getType() {
        return type;
    }

    public Set<ColumnFlag> getFlags() {
        return flags;
    }

    public int getDecimals() {
        return decimals;
    }

    public static class Builder {
        private final Set<ColumnFlag> flags = EnumSet.noneOf(ColumnFlag.class);
        private int sequenceId;
        private String catalog = "def";
        private String schema;
        private String table;
        private String orgTable;
        private String name;
        private String orgName;
        private MySQLCharacterSet characterSet = MySQLCharacterSet.UTF8_GENERAL_CI;
        private long columnLength;
        private ColumnType type;
        private int decimals;

        public Builder sequenceId(int sequenceId) {
            this.sequenceId = sequenceId;
            return this;
        }

        public Builder catalog(String catalog) {
            this.catalog = catalog;
            return this;
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder orgTable(String orgTable) {
            this.orgTable = orgTable;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder orgName(String orgName) {
            this.orgName = orgName;
            return this;
        }

        public Builder characterSet(MySQLCharacterSet characterSet) {
            this.characterSet = characterSet;
            return this;
        }

        public Builder columnLength(long columnLength) {
            this.columnLength = columnLength;
            return this;
        }

        public Builder type(ColumnType type) {
            this.type = type;
            return this;
        }

        public Builder addFlags(ColumnFlag... flags) {
            Collections.addAll(this.flags, flags);
            return this;
        }

        public Builder addFlags(Collection<ColumnFlag> flags) {
            this.flags.addAll(flags);
            return this;
        }

        public Builder decimals(int decimals) {
            this.decimals = decimals;
            return this;
        }

        public ColumnDefinition build() {
            return new ColumnDefinition(this);
        }
    }
}
