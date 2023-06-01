package com.github.trganda.codec;

import com.github.trganda.codec.auths.Handshake;
import com.github.trganda.codec.packets.*;
import io.netty.channel.ChannelHandlerContext;

public interface MySQLServerPacketVisitor {
    void visit(Handshake handshake, ChannelHandlerContext ctx);

    void visit(OkResponse ok, ChannelHandlerContext ctx);

    void visit(EOFResponse eof, ChannelHandlerContext ctx);

    void visit(AuthSwitchRequest swi, ChannelHandlerContext ctx);

    void visit(StatisticsResponse stat, ChannelHandlerContext ctx);

    void visit(ErrorResponse error, ChannelHandlerContext ctx);

    void visit(ReplicationEvent repEvent, ChannelHandlerContext ctx);
}
