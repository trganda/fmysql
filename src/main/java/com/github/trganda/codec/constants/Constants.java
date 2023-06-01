package com.github.trganda.codec.constants;

public interface Constants {

    int NUL_BYTE = 0x00;
    int RESPONSE_OK = 0x00;
    int RESPONSE_EOF = 0xfe;
    int RESPONSE_ERROR = 0xff;
    int MINIMUM_SUPPORTED_PROTOCOL_VERSION = 10;
    int SQL_STATE_SIZE = 6;

    // Handshake constants
    int AUTH_PLUGIN_DATA_PART1_LEN = 8;
    int AUTH_PLUGIN_DATA_PART2_MIN_LEN = 13;
    int AUTH_PLUGIN_DATA_MIN_LEN = AUTH_PLUGIN_DATA_PART1_LEN + AUTH_PLUGIN_DATA_PART2_MIN_LEN;
    int HANDSHAKE_RESERVED_BYTES = 10;

    // Load Local in File
    byte LOAD_LOCAL_IN_FILE_RESPONSE_FLAG = (byte) 0xFB;

    // Auth plugins
    String DEFAULT_AUTH_PLUGIN_NAME = "mysql_native_password";
    String CACHING_SHA2_PASSWORD = "caching_sha2_password";

    // Changed from 1MB to 10MB.
    int DEFAULT_MAX_PACKET_SIZE = 10485760;
}
