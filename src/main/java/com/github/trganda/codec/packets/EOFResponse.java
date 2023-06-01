package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLServerPacketVisitor;
import com.github.trganda.codec.constants.ServerStatusFlag;
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
