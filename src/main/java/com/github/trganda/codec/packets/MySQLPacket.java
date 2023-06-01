package com.github.trganda.codec.packets;

import com.github.trganda.codec.Visitable;

public interface MySQLPacket extends Visitable {
    int getSequenceId();
}
