package com.github.trganda.codec.packets;

import com.github.trganda.codec.ReplicationEventPayload;

public class RotateEventPayload implements ReplicationEventPayload {
  private final long pos;
  private final String filename;

  public RotateEventPayload(long pos, String filename) {
    this.pos = pos;
    this.filename = filename;
  }

  public long getPos() {
    return pos;
  }

  public String getFilename() {
    return filename;
  }
}
