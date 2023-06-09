package com.github.trganda.codec.decoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.constants.CapabilityFlags;
import com.github.trganda.codec.constants.ColumnFlag;
import com.github.trganda.codec.constants.ColumnType;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.packets.ColumnCount;
import com.github.trganda.codec.packets.ColumnDefinition;
import com.github.trganda.codec.packets.ResultSetRow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MySQLServerResultSetPacketDecoder extends AbstractPacketDecoder
        implements MySQLServerPacketDecoder {

    enum State {
        COLUMN_COUNT,
        COLUMN_DEFINITION,
        COMPLETE,
        ERROR,
        ROW
    }

    private State state = State.COLUMN_COUNT;
    private List<ColumnDefinition> columnDefinitions;

    public MySQLServerResultSetPacketDecoder() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public MySQLServerResultSetPacketDecoder(int maxPacketSize) {
        super(maxPacketSize);
    }

    @Override
    protected void decodePayload(
            ChannelHandlerContext ctx, int sequenceId, ByteBuf packet, List<Object> out) {
        final Channel channel = ctx.channel();
        final Set<CapabilityFlags> capabilities = CapabilityFlags.getCapabilitiesAttr(channel);
        final Charset serverCharset = MySQLCharacterSet.getServerCharsetAttr(channel).getCharset();

        switch (state) {
            case COLUMN_COUNT:
                handleColumnCount(sequenceId, packet, out, capabilities, serverCharset);
                break;
            case COLUMN_DEFINITION:
                handleColumngDefinition(sequenceId, packet, out, capabilities, serverCharset);
                break;
            case ROW:
                handleRow(sequenceId, packet, out, capabilities, serverCharset);
                break;
            case COMPLETE:
                throw new IllegalStateException(
                        "Received an unexpected packet after decoding a result set");
            case ERROR:
                throw new IllegalStateException("Received a packet while in an error state");
        }
    }

    private void handleColumnCount(
            int sequenceId,
            ByteBuf packet,
            List<Object> out,
            Set<CapabilityFlags> capabilities,
            Charset serverCharset) {
        final int header = packet.readByte() & 0xff;
        if (header == RESPONSE_ERROR) {
            state = State.ERROR;
            out.add(decodeErrorResponse(sequenceId, packet, serverCharset));
        } else if (header == RESPONSE_OK) {
            state = State.COMPLETE;
            out.add(decodeOkResponse(sequenceId, packet, capabilities, serverCharset));
        } else {
            state = State.COLUMN_DEFINITION;
            out.add(decodeFieldCount(sequenceId, packet, header));
        }
    }

    private ColumnCount decodeFieldCount(int sequenceId, ByteBuf packet, int header) {
        final int currentResultSetFieldCount =
                (int) CodecUtils.readLengthEncodedInteger(packet, header);
        if (currentResultSetFieldCount < 0) {
            throw new IllegalStateException("Field count is too large to handle");
        }
        columnDefinitions = new ArrayList<>(currentResultSetFieldCount);
        return new ColumnCount(sequenceId, currentResultSetFieldCount);
    }

    private void handleColumngDefinition(
            int sequenceId,
            ByteBuf packet,
            List<Object> out,
            Set<CapabilityFlags> capabilities,
            Charset serverCharset) {
        final int header = packet.readUnsignedByte();
        if (header == RESPONSE_EOF) {
            state = State.ROW;
            out.add(decodeEOFResponse(sequenceId, packet, capabilities));
        } else {
            final ColumnDefinition columnDefinition =
                    decodeColumnDefinition(sequenceId, packet, header, serverCharset);
            columnDefinitions.add(columnDefinition);
            out.add(columnDefinition);
        }
    }

    private ColumnDefinition decodeColumnDefinition(
            int sequenceId, ByteBuf packet, int header, Charset serverCharset) {
        final ColumnDefinition.Builder builder =
                ColumnDefinition.builder()
                        .sequenceId(sequenceId)
                        .catalog(CodecUtils.readLengthEncodedString(packet, header, serverCharset))
                        .schema(CodecUtils.readLengthEncodedString(packet, serverCharset))
                        .table(CodecUtils.readLengthEncodedString(packet, serverCharset))
                        .orgTable(CodecUtils.readLengthEncodedString(packet, serverCharset))
                        .name(CodecUtils.readLengthEncodedString(packet, serverCharset))
                        .orgName(CodecUtils.readLengthEncodedString(packet, serverCharset));
        packet.readByte();
        builder.characterSet(MySQLCharacterSet.findById(packet.readShortLE()))
                .columnLength(packet.readUnsignedIntLE())
                .type(ColumnType.lookup(packet.readUnsignedByte()))
                .addFlags(CodecUtils.readShortEnumSet(packet, ColumnFlag.class))
                .decimals(packet.readUnsignedByte());
        packet.skipBytes(2);
        return builder.build();
    }

    private void handleRow(
            int sequenceId,
            ByteBuf packet,
            List<Object> out,
            Set<CapabilityFlags> capabilities,
            Charset serverCharset) {
        final int header = packet.readByte() & 0xff;
        switch (header) {
            case RESPONSE_ERROR:
                state = State.ERROR;
                out.add(decodeErrorResponse(sequenceId, packet, serverCharset));
                break;
            case RESPONSE_EOF:
                state = State.COMPLETE;
                out.add(decodeEOFResponse(sequenceId, packet, capabilities));
                break;
            case RESPONSE_OK:
                state = State.COMPLETE;
                decodeOkResponse(sequenceId, packet, capabilities, serverCharset);
                break;
            default:
                decodeRow(sequenceId, packet, header, out);
        }
    }

    private void decodeRow(int sequenceId, ByteBuf packet, int firstByte, List<Object> out) {
        final int size = columnDefinitions.size();
        final String[] values = new String[size];
        values[0] =
                CodecUtils.readLengthEncodedString(
                        packet, firstByte, columnDefinitions.get(0).getCharacterSet().getCharset());
        for (int i = 1; i < size; i++) {
            values[i] =
                    CodecUtils.readLengthEncodedString(
                            packet, columnDefinitions.get(i).getCharacterSet().getCharset());
        }
        out.add(new ResultSetRow(sequenceId, values));
    }
}
