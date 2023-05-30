package com.github.trganda.codec;

import com.github.trganda.codec.packets.ReplicationEventHeader;
import io.netty.channel.ChannelHandlerContext;

public interface ReplicationEvent extends Visitable {
  ReplicationEventHeader header();

  ReplicationEventPayload payload();

  @Override
  default void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
    visitor.visit(this, ctx);
  }
}
