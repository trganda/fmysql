package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLPacket;

/**
 * AbstractMySQLPacket
 */
abstract class AbstractMySQLPacket implements MySQLPacket {

  private final int sequenceId;

  public AbstractMySQLPacket(int sequenceId) {
    this.sequenceId = sequenceId;
  }

  @Override
  public int getSequenceId() {
    return sequenceId;
  }
}
