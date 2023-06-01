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
    NUM(0x8000),
    NO_DEFAULT_VALUE(0x1000),
    BIT(-7);
    private final int value;

    ColumnFlag(int val) {
        this.value = val;
    }
}
