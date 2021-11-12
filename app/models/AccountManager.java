package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Message;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
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
     * Follow a user
     * @param followerID The user who is following
     * @param followedID The user that is being followed
     * @return A completion stage indicating the end of the operation
     */
    public CompletionStage<Void> followUser(int followerID, int followedID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "INSERT INTO Follows VALUES(%d, %d) ON CONFLICT DO NOTHING;";
                    sql = String.format(sql, followerID, followedID);

                    statement.executeUpdate(sql);

                    logger.info("user:"+followerID+" followed user:"+followedID);

                    statement.close();

                    return null;
                })
        );
    }

    /**
     * Unollow a user
     * @param followerID The user who is unfollowing
     * @param followedID The user that is being unfollowed
     * @return A completion stage indicating the end of the operation
     */
    public CompletionStage<Void> unfollowUser(int followerID, int followedID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "DELETE FROM Follows WHERE followeruserid=%d AND followeduserid=%d";
                    sql = String.format(sql, followerID, followedID);

                    statement.executeUpdate(sql);

                    logger.info("user:"+followerID+" unfollowed user:"+followedID);

                    statement.close();

                    return null;
                })
        );
    }

    /**
     * Get all the users in the database in the context of a specific user.
     * @param mainUserID The userid of the context of the search
     * @return A map of users and whether they are followed by the user or not
     */
    public CompletionStage<LinkedHashMap<User, Boolean>> getAllUsers(int mainUserID, String query) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT UserID, Username, Email, COUNT(FollowerUserID) AS Following "+
                                 "FROM Users LEFT JOIN Follows "+
                                 "ON FollowerUserID=%d AND FollowedUserID=UserID "+
                                 "WHERE LOWER(Email) LIKE '%%%s%%' "+
                                 "GROUP BY Username, Email, UserID "+
                                 "ORDER BY Following DESC;";
                    sql = String.format(sql, mainUserID, query.toLowerCase());
                    LinkedHashMap<User, Boolean> users = new LinkedHashMap<>();
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Retrieving users...");

                    while(results.next()) {
                        int userID = results.getInt("UserID");
                        String username = results.getString("Username");
                        String email = results.getString("Email");
                        boolean isFollowing = results.getBoolean("Following");

                        User user = new User(userID, username, email, null, null, null, null);
                        users.put(user, isFollowing);
                    }

                    logger.info("Successfully retrieved all users.");

                    results.close();
                    statement.close();

                    return users;
                })
        );
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
     * Set the user's last access date to this moment
     * @param username User to update
     * @return CompletableStage for asynchronous code
     */
    public CompletionStage<Void> setLastAccess(String username) {
        return CompletableFuture.supplyAsync(() ->
            dataSource.withConnection(conn -> {
                Statement statement = conn.createStatement();
                String sql = "UPDATE Users SET LastAccess=CURRENT_TIMESTAMP WHERE Username='%s';";
                sql = String.format(sql, username);

                logger.info("Updating "+username+"'s last access date...");

                statement.executeUpdate(sql);

                logger.info("Successfully updated "+username+" last access.");

                statement.close();

                return null;
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
                            int userID = results.getInt("UserID");
                            String email = results.getString("Email");
                            String first = results.getString("FirstName");
                            String last = results.getString("LastName");
                            Timestamp creationDate = results.getTimestamp("CreationDate");
                            Timestamp lastAccess = results.getTimestamp("LastAccess");

                            user = new User(userID, username, email, first, last, creationDate, lastAccess);

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

    public CompletionStage<Integer> getFollowers(int userID) {
        return CompletableFuture.supplyAsync(() ->
            dataSource.withConnection(conn -> {
                Statement statement = conn.createStatement();
                String sql = "SELECT COUNT(*)  FROM follows WHERE followeduserid = %d;";
                sql = String.format(sql, userID);
                ResultSet results = statement.executeQuery(sql);

                if (results.next()) {
                    int count = results.getInt("count");
                    return count;
                } else {
                    return 0;
                }
            })
        );
    }

    public CompletionStage<Integer> getFollowing(int userID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT COUNT(*)  FROM follows WHERE followeruserid = %d;";
                    sql = String.format(sql, userID);
                    ResultSet results = statement.executeQuery(sql);

                    if (results.next()) {
                        int count = results.getInt("count");
                        return count;
                    } else {
                        return 0;
                    }
                })
        );
    }
}
