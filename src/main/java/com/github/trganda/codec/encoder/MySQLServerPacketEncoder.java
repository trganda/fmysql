package com.github.trganda.codec.encoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.auths.Handshake;
import com.github.trganda.codec.constants.CapabilityFlags;
import com.github.trganda.codec.constants.Constants;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.constants.ServerStatusFlag;
import com.github.trganda.codec.packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class MySQLServerPacketEncoder extends AbstractPacketEncoder<MySQLServerPacket> {

    @Override
    protected void encodePacket(ChannelHandlerContext ctx, MySQLServerPacket packet, ByteBuf buf) {
        final EnumSet<CapabilityFlags> capabilities =
                CapabilityFlags.getCapabilitiesAttr(ctx.channel());
        final Charset serverCharset =
                MySQLCharacterSet.getServerCharsetAttr(ctx.channel()).getCharset();
        if (packet instanceof ColumnCount) {
            encodeColumnCount((ColumnCount) packet, buf);
        } else if (packet instanceof ColumnDefinition) {
            encodeColumnDefinition(serverCharset, (ColumnDefinition) packet, buf);
        } else if (packet instanceof EOFResponse) {
            encodeEOFResponse(capabilities, (EOFResponse) packet, buf);
        } else if (packet instanceof Handshake) {
            encodeHandshake((Handshake) packet, buf);
        } else if (packet instanceof AuthSwitchRequest) {
            encodeAuthSwitchRequest((AuthSwitchRequest) packet, buf);
        } else if (packet instanceof StatisticsResponse) {
            encodeStatisticsResponse((StatisticsResponse) packet, buf);
        } else if (packet instanceof OkResponse) {
            encodeOkResponse(capabilities, serverCharset, (OkResponse) packet, buf);
        } else if (packet instanceof ErrorResponse) {
            encodeErrorResponse(capabilities, serverCharset, (ErrorResponse) packet, buf);
        } else if (packet instanceof ResultSetRow) {
            encodeResultSetRow(serverCharset, (ResultSetRow) packet, buf);
        } else if (packet instanceof LoadInFileResponse) {
            encodeLoadInFileResponse(serverCharset, (LoadInFileResponse) packet, buf);
        } else {
            String msg = "Unknown packet type: " + packet.getClass();
            System.out.println(msg);
            throw new IllegalStateException(msg);
        }
    }

    protected void encodeColumnCount(ColumnCount columnCount, ByteBuf buf) {
        CodecUtils.writeLengthEncodedInt(buf, (long) columnCount.getFieldCount());
    }

    protected void encodeColumnDefinition(
            Charset serverCharset, ColumnDefinition packet, ByteBuf buf) {
        CodecUtils.writeLengthEncodedString(buf, packet.getCatalog(), serverCharset);
        CodecUtils.writeLengthEncodedString(buf, packet.getSchema(), serverCharset);
        CodecUtils.writeLengthEncodedString(buf, packet.getTable(), serverCharset);
        CodecUtils.writeLengthEncodedString(buf, packet.getOrgTable(), serverCharset);
        CodecUtils.writeLengthEncodedString(buf, packet.getName(), serverCharset);
        CodecUtils.writeLengthEncodedString(buf, packet.getOrgName(), serverCharset);
        buf.writeByte(0x0c);
        buf.writeShortLE(packet.getCharacterSet().getId())
                .writeIntLE((int) packet.getColumnLength())
                .writeByte(packet.getType().getValue())
                .writeShortLE((int) CodecUtils.toLong(packet.getFlags()))
                .writeByte(packet.getDecimals())
                .writeShort(0);
        // TODO Add default values for COM_FIELD_LIST
    }

    protected void encodeEOFResponse(
            EnumSet<CapabilityFlags> capabilities, EOFResponse eof, ByteBuf buf) {
        buf.writeByte(0xfe);
        if (capabilities.contains(CapabilityFlags.CLIENT_PROTOCOL_41)) {
            buf.writeShortLE(eof.getWarnings())
                    .writeShortLE((int) CodecUtils.toLong(eof.getStatusFlags()));
        }
    }

    protected void encodeHandshake(Handshake handshake, ByteBuf buf) {
        buf.writeByte(handshake.getProtocolVersion())
                .writeBytes(handshake.getServerVersion().array())
                .writeByte(Constants.NUL_BYTE)
                .writeIntLE(handshake.getConnectionId())
                .writeBytes(handshake.getAuthPluginData(), Constants.AUTH_PLUGIN_DATA_PART1_LEN)
                .writeByte(Constants.NUL_BYTE)
                .writeShortLE((int) CodecUtils.toLong(handshake.getCapabilities()))
                .writeByte(handshake.getCharacterSet().getId())
                .writeShortLE((int) CodecUtils.toLong(handshake.getServerStatus()))
                .writeShortLE((int) (CodecUtils.toLong(handshake.getCapabilities()) >> Short.SIZE));
        if (handshake.getCapabilities().contains(CapabilityFlags.CLIENT_PLUGIN_AUTH)) {
            // add 1 reserved byte at the end
            buf.writeByte(
                    handshake.getAuthPluginData().readableBytes()
                            + Constants.AUTH_PLUGIN_DATA_PART1_LEN
                            + 1);
        } else {
            buf.writeByte(Constants.NUL_BYTE);
        }
        buf.writeZero(Constants.HANDSHAKE_RESERVED_BYTES);
        if (handshake.getCapabilities().contains(CapabilityFlags.CLIENT_SECURE_CONNECTION)) {
            final int padding =
                    Constants.AUTH_PLUGIN_DATA_PART2_MIN_LEN
                            - handshake.getAuthPluginData().readableBytes();
            buf.writeBytes(handshake.getAuthPluginData());
            if (padding > 0) {
                buf.writeZero(padding);
            }
        }
        if (handshake.getCapabilities().contains(CapabilityFlags.CLIENT_PLUGIN_AUTH)) {
            ByteBufUtil.writeUtf8(buf, handshake.getAuthPluginName());
            buf.writeByte(Constants.NUL_BYTE);
        }
    }

    protected void encodeAuthSwitchRequest(AuthSwitchRequest packet, ByteBuf buf) {
        buf.writeByte(0xfe);
        CodecUtils.writeNullTerminatedString(
                buf, packet.getAuthPluginName(), StandardCharsets.UTF_8);
        buf.writeBytes(packet.getAuthPluginData()).writeByte(Constants.NUL_BYTE);
    }

    protected void encodeStatisticsResponse(StatisticsResponse packet, ByteBuf buf) {
        ByteBufUtil.writeUtf8(buf, packet.getStatString());
    }

    protected void encodeOkResponse(
            EnumSet<CapabilityFlags> capabilities,
            Charset serverCharset,
            OkResponse response,
            ByteBuf buf) {
        buf.writeByte(0);
        CodecUtils.writeLengthEncodedInt(buf, response.getAffectedRows());
        CodecUtils.writeLengthEncodedInt(buf, response.getLastInsertId());
        if (capabilities.contains(CapabilityFlags.CLIENT_PROTOCOL_41)) {
            buf.writeShortLE((int) CodecUtils.toLong(response.getStatusFlags()))
                    .writeShortLE(response.getWarnings());

        } else if (capabilities.contains(CapabilityFlags.CLIENT_TRANSACTIONS)) {
            buf.writeShortLE((int) CodecUtils.toLong(response.getStatusFlags()));
        }
        if (capabilities.contains(CapabilityFlags.CLIENT_SESSION_TRACK)) {
            CodecUtils.writeLengthEncodedString(buf, response.getInfo(), serverCharset);
            if (response.getStatusFlags().contains(ServerStatusFlag.SESSION_STATE_CHANGED)) {
                CodecUtils.writeLengthEncodedString(
                        buf, response.getSessionStateChanges(), serverCharset);
            }
        } else {
            if (response.getInfo() != null) {
                buf.writeCharSequence(response.getInfo(), serverCharset);
            }
        }
    }

    protected void encodeErrorResponse(
            EnumSet<CapabilityFlags> capabilities,
            Charset serverCharset,
            ErrorResponse packet,
            ByteBuf buf) {
        buf.writeByte(0xff).writeShortLE(packet.getErrorNumber()).writeBytes(packet.getSqlState());
        ByteBufUtil.writeUtf8(buf, packet.getMessage());
    }

    protected void encodeResultSetRow(Charset serverCharset, ResultSetRow packet, ByteBuf buf) {
        for (Object value : packet.getValues()) {
            if (value instanceof String) {
                CodecUtils.writeLengthEncodedString(buf, (String) value, serverCharset);
            } else if (value instanceof byte[]) {
                CodecUtils.writeLengthEncodedInt(buf, (long) ((byte[]) value).length);
                buf.writeBytes((byte[]) value);
            }
        }
    }

    protected void encodeLoadInFileResponse(
            Charset serverCharset, LoadInFileResponse packet, ByteBuf buf) {
        buf.writeByte(packet.getFlag());
        buf.writeCharSequence(packet.getFile(), serverCharset);
    }
}
