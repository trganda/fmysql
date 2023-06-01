package com.github.trganda.codec.constants;

/**
 * The MySQL client/server capability flags.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/status-flags.html">Server Status Flags
 *     Reference Documentation</a>
 * or
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad>Server Status Flags</a>
 */
public enum ServerStatusFlag {
    IN_TRANSACTION,
    AUTO_COMMIT,
    MORE_RESULTS_EXIST,
    NO_GOOD_INDEX_USED,
    CURSOR_EXISTS,
    LAST_ROW_SENT,
    DATABASE_DROPPED,
    NO_BACKSLASH_ESCAPES,
    METADATA_CHANGED,
    QUERY_WAS_SLOW,
    PREPARED_STATEMENT_OUT_PARAMS,
    IN_READONLY_TRANSACTION,
    SESSION_STATE_CHANGED,
    UNKNOWN_13,
    UNKNOWN_14,
    UNKNOWN_15
}
