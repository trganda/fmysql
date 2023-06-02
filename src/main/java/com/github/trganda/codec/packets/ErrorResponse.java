package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLServerPacketVisitor;

import io.netty.channel.ChannelHandlerContext;

/** This packet indicates that an error occurred. */
public class ErrorResponse extends AbstractMySQLPacket implements MySQLServerPacket {

    private final int errorNumber;
    private final byte[] sqlState;
    private final String message;

    public ErrorResponse(int sequenceId, int errorNumber, byte[] sqlState, String message) {
        super(sequenceId);
        this.errorNumber = errorNumber;
        this.sqlState = sqlState;
        this.message = message;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public byte[] getSqlState() {
        return sqlState;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
        visitor.visit(this, ctx);
    }
}
