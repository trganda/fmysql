/*
 * Copyright 2022 paxos.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.trganda.codec;

import io.netty.channel.ChannelHandlerContext;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class EOFResponse extends AbstractMySQLPacket implements MySQLServerPacket {

  private final int warnings;
  private final Set<ServerStatusFlag> statusFlags = EnumSet.noneOf(ServerStatusFlag.class);

  public EOFResponse(int sequenceId, int warnings, ServerStatusFlag... flags) {
    super(sequenceId);
    this.warnings = warnings;
    Collections.addAll(statusFlags, flags);
  }

  public EOFResponse(int sequenceId, int warnings, Collection<ServerStatusFlag> flags) {
    super(sequenceId);
    this.warnings = warnings;
    statusFlags.addAll(flags);
  }

  public int getWarnings() {
    return warnings;
  }

  public Set<ServerStatusFlag> getStatusFlags() {
    return statusFlags;
  }

  @Override
  public void accept(MySQLServerPacketVisitor visitor, ChannelHandlerContext ctx) {
    visitor.visit(this, ctx);
  }
}
