package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLServerPacketVisitor;
import io.netty.channel.ChannelHandlerContext;

public class StatisticsResponse extends AbstractMySQLPacket implements MySQLServerPacket {

  private final String statString;

  public StatisticsResponse(int sequenceId, String statString) {
    super(sequenceId);
    this.statString = statString;
  }

  @Override
  public void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
    visitor.visit(this, ctx);
  }

  public String getStatString() {
    return statString;
  }
}
