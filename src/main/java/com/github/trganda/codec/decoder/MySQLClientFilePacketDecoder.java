package com.github.trganda.codec.decoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.packets.EmptyPacket;
import com.github.trganda.codec.packets.LoadInFileContentPacket;
import com.github.trganda.codec.packets.OkResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class MySQLClientFilePacketDecoder extends AbstractPacketDecoder
        implements MySQLClientPacketDecoder {

    public MySQLClientFilePacketDecoder() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public MySQLClientFilePacketDecoder(int maxPacketSize) {
        super(maxPacketSize);
    }

    @Override
    protected void decodePayload(
            ChannelHandlerContext ctx, int sequenceId, ByteBuf packet, List<Object> out) {
        MySQLCharacterSet clientCharset = MySQLCharacterSet.getClientCharsetAttr(ctx.channel());

        if (packet.readableBytes() == 0) {
            // send response
            ctx.writeAndFlush(OkResponse.builder().sequenceId(++sequenceId).build());
            ctx.pipeline()
                    .replace(
                            "fileDecoder", "commandDecoder", new MySQLClientCommandPacketDecoder());
            return;
        }

        String contents =
                CodecUtils.readFixedLengthString(
                        packet, packet.readableBytes(), clientCharset.getCharset());

        out.add(new LoadInFileContentPacket(sequenceId, contents));
    }
}
