package util;

/**
 * Simple class representing a message indicating success or error
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class Message {
    /**
     * Different types of messages
     */
    public enum Type {
        INFO,
        ERROR
    }

    private final String message;
    private final Type type;

    /**
     * Constructor for the message class
     * @param message The message for this object to store
     * @param type The type of message this is (eg. Info, Error)
     */
    Message(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Retrieve the text from this object
     * @return the text stored in this object
     */
    public String getText() {
        return message;
    }

    /**
     * Retrieve the type of message
     * @return the message type
     */
    public Type getType() {
        return type;
    }

    /**
     * Whether the operation succeeded
     * @return whether or not the operation succeeded
     */
    public boolean succeeded() {
        return type.equals(Type.INFO);
    }

    /**
     * Make a new info message
     * @param message The info message
     * @return A new info message object
     */
    public static Message info(String message) {
        return new Message(message, Type.INFO);
    }

    /**
     * Make a new error message
     * @param message the error message
     * @return the resulting message object
     */
    public static Message error(String message) {
        return new Message(message, Type.ERROR);
    }

    /**
     * Convert object to string format
     * @return resulting string
     */
    @Override
    public String toString() {
        return "["+type+"] "+message;
    }
}
