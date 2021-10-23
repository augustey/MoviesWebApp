package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Class for handling user database operations.
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class AccountManager {
    private final DataSource dataSource;
    private final Logger logger;

    /**
     * Constructor for AccountManager
     * @param dataSource the DataSource used to access the PostgreSQL database
     */
    @Inject
    AccountManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Attempt to retrieve a user with a given username and password.
     * @param username The user's username
     * @param password The user's password
     * @return The corresponding User object if credentials were correct
     */
    public CompletionStage<User> getUser(String username, String password) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT *  FROM Users WHERE Username='%s';";
                    sql = String.format(sql, username);
                    User user = null;
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Attempting to verify user " + username + "...");

                    if(results.next()) {
                        String resultPass = results.getString("Password");

                        if (resultPass.equals(password)) {
                            String email = results.getString("Email");
                            String first = results.getString("FirstName");
                            String last = results.getString("LastName");
                            Timestamp creationDate = results.getTimestamp("CreationDate");
                            Timestamp lastAccess = results.getTimestamp("LastAccess");

                            user = new User(username, email, first, last, creationDate, lastAccess);

                            logger.info("User " + username + " successfully retrieved and validated.");
                        }
                    }

                    if(user == null)
                        logger.info("Unable to verify user.");

                    results.close();
                    statement.close();

                    return user;
                })
        );
    }
}
