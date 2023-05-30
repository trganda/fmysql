package com.github.trganda.codec.auths;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

abstract class AbstractAuthPluginDataBuilder<B extends AbstractAuthPluginDataBuilder>
    extends AbstractCapabilitiesBuilder<B> {
  protected final ByteBuf authPluginData = Unpooled.buffer();

  public B addAuthData(byte[] bytes) {
    authPluginData.writeBytes(bytes);
    return (B) this;
  }

  public B addAuthData(ByteBuf buf, int length) {
    authPluginData.writeBytes(buf, length);
    return (B) this;
  }
}
