package com.github.trganda.engine;

import java.io.IOException;

/** An interface to callback events received from the MySQL server. */
public interface SQLEngine {
  /**
   * Authenticating the user and password.
   *
   * @param database Database name
   * @param userName User name
   * @param scramble411 Encoded password
   * @param authSeed Encoding seed
   * @throws IOException Thrown with IllegalAccessException as the inner cause if the authentication
   *     is failed
   */
  void authenticate(String database, String userName, byte[] scramble411, byte[] authSeed)
      throws IOException;

  /**
   * Querying the SQL.
   *
   * @param resultSetWriter Response writer
   * @param database Database name
   * @param userName User name
   * @param scramble411 Encoded password
   * @param authSeed Encoding seed
   * @param sql SQL text
   * @throws IOException Thrown with IllegalAccessException as the inner cause if the
   *     authentication/authorization is failed, or IllegalArgumentException if SQL is invalid
   */
  void query(
      ResultSetWriter resultSetWriter,
      String database,
      String userName,
      byte[] scramble411,
      byte[] authSeed,
      String sql)
      throws IOException;
}
