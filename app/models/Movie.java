package models;
import java.sql.Timestamp;

/**
 * Represents one movie
 *
 * @author Alex lee     al3774@rit.edu
 */
public class Movie {
    private int movieID;
    private String title;
    private int length;
    private Timestamp releaseDate;
    private String mpaa;

    /**
     * Constructor for movie
     * @param title the title
     * @param length length of the movie
     * @param releaseDate date released
     * @param mpaa rating
     */
    public Movie(int movieID, String title, int length, Timestamp releaseDate, String mpaa) {
        this.movieID = movieID;
        this.title = title;
        this.length = length;
        this.releaseDate = releaseDate;
        this.mpaa = mpaa;
    }

    /**
     * Gets the title
     */
    public String getTitle() { return title; }

    /**
     * Gets the length
     */
    public int getLength() { return length; }

    /**
     * Gets the release date
     */
    public Timestamp getReleaseDate() { return releaseDate; }

    /**
     * Gets the mpaa rating
     */
    public String getMpaa() { return mpaa; }

}
