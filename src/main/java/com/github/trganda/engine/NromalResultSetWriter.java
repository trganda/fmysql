// package com.github.trganda.engine;
//
// import com.github.trganda.codec.*;
// import io.netty.channel.ChannelHandlerContext;
//
// import java.util.List;
//
// public class NromalResultSetWriter implements ResultSetWriter {
//
//  private ChannelHandlerContext ctx;
//  private int[] sequenceId;
//
//  private boolean[] columnsWritten;
//
//  public NromalResultSetWriter(ChannelHandlerContext ctx, int[] sequenceId) {
//    this.ctx = ctx;
//    this.sequenceId = sequenceId;
//    columnsWritten = new boolean[1];
//  }
//
//  public boolean[] getColumnsWritten() {
//    return columnsWritten;
//  }
//
//  @Override
//  public void writeColumns(List<QueryResultColumn> columns) {
//    ctx.write(new ColumnCount(++sequenceId[0], columns.size()));
//    for (QueryResultColumn column : columns) {
//      ColumnType columnType;
//      switch (column.getType()) {
//        default:
//          columnType = ColumnType.MYSQL_TYPE_VAR_STRING;
//          break;
//      }
//      ctx.write(
//          ColumnDefinition.builder()
//              .sequenceId(++sequenceId[0])
//              .catalog("catalog")
//              .schema("schema")
//              .table("table")
//              .orgTable("org_table")
//              .name(column.getName())
//              .orgName(column.getName())
//              .columnLength(10)
//              .type(columnType)
//              .addFlags(ColumnFlag.NUM)
//              .decimals(5)
//              .build());
//    }
//    ctx.write(new EofResponse(++sequenceId[0], 0));
//
//    System.out.println("[mysql-protocol] Columns done");
//
//    columnsWritten[0] = !columns.isEmpty();
//  }
//
//  @Override
//  public void writeRow(List<String> row) {
//    if (++sequenceId[0] % 100 == 0) {
//      ctx.writeAndFlush(new ResultsetRow(sequenceId[0], row.toArray(new String[0])));
//    } else {
//      ctx.write(new ResultsetRow(sequenceId[0], row.toArray(new String[0])));
//    }
//  }
//
//  @Override
//  public void finish() {
//    ctx.writeAndFlush(new EofResponse(++sequenceId[0], 0));
//
//    System.out.println("[mysql-protocol] All done");
//  }
// }
