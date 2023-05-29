package com.github.trganda.codec;

import io.netty.channel.ChannelHandlerContext;

public interface Visitable {
  default void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
    throw new IllegalStateException("Failed to accept");
  }
}
