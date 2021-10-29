package models;

/**
 * Value object for a movie collection
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class MovieCollection {
    private int collectionID;
    private String name;
    private int total;
    private int length;

    /**
     * COnstructor for MovieCollection
     * @param collectionID Collection's ID
     * @param name Collection's name
     * @param total Collection's number of movies
     * @param length Collection's total length
     */
    public MovieCollection(int collectionID, String name, int total, int length) {
        this.collectionID = collectionID;
        this.name = name;
        this.total = total;
        this.length = length;
    }

    /**
     * Getter for collectionID
     */
    public int getCollectionID() {
        return collectionID;
    }

    /**
     * Getter for name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Getter for length
     */
    public int getLength() {
        return length;
    }

    /**
     * Getter for length formatted to (hours:minutes)
     */
    public String getLengthFormat() {
        return (int)Math.floor(length/60) + "hr " + length%60+"min";
    }
}
