package com.github.trganda.codec.encoder;

import com.github.trganda.codec.packets.MySQLPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * An abstract encoder for sending mysql protocol packet
 *
 * @param <T> payload type object
 */
public abstract class AbstractPacketEncoder<T extends MySQLPacket> extends MessageToByteEncoder<T> {

    @Override
    protected final void encode(ChannelHandlerContext ctx, T packet, ByteBuf buf) throws Exception {
        final int writerIdx = buf.writerIndex();
        // advance the writer index, so we can set the packet length after encoding
        buf.writeInt(0);
        encodePacket(ctx, packet, buf);
        final int len = buf.writerIndex() - writerIdx - 4;
        buf.setMediumLE(writerIdx, len).setByte(writerIdx + 3, packet.getSequenceId());
    }

    protected abstract void encodePacket(ChannelHandlerContext ctx, T packet, ByteBuf buf)
            throws Exception;
}
