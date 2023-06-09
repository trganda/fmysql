package com.github.trganda.codec.decoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.auths.Handshake;
import com.github.trganda.codec.constants.CapabilityFlags;
import com.github.trganda.codec.constants.Constants;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.constants.ServerStatusFlag;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class MySQLServerConnectionPacketDecoder extends AbstractPacketDecoder
        implements MySQLServerPacketDecoder {

    public MySQLServerConnectionPacketDecoder() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public MySQLServerConnectionPacketDecoder(int maxPacketSize) {
        super(maxPacketSize);
    }

    @Override
    protected void decodePayload(
            ChannelHandlerContext ctx, int sequenceId, ByteBuf packet, List<Object> out) {
        final Channel channel = ctx.channel();
        final Set<CapabilityFlags> capabilities = CapabilityFlags.getCapabilitiesAttr(channel);
        final Charset serverCharset = MySQLCharacterSet.getServerCharsetAttr(channel).getCharset();

        final int header = packet.readByte() & 0xff;
        switch (header) {
            case RESPONSE_OK:
                out.add(decodeOkResponse(sequenceId, packet, capabilities, serverCharset));
                break;
            case RESPONSE_EOF:
                if (capabilities.contains(CapabilityFlags.CLIENT_PLUGIN_AUTH)) {
                    decodeAuthSwitchRequest(sequenceId, packet, out);
                } else {
                    out.add(decodeEOFResponse(sequenceId, packet, capabilities));
                }
                break;
            case RESPONSE_ERROR:
                out.add(decodeErrorResponse(sequenceId, packet, serverCharset));
                break;
            case 1:
                // TODO Decode auth more data packet:
                // https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::AuthMoreData
                throw new UnsupportedOperationException("Implement auth more data");
            default:
                decodeHandshake(packet, out, header);
        }
    }

    private void decodeAuthSwitchRequest(int sequenceId, ByteBuf packet, List<Object> out) {
        // TODO Implement AuthSwitchRequest decode
        throw new UnsupportedOperationException("Implement decodeAuthSwitchRequest decode.");
    }

    private void decodeHandshake(ByteBuf packet, List<Object> out, int protocolVersion) {
        if (protocolVersion < MINIMUM_SUPPORTED_PROTOCOL_VERSION) {
            throw new CodecException("Unsupported version of MySQL");
        }

        final Handshake.Builder builder = Handshake.builder();
        builder.protocolVersion(protocolVersion)
                .serverVersion(CodecUtils.readNullTerminatedString(packet))
                .connectionId(packet.readIntLE())
                .addAuthData(packet, Constants.AUTH_PLUGIN_DATA_PART1_LEN);

        packet.skipBytes(1); // Skip auth plugin data terminator
        builder.addCapabilities(
                CodecUtils.toEnumSet(CapabilityFlags.class, packet.readUnsignedShortLE()));
        if (packet.isReadable()) {
            builder.characterSet(MySQLCharacterSet.findById(packet.readByte()))
                    .addServerStatus(CodecUtils.readShortEnumSet(packet, ServerStatusFlag.class))
                    .addCapabilities(
                            CodecUtils.toEnumSet(
                                    CapabilityFlags.class,
                                    packet.readUnsignedShortLE() << Short.SIZE));
            if (builder.hasCapability(CapabilityFlags.CLIENT_RESERVED2)) {
                final int authDataLen = packet.readByte();

                packet.skipBytes(Constants.HANDSHAKE_RESERVED_BYTES); // Skip reserved bytes
                final int readableBytes =
                        Math.max(
                                Constants.AUTH_PLUGIN_DATA_PART2_MIN_LEN,
                                authDataLen - Constants.AUTH_PLUGIN_DATA_PART1_LEN);
                builder.addAuthData(packet, readableBytes);
                if (builder.hasCapability(CapabilityFlags.CLIENT_PLUGIN_AUTH)
                        && packet.isReadable()) {
                    int len = packet.readableBytes();
                    if (packet.getByte(packet.readerIndex() + len - 1) == 0) {
                        len--;
                    }
                    builder.authPluginName(
                            CodecUtils.readFixedLengthString(packet, len, CharsetUtil.UTF_8));
                    packet.skipBytes(1);
                }
            }
        }
        final Handshake handshake = builder.build();
        out.add(handshake);
    }
}
