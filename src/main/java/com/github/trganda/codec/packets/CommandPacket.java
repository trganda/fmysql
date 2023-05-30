package com.github.trganda.codec.packets;

import com.github.trganda.codec.MySQLClientPacket;
import com.github.trganda.codec.constants.Command;

/** */
public class CommandPacket extends AbstractMySQLPacket implements MySQLClientPacket {

  private final Command command;

  public CommandPacket(int sequenceId, Command command) {
    super(sequenceId);
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }
}
