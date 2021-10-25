package models;

import java.sql.Timestamp;

/**
 * A value object representing a user. This class is used
 */
public class User {
    private int userID;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Timestamp creationDate;
    private Timestamp lastAccess;

    /**
     * Constructor for user
     * @param username username
     * @param email email
     * @param firstName first name
     * @param lastName last name
     * @param creationDate creation date
     * @param lastAccess last access date
     */
    User(int userID, String username, String email, String firstName, String lastName, Timestamp creationDate, Timestamp lastAccess) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.creationDate = creationDate;
        this.lastAccess = lastAccess;
    }

    /**
     * Getter or userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Getter or username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter or email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Getter or firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Getter or lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Getter or creationDate
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Getter or lastAccess
     */
    public Timestamp getLastAccess() {
        return lastAccess;
    }
}
