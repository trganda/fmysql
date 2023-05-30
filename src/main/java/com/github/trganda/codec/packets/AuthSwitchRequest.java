package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLServerPacket;
import com.github.trganda.codec.MySQLServerPacketVisitor;
import io.netty.channel.ChannelHandlerContext;

public class AuthSwitchRequest extends AbstractMySQLPacket implements MySQLServerPacket {

  private final byte[] salt;
  private final String authPluginName;

  public AuthSwitchRequest(int sequenceId, String authPluginName, byte[] salt) {
    super(sequenceId);
    this.authPluginName = authPluginName;
    this.salt = salt;
  }

  @Override
  public void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
    visitor.visit(this, ctx);
  }

  public String getAuthPluginName() {
    return authPluginName;
  }

  public byte[] getAuthPluginData() {
    return this.salt;
  }
}
