package com.github.trganda.dser;

import com.github.trganda.FakeServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SerialTest {

    private static FakeServer server;

    private static int port = 3306;

    @BeforeAll
    public static void startServer() {
        server = new FakeServer(port);
    }

    @AfterAll
    public static void stopServer() {
        server.close();
    }

    @Test
    public void serialTest() throws ClassNotFoundException, SQLException {
        String driver = "com.mysql.cj.jdbc.Driver";
        String DB_URL =
            "jdbc:mysql://127.0.0.1:"
                + port
                + "/test?autoDeserialize=true&queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor";
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(DB_URL, server.getUser(), server.getPassword());
    }
}
