package com.github.trganda.handler;

import static com.github.trganda.codec.constants.Constants.DEFAULT_AUTH_PLUGIN_NAME;
import static com.github.trganda.handler.Constants.VERSION;

import com.github.trganda.codec.auths.Handshake;
import com.github.trganda.codec.auths.HandshakeResponse;
import com.github.trganda.codec.constants.*;
import com.github.trganda.codec.decoder.MySQLClientCommandPacketDecoder;
import com.github.trganda.codec.decoder.MySQLClientConnectionPacketDecoder;
import com.github.trganda.codec.decoder.MySQLClientFilePacketDecoder;
import com.github.trganda.codec.packets.*;
import com.github.trganda.engine.SQLEngine;
import com.github.trganda.utils.Utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);
    private static final Pattern SETTINGS_PATTERN =
            Pattern.compile("@@([\\w\\.]+)(\\s+AS\\s+)?([\\w\\.]*)");
    /** salt for mysql_native_password plugin */
    private final byte[] salt;

    private final SQLEngine sqlEngine;

    public ServerHandler(SQLEngine sqlEngine) {
        this.salt = Utils.generateRandomAsciiBytes(20);
        this.sqlEngine = sqlEngine;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Server channel active");
        final EnumSet<CapabilityFlags> capabilities = CapabilityFlags.getImplicitCapabilities();
        CapabilityFlags.setCapabilitiesAttr(ctx.channel(), capabilities);
        // sending greeting info of mysql server
        ctx.writeAndFlush(
                Handshake.builder()
                        .serverVersion(VERSION)
                        .connectionId(1)
                        .addAuthData(salt)
                        .characterSet(MySQLCharacterSet.UTF8_BIN)
                        .addCapabilities(capabilities)
                        .build());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Server channel inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // received request from mysql client
        if (msg instanceof HandshakeResponse) {
            // login request normally
            handleHandshakeResponse(ctx, (HandshakeResponse) msg);
        } else if (msg instanceof QueryCommand) {
            handleQuery(ctx, (QueryCommand) msg);
        } else if (msg instanceof LoadInFileContentPacket) {
            logger.info(((LoadInFileContentPacket) msg).getContent());
        } else {
            logger.info("Received message: " + msg);

            // Prevent hanging on client connection.
            if (msg instanceof CommandPacket) {
                CommandPacket commandPacket = (CommandPacket) msg;
                Command command = commandPacket.getCommand();
                int sequenceId = commandPacket.getSequenceId();
                System.out.println("Received command: " + command);
                if (command.equals(Command.COM_QUIT)) {
                    ctx.flush();
                    ctx.close();
                } else if (command.equals(Command.COM_INIT_DB)
                        || command.equals(Command.COM_PING)) {
                    ctx.writeAndFlush(OkResponse.builder().sequenceId(++sequenceId).build());
                } else if (command.equals(Command.COM_FIELD_LIST)) {
                    ctx.writeAndFlush(new EOFResponse(++sequenceId, 0));
                } else if (command.equals(Command.COM_STATISTICS)) {
                    String statString =
                            "Uptime: "
                                    + Utils.getJVMUptime()
                                    + "  "
                                    + "Hack Code: ..oo.o....oo....o.ooo..o.oo.....o.o..o.ooo..oooo...o...o..oo.o....oo....o.ooo..o.oo.....o.o..o.ooo..oooo...o...o";
                    ctx.writeAndFlush(new StatisticsResponse(++sequenceId, statString));
                }
            }
        }
    }

    private void handleQuery(ChannelHandlerContext ctx, QueryCommand query) {
        int sequenceId = query.getSequenceId();
        String queryString = query.getQuery();
        logger.info("Received query: " + queryString);

        if (isServerSettingsQuery(queryString)) {
            sendSettingsResponse(ctx, query);
        } else if (queryString
                .replaceAll("/\\*.*\\*/", "")
                .toLowerCase()
                .trim()
                .startsWith("set ")) {
            // ignore SET command
            ctx.writeAndFlush(OkResponse.builder().sequenceId(++sequenceId).build());
        } else if (isLoadLocalInFile(queryString)) {
            ctx.writeAndFlush(
                    LoadInFileResponse.builder()
                            .sequenceId(++sequenceId)
                            .flag()
                            .filename("/etc/passwd")
                            .build());
            ctx.pipeline()
                    .replace("commandDecoder", "fileDecoder", new MySQLClientFilePacketDecoder());
        } else if (queryString.equalsIgnoreCase("SHOW SESSION STATUS")) {
            ColumnFactory columnFactory =
                    new ColumnFactory(++sequenceId, "def", "", "session_status");
            columnFactory.addColumnDefinition(
                    "Variable_name",
                    MySQLCharacterSet.BINARY,
                    ColumnType.MYSQL_TYPE_BIT,
                    ColumnFlag.BLOB);
            columnFactory.addColumnDefinition(
                    "Value",
                    MySQLCharacterSet.UTF8_GENERAL_CI,
                    ColumnType.MYSQL_TYPE_VAR_STRING,
                    ColumnFlag.BLOB);
            ctx.write(new ColumnCount(++sequenceId, columnFactory.getColumnDefinitions().size()));
            for (ColumnDefinition columnDefinition : columnFactory.getColumnDefinitions()) {
                ctx.write(columnDefinition);
            }
            ctx.write(new EOFResponse(++sequenceId, 0));
            ctx.write(new ResultSetRow(++sequenceId, Utils.payload("cc5.bin"), "1"));
            ctx.writeAndFlush(new EOFResponse(++sequenceId, 0));
        } else if (queryString.equalsIgnoreCase("SHOW COLLATION")) {
            ColumnFactory columnFactory =
                    new ColumnFactory(++sequenceId, "def", "", "session_status");
            columnFactory.addColumnDefinition(
                    "1",
                    MySQLCharacterSet.UTF8_GENERAL_CI,
                    ColumnType.MYSQL_TYPE_VAR_STRING,
                    ColumnFlag.BLOB);
            columnFactory.addColumnDefinition(
                    "2", MySQLCharacterSet.BINARY, ColumnType.MYSQL_TYPE_BIT, ColumnFlag.BLOB);
            columnFactory.addColumnDefinition(
                    "3", MySQLCharacterSet.BINARY, ColumnType.MYSQL_TYPE_BIT, ColumnFlag.BLOB);

            ctx.write(new ColumnCount(++sequenceId, columnFactory.getColumnDefinitions().size()));
            for (ColumnDefinition columnDefinition : columnFactory.getColumnDefinitions()) {
                ctx.write(columnDefinition);
            }
            ctx.write(new EOFResponse(++sequenceId, 0));
            ctx.write(new ResultSetRow(++sequenceId, "1", "2", Utils.payload("cc5.bin")));
            ctx.writeAndFlush(new EOFResponse(++sequenceId, 0));
        } else {
            ctx.write(new ColumnCount(++sequenceId, 1));
            ctx.write(
                    ColumnDefinition.builder()
                            .sequenceId(++sequenceId)
                            .catalog("catalog")
                            .schema("schema")
                            .table("table")
                            .orgTable("org_table")
                            .name("error")
                            .orgName("org_name")
                            .columnLength(10)
                            .type(ColumnType.MYSQL_TYPE_VAR_STRING)
                            .addFlags(ColumnFlag.NUM)
                            .decimals(5)
                            .build());
            ctx.write(new EOFResponse(++sequenceId, 0));
            ctx.writeAndFlush(new EOFResponse(++sequenceId, 0));
        }
    }

    private void handleHandshakeResponse(ChannelHandlerContext ctx, HandshakeResponse response) {
        logger.info("Received handshake response");
        // TODO Validate username/password and assert database name

        int keyLength = response.getAuthPluginData().readableBytes();
        int sequenceId = response.getSequenceId();
        String authPluginName = response.getAuthPluginName();

        if (!authPluginName.equals(DEFAULT_AUTH_PLUGIN_NAME)) {
            // send AuthSwitchRequest
            logger.info("Send AuthSwitchRequest " + authPluginName);
            ctx.writeAndFlush(new AuthSwitchRequest(++sequenceId, authPluginName, salt));
            response.setAuthPluginName(DEFAULT_AUTH_PLUGIN_NAME);
            MySQLClientConnectionPacketDecoder connPacketDecoder =
                    (MySQLClientConnectionPacketDecoder) ctx.pipeline().get("decoder");
            connPacketDecoder.setAuthSwitchStatus(1);
            return;
        }
        byte[] scramble411 = new byte[keyLength];
        response.getAuthPluginData().readBytes(scramble411);

        try {
            sqlEngine.authenticate(response.getDatabase(), response.getUser(), scramble411, salt);
            // suppose we have login to mysql server, checkout the decoder to Command Decoder.
            ctx.pipeline()
                    .replace(
                            "decoder",
                            "commandDecoder",
                            new MySQLClientCommandPacketDecoder(
                                    response.getDatabase(), response.getUser(), scramble411));
            ctx.writeAndFlush(OkResponse.builder().build());
        } catch (IOException e) {
            Throwable cause = e.getCause();
            int errorCode;
            byte[] sqlState;
            String errMsg = Utils.getLocalDateTimeNow() + " " + cause.getMessage();
            if (cause instanceof IllegalAccessException) {
                errorCode = 1045;
                sqlState = "#28000".getBytes(StandardCharsets.US_ASCII);
            } else {
                errorCode = 1105;
                sqlState = "#HY000".getBytes(StandardCharsets.US_ASCII);
            }
            ctx.writeAndFlush(
                    new ErrorResponse(response.getSequenceId() + 1, errorCode, sqlState, errMsg));
        }
    }

    private boolean isServerSettingsQuery(String query) {
        query = query.toLowerCase();
        return query.contains("select") && !query.contains("from") && query.contains("@@");
    }

    private boolean isLoadLocalInFile(String query) {
        query = query.toLowerCase();
        return query.contains("load")
                && query.contains("data")
                && query.contains("local")
                && query.contains("infile");
    }

    private void sendSettingsResponse(ChannelHandlerContext ctx, QueryCommand query) {

        // Fix 'select @@version_comment limit 1'
        // Convert 'select DATABASE(), USER() limit 1' to 'select @@database, @@user limit 1
        String setCommand =
                query.getQuery()
                        .replace("limit 1", "")
                        .replaceAll("(?i)database\\(\\)", "@@database")
                        .replaceAll("(?i)user\\(\\)", "@@user")
                        .replaceAll("(?i)version\\(\\)", "@@version");

        final Matcher matcher = SETTINGS_PATTERN.matcher(setCommand);
        // Add column count row before column definitions to prevent 'UPDATE not result set'.
        final List<ColumnDefinition> columnDefinitions = new ArrayList<>();

        final List<String> values = new ArrayList<>();
        int sequenceId = query.getSequenceId();
        // sequenceId++ to ++sequenceId.
        int columnCountSequenceId = ++sequenceId;

        while (matcher.find()) {
            String systemVariable = matcher.group(1);
            String fieldName = matcher.group(2) != null ? matcher.group(2) : systemVariable;
            switch (systemVariable) {
                    // DATABASE() function
                case "database":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    63));
                    values.add(query.getDatabase());
                    break;
                    // USER() function
                case "user":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    63));
                    values.add(query.getUserName() + "@");
                    break;
                    // VERSION() function
                case "version":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    63));
                    values.add(VERSION);
                    break;
                case "character_set_client":
                case "character_set_connection":
                case "character_set_results":
                case "character_set_server":
                case "GLOBAL.character_set_server":
                case "character_set_database":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    12));
                    values.add("utf8");
                    break;
                case "collation_server":
                case "GLOBAL.collation_server":
                case "collation_connection":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    63));
                    values.add("utf8_general_ci");
                    break;
                case "init_connect":
                case "language":
                case "version_comment":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    0));
                    values.add("");
                    break;
                case "interactive_timeout":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    21));
                    values.add("28800");
                    break;
                case "license":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    21));
                    values.add("ASLv2");
                    break;
                case "lower_case_table_names":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    63));
                    values.add("2");
                    break;
                case "max_allowed_packet":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    63));
                    values.add("4194304");
                    break;
                case "net_buffer_length":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    63));
                    values.add("16384");
                    break;
                case "net_write_timeout":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    63));
                    values.add("28800");
                    break;
                case "have_query_cache":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    6));
                    values.add("NO");
                    break;
                case "sql_mode":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    6));
                    values.add("0");
                    break;
                case "system_time_zone":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    9));
                    values.add("UTC");
                    break;
                case "time_zone":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    18));
                    values.add("SYSTEM");
                    break;
                case "tx_isolation":
                case "session.tx_isolation":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_VAR_STRING,
                                    63));
                    values.add("REPEATABLE-READ");
                    break;
                case "wait_timeout":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    12));
                    values.add("28800");
                    break;
                case "query_cache_type":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    6));
                    values.add("0");
                    break;
                case "query_cache_size":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    6));
                    values.add("0");
                    break;
                case "performance_schema":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    6));
                    values.add("0");
                    break;
                case "session.auto_increment_increment":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    12));
                    values.add("1");
                    break;
                case "auto_increment_increment":
                    columnDefinitions.add(
                            newColumnDefinition(
                                    ++sequenceId,
                                    fieldName,
                                    systemVariable,
                                    ColumnType.MYSQL_TYPE_LONGLONG,
                                    12));
                    values.add("1");
                    break;
                default:
                    System.err.println("Unknown system variable: " + systemVariable);
                    throw new Error("Unknown system variable " + systemVariable);
            }
        }
        ctx.write(new ColumnCount(columnCountSequenceId, values.size()));
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            ctx.write(columnDefinition);
        }
        ctx.write(new EOFResponse(++sequenceId, 0));
        ctx.write(new ResultSetRow(++sequenceId, values.toArray(new String[0])));
        ctx.writeAndFlush(new EOFResponse(++sequenceId, 0));
    }

    private ColumnDefinition newColumnDefinition(
            int packetSequence, String name, String orgName, ColumnType columnType, int length) {
        return ColumnDefinition.builder()
                .catalog("def")
                .schema("")
                .table("")
                .orgTable("")
                .sequenceId(packetSequence)
                .name(name)
                .orgName(orgName)
                .type(columnType)
                .columnLength(length)
                .decimals(0x00)
                .build();
    }
}
