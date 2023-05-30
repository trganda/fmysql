package com.github.trganda.codec.constants;

/**
 * Field flags.
 *
 * <p>{@see http://dev.mysql.com/doc/refman/5.7/en/c-api-data-structures.html}
 */
public enum ColumnFlag {
  NOT_NULL,
  PRI_KEY,
  UNIQUE_KEY,
  MULTIPLE_KEY,
  UNSIGNED,
  ZEROFILL,
  BINARY,
  AUTO_INCREMENT,
  ENUM,
  SET,
  BLOB,
  TIMESTAMP,
  NUM,
  NO_DEFAULT_VALUE,
  UNKNOWN14,
  UNKNOWN15
}
