package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Message;

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
    private final Message USER_EXISTS = Message.error("User already exists");
    private final Message EMAIL_EXISTS = Message.error("Email already in use");

    private final Message USER_SUCCESS = Message.info("User created");



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
     * Attempt to create a new user in the database
     * @param username User's usernmae
     * @param password User's password
     * @param email User's email
     * @param firstName User's first name
     * @param lastName User's last name
     * @return A message object indicating whether the operation was successful or not
     */
    public CompletionStage<Message> createUser(String username, String password, String email, String firstName, String lastName) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT *  FROM Users WHERE Username='%s';";
                    sql = String.format(sql, username);
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Attempting to create user "+username+"...");

                    if(results.next()) {
                        logger.info("Could not create user "+username);
                        results.close();
                        statement.close();
                        return USER_EXISTS;
                    }

                    results.close();

                    sql = "SELECT *  FROM Users WHERE Email='%s';";
                    sql = String.format(sql, email);
                    results = statement.executeQuery(sql);

                    if(results.next()) {
                        logger.info("Could not create user "+username);
                        results.close();
                        statement.close();
                        return EMAIL_EXISTS;
                    }

                    results.close();

                    sql = "INSERT INTO Users (Username, Password, Email, FirstName, LastName) "+
                          "VALUES('%s', '%s', '%s', '%s', '%s');";
                    sql = String.format(sql, username, password, email, firstName, lastName);

                    statement.executeUpdate(sql);

                    logger.info("User "+username+" created successfully!");

                    return USER_SUCCESS;
                })
        );
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
