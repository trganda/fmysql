package com.github.trganda.file;

import com.github.trganda.FakeServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoadLocalFile {

    private static FakeServer server;

    private static final int port = 3306;

    @BeforeAll
    public static void startServer() {
        server = new FakeServer(port);
    }

    @AfterAll
    public static void stopServer() {
        server.close();
    }

    @Test
    void loadLocalFileTest() throws Exception {
        // Raise a connection to the servers
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn =
                 DriverManager.getConnection(
                     "jdbc:mysql://localhost:"
                         + server.getPort()
                         + "/test?allowUrlInLocalInfile=true&allowLoadLocalInfile=true",
                     server.getUser(),
                     server.getPassword())) {
            Statement stmt = conn.createStatement();
            int rowsAffected =
                stmt.executeUpdate(
                    "load data local infile \"/etc\" into table fool FIELDS TERMINATED BY '\\n'");
            System.out.println(rowsAffected + " row(s) affected.");
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT 1");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
