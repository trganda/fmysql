package com.github.trganda.codec.packets;

public class EmptyPacket extends AbstractMySQLPacket implements MySQLClientPacket {

    public EmptyPacket(int sequenceId) {
        super(sequenceId);
    }
}
