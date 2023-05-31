package com.github.trganda.codec.packets;

public class LoadInFileContentPacket extends AbstractMySQLPacket implements MySQLClientPacket {
  private int sequenceId;
  private String content;

  public LoadInFileContentPacket(int sequenceId, String content) {
    super(sequenceId);
    this.content = content;
  }

  public String getContent() {
    return content;
  }
}
