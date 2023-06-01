package com.github.trganda.codec.decoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.auths.HandshakeResponse;
import com.github.trganda.codec.constants.CapabilityFlags;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** Decoder for handshake of mysql connection from client */
public class MySQLClientConnectionPacketDecoder extends AbstractPacketDecoder
        implements MySQLClientPacketDecoder {

    // receive AuthSwitchResponse while status is 1, otherwise receive HandshakeResponse
    private int authSwitchStatus = 0;
    // keep HandshakeResponse on init handshake
    private HandshakeResponse handshakeResponse;

    public MySQLClientConnectionPacketDecoder() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public MySQLClientConnectionPacketDecoder(int maxPacketSize) {
        super(maxPacketSize);
    }

    @Override
    protected void decodePayload(
            ChannelHandlerContext ctx, int sequenceId, ByteBuf packet, List<Object> out) {
        // if status is 0, need to processing login request from mysql client
        if (authSwitchStatus == 0) {
            final EnumSet<CapabilityFlags> clientCapabilities =
                    CodecUtils.readIntEnumSet(packet, CapabilityFlags.class);

            if (!clientCapabilities.contains(CapabilityFlags.CLIENT_PROTOCOL_41)) {
                throw new DecoderException("MySQL client protocol 4.1 support required");
            }

            final HandshakeResponse.Builder builder = HandshakeResponse.create();
            builder.addCapabilities(clientCapabilities)
                    .maxPacketSize((int) packet.readUnsignedIntLE());

            final MySQLCharacterSet characterSet = MySQLCharacterSet.findById(packet.readByte());
            builder.characterSet(characterSet);
            packet.skipBytes(23);

            if (packet.isReadable()) {
                // username
                builder.user(
                        CodecUtils.readNullTerminatedString(packet, characterSet.getCharset()));

                final EnumSet<CapabilityFlags> serverCapabilities =
                        CapabilityFlags.getCapabilitiesAttr(ctx.channel());
                final EnumSet<CapabilityFlags> capabilities = EnumSet.copyOf(clientCapabilities);
                capabilities.retainAll(serverCapabilities);

                final int authResponseLength;
                if (capabilities.contains(CapabilityFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
                    authResponseLength = (int) CodecUtils.readLengthEncodedInteger(packet);
                } else if (capabilities.contains(CapabilityFlags.CLIENT_RESERVED2)) {
                    authResponseLength = packet.readUnsignedByte();
                } else {
                    authResponseLength = CodecUtils.findNullTermLen(packet);
                }
                // password
                builder.addAuthData(packet, authResponseLength);

                if (capabilities.contains(CapabilityFlags.CLIENT_CONNECT_WITH_DB)) {
                    // database name
                    builder.database(
                            CodecUtils.readNullTerminatedString(packet, characterSet.getCharset()));
                }

                if (capabilities.contains(CapabilityFlags.CLIENT_PLUGIN_AUTH)) {
                    // auth plugin name
                    builder.authPluginName(
                            CodecUtils.readNullTerminatedString(packet, StandardCharsets.UTF_8));
                }

                if (capabilities.contains(CapabilityFlags.CLIENT_CONNECT_ATTRS)) {
                    final long keyValueLen = CodecUtils.readLengthEncodedInteger(packet);
                    for (int i = 0; i < keyValueLen; i++) {
                        builder.addAttribute(
                                CodecUtils.readLengthEncodedString(packet, StandardCharsets.UTF_8),
                                CodecUtils.readLengthEncodedString(packet, StandardCharsets.UTF_8));
                    }
                }
            }
            HandshakeResponse response = builder.build();
            this.handshakeResponse = response;
            out.add(response);
        } else {
            // receive AuthSwitchResponse after AuthSwitchRequest send to client
            Objects.requireNonNull(this.handshakeResponse, "handshakeResponse is null");
            this.handshakeResponse.setSequenceId(sequenceId);
            this.handshakeResponse.getAuthPluginData().clear();
            this.handshakeResponse.getAuthPluginData().writeBytes(packet, packet.readableBytes());
            out.add(handshakeResponse);
        }
    }

    public void setAuthSwitchStatus(int status) {
        this.authSwitchStatus = status;
    }
}
