package com.github.trganda.engine;

import com.github.trganda.utils.SHAUtils;
import com.github.trganda.utils.Utils;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class NormalSQLEngine implements SQLEngine {

    private final String user;
    private final String passwd;

    public NormalSQLEngine(String user, String passwd) {
        this.user = user;
        this.passwd = passwd;
    }

    @Override
    public void authenticate(String database, String userName, byte[] scramble411, byte[] authSeed)
            throws IOException {
        // Check if the password is valid
        authenticateSimply(database, userName, scramble411, authSeed);
    }

    @Override
    public void query(
            ResultSetWriter resultSetWriter,
            String database,
            String userName,
            byte[] scramble411,
            byte[] authSeed,
            String sql)
            throws IOException {
        // Print useful information
        System.out.println("Database: " + database + ", User: " + userName + ", SQL: " + sql);

        // Check if the password is valid
        authenticateSimply(database, userName, scramble411, authSeed);
    }

    // Just check if the password equal to the preset
    private void authenticateSimply(
            String database, String userName, byte[] scramble411, byte[] authSeed)
            throws IOException {
        // SHA1 and encode the password
        String validPasswordSha1 = SHAUtils.SHA(this.passwd, SHAUtils.SHA_1);
        String validScramble411WithSeed20 = Utils.scramble411(validPasswordSha1, authSeed);

        // Use utils to compare the password
        if (!userName.equals(user) || !Utils.compareDigest(
                validScramble411WithSeed20, Base64.getEncoder().encodeToString(scramble411))) {
            // Throw an exception if the checking failed
            throw new IOException(
                    new IllegalAccessException("Authentication failed: user name or password is incorrect"));
        }
    }
}
