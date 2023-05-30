package com.github.trganda.codec;

import static com.github.trganda.codec.constants.Constants.LOAD_LOCAL_IN_FILE_RESPONSE_FLAG;

public class LoadInFileResponse extends AbstractMySQLPacket implements MySQLServerPacket {

  private byte flag;

  private String file;

  public LoadInFileResponse(Builder builder) {
    super(builder.sequenceId);
    this.flag = builder.flag;
    this.file = builder.file;
  }

  public byte getFlag() {
    return flag;
  }

  public String getFile() {
    return file;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int sequenceId;

    private byte flag;

    private String file;

    public Builder sequenceId(int sequenceId) {
      this.sequenceId = sequenceId;
      return this;
    }

    public Builder flag() {
      this.flag = LOAD_LOCAL_IN_FILE_RESPONSE_FLAG;
      return this;
    }

    public Builder filename(String file) {
      this.file = file;
      return this;
    }

    public LoadInFileResponse build() {
      return new LoadInFileResponse(this);
    }
  }
}
