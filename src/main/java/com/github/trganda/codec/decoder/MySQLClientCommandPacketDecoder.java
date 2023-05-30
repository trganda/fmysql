package com.github.trganda.codec.decoder;

import com.github.trganda.codec.CodecUtils;
import com.github.trganda.codec.QueryCommand;
import com.github.trganda.codec.constants.Command;
import com.github.trganda.codec.constants.Constants;
import com.github.trganda.codec.constants.MySQLCharacterSet;
import com.github.trganda.codec.packets.CommandPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.Optional;

public class MySQLClientCommandPacketDecoder extends AbstractPacketDecoder
    implements MySQLClientPacketDecoder {

  private String database;
  private String userName;
  private byte[] scramble411;

  public MySQLClientCommandPacketDecoder() {
    this(Constants.DEFAULT_MAX_PACKET_SIZE);
  }

  public MySQLClientCommandPacketDecoder(int maxPacketSize) {
    super(maxPacketSize);
  }

  public MySQLClientCommandPacketDecoder(String database, String userName, byte[] scramble411) {
    this(Constants.DEFAULT_MAX_PACKET_SIZE, database, userName, scramble411);
  }

  public MySQLClientCommandPacketDecoder(
      int maxPacketSize, String database, String userName, byte[] scramble411) {
    super(maxPacketSize);

    this.database = database;
    this.userName = userName;
    this.scramble411 = scramble411;
  }

  @Override
  protected void decodePayload(
      ChannelHandlerContext ctx, int sequenceId, ByteBuf packet, List<Object> out) {
    final MySQLCharacterSet clientCharset = MySQLCharacterSet.getClientCharsetAttr(ctx.channel());

    final byte commandCode = packet.readByte();
    final Optional<Command> command = Command.findByCommandCode(commandCode);
    if (!command.isPresent()) {
      throw new DecoderException("Unknown command " + commandCode);
    }
    switch (command.get()) {
      // currently only support COM_QUERY
      case COM_QUERY:
        out.add(
            new QueryCommand(
                sequenceId,
                CodecUtils.readFixedLengthString(
                    packet, packet.readableBytes(), clientCharset.getCharset()),
                database,
                userName,
                scramble411));
        break;
      default:
        out.add(new CommandPacket(sequenceId, command.get()));
    }
  }
}
