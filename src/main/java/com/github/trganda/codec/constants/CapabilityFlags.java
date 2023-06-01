package com.github.trganda.codec.constants;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.EnumSet;
import java.util.Set;

/**
 * An enum of all the MySQL client/server capability flags.
 *
 * @see <a
 *     href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__capabilities__flags.html">
 *     Capability Flags Reference Documentation</a>
 */
public enum CapabilityFlags {
    /** 0x01: 1U << 0 */
    CLIENT_LONG_PASSWORD,
    /** 0x02: 1U << 1 */
    CLIENT_FOUND_ROWS,
    /** 0x04: 1U << 2 */
    CLIENT_LONG_FLAG,
    /** 0x08: 1U << 3 */
    CLIENT_CONNECT_WITH_DB,
    /** 0x10: 1U << 4 */
    CLIENT_NO_SCHEMA,
    /** 0x20: 1U << 5 */
    CLIENT_COMPRESS,
    /** 0x40: 1U << 6 */
    CLIENT_ODBC,
    /** 0x80: 1U << 7 */
    CLIENT_LOCAL_FILES,
    /** 0x100: 1U << 8 */
    CLIENT_IGNORE_SPACE,
    /** 0x200: 1U << 9 */
    CLIENT_PROTOCOL_41,
    /** 0x400: 1U << 10 */
    CLIENT_INTERACTIVE,
    /** 0x800: 1U << 11 */
    CLIENT_SSL,
    /** 0x1000: 1U << 12 */
    CLIENT_IGNORE_SIGPIPE,
    /** 0x2000: 1U << 13 */
    CLIENT_TRANSACTIONS,
    /** Deprecated: Old flag for 4.1 protocol 0x4000: 1U << 14 */
    CLIENT_RESERVED,
    /**
     * Deprecated: old flag for 4.1 authentication. Old name: CLIENT_SECURE_CONNECTION 0x8000: 1U <<
     * 15
     */
    CLIENT_RESERVED2,
    /** 0x10000: 1U << 16 */
    CLIENT_MULTI_STATEMENTS,
    /** 0x20000: 1U << 17 */
    CLIENT_MULTI_RESULTS,
    /** 0x40000: 1U << 18 */
    CLIENT_PS_MULTI_RESULTS,
    /** 0x80000: 1U << 19 */
    CLIENT_PLUGIN_AUTH,
    /** 0x100000: 1U << 20 */
    CLIENT_CONNECT_ATTRS,
    /** 0x200000: 1U << 21 */
    CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA,
    /** 0x400000: 1U << 22 */
    CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS,
    /** 0x800000: 1U << 23 */
    CLIENT_SESSION_TRACK,
    /** 0x1000000: 1U << 24 */
    CLIENT_DEPRECATE_EOF,
    /** 0x2000000: 1U << 25 */
    CLIENT_OPTIONAL_RESULTSET_METADATA,
    /** 0x4000000: 1U << 26 */
    CLIENT_ZSTD_COMPRESSION_ALGORITHM,
    /** 0x8000000: 1U << 27 */
    CLIENT_QUERY_ATTRIBUTES,
    /** 0x10000000: 1U << 28 */
    MULTI_FACTOR_AUTHENTICATION,
    /** 0x20000000: 1U << 29 */
    CLIENT_CAPABILITY_EXTENSIONS,
    /** 0x40000000: 1U << 30 */
    CLIENT_SSL_VERIFY_SERVER_CERT,
    /** 0x80000000: 1U << 31 */
    CLIENT_REMEMBER_OPTIONS;

    public static EnumSet<CapabilityFlags> getImplicitCapabilities() {
        return EnumSet.of(
                CapabilityFlags.CLIENT_LONG_PASSWORD,
                CapabilityFlags.CLIENT_PROTOCOL_41,
                CapabilityFlags.CLIENT_TRANSACTIONS,
                CapabilityFlags.CLIENT_CONNECT_WITH_DB,
                CapabilityFlags.CLIENT_RESERVED2,
                CapabilityFlags.CLIENT_LOCAL_FILES,
                CapabilityFlags.CLIENT_PLUGIN_AUTH,
                CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA);
    }

    private static final AttributeKey<EnumSet<CapabilityFlags>> capabilitiesKey =
            AttributeKey.newInstance(CapabilityFlags.class.getName());

    public static EnumSet<CapabilityFlags> getCapabilitiesAttr(Channel channel) {
        final Attribute<EnumSet<CapabilityFlags>> attr = channel.attr(capabilitiesKey);
        if (attr.get() == null) {
            attr.set(getImplicitCapabilities());
        }
        return attr.get();
    }

    /**
     * Set the capabilities attributes of current channel as context.
     *
     * @param channel the channel of current connection
     * @param capabilities the capabilities of mysql client or server
     */
    public static void setCapabilitiesAttr(Channel channel, Set<CapabilityFlags> capabilities) {
        final Attribute<EnumSet<CapabilityFlags>> attr = channel.attr(capabilitiesKey);
        attr.set(EnumSet.copyOf(capabilities));
    }
}
