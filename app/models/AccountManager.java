package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Message;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AccountManager {
    private final DataSource dataSource;
    private final Message INCORRECT_USER = Message.error("Username or password is incorrect");
    private final Logger logger;

    @Inject
    AccountManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletionStage<Message> verifyUser(String username, String password) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT Username, Password FROM Users WHERE Username='%s';";
                    sql = String.format(sql, username);
                    Message returnMessage;
                    ResultSet results = statement.executeQuery(sql);
                    logger.info("Attempting to verify user " + username + "...");

                    results.next();

                    String resultPass = results.getString("Password");

                    if(!results.wasNull() && resultPass.equals(password)) {
                        returnMessage = Message.info("Success");
                        logger.info("User " + username+ " successfully retrieved and validated.");
                    }
                    else {
                        returnMessage = INCORRECT_USER;
                        logger.info("Unable to verify user.");
                    }

                    results.close();
                    statement.close();

                    return returnMessage;
                })
        );
    }
}
