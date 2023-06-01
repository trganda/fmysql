package com.github.trganda.codec.packets;

import com.github.trganda.codec.constants.Command;

public class QueryCommand extends CommandPacket {

    private final String query;
    private final String database;
    private final String userName;
    private final byte[] scramble411;

    public QueryCommand(
        int sequenceId, String query, String database, String userName, byte[] scramble411) {
        super(sequenceId, Command.COM_QUERY);
        this.query = query;
        this.database = database;
        this.userName = userName;
        this.scramble411 = scramble411;
    }

    public String getQuery() {
        return query;
    }

    public String getDatabase() {
        return database;
    }

    public String getUserName() {
        return userName;
    }

    public byte[] getScramble411() {
        return scramble411;
    }
}
