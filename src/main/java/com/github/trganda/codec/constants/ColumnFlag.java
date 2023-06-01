package com.github.trganda.codec.constants;

/**
 * Field flags.
 *
 * <p>{@see
 * https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html}
 */
public enum ColumnFlag {
    NOT_NULL(0x01),
    PRI_KEY(0x02),
    UNIQUE_KEY(0x04),
    MULTIPLE_KEY(0x08),
    BLOB(0x10),
    UNSIGNED(0x20),
    ZEROFILL(0x40),
    BINARY(0x80),
    ENUM(0x100),
    AUTO_INCREMENT(0x200),
    TIMESTAMP(0x400),
    SET(0x800),
    NO_DEFAULT_VALUE(0x1000),
    ON_UPDATE_NOW(0x2000),
    PART_KEY(0x4000),
    NUM(0x8000),
    GROUP(0x8000),
    UNIQUE(0x10000),
    BINCMP(0x20000),
    GET_FIXED_FIELDS(0x40000),
    FIELD_IN_PART_FUNC(0x80000),
    FIELD_IN_ADD_INDEX(0x100000);
    private final int value;

    ColumnFlag(int val) {
        this.value = val;
    }

    public int getValue() {
        return value;
    }
}
