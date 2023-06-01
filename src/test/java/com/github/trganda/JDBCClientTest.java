/*
 * Copyright 2022 paxos.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.trganda;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class JDBCClientTest {

    private static TestServer server;

    @BeforeAll
    public static void startServer() {
        int port = 3306;
        server = new TestServer(port);
    }

    @AfterAll
    public static void stopServer() {
        server.close();
    }

    @Test
    void selfConnect() throws Exception {
        // Raise a connection to the servers
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn =
                 DriverManager.getConnection(
                     "jdbc:mysql://localhost:" + server.getPort() + "/test",
                     server.getUser(),
                     server.getPassword())) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT 1")) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1));
                    }
                }
            }
        }
    }
}
