package models;

import java.util.Date;

/**
 * Represents one movie
 *
 * @author Alex lee     al3774@rit.edu
 */
public class Movie {
    private int movieID;
    private String title;
    private int length;
    private Date releaseDate;
    private String mpaa;

    /**
     * Constructor for movie
     * @param title the title
     * @param length length of the movie
     * @param releaseDate date released
     * @param mpaa rating
     */
    public Movie(int movieID, String title, int length, Date releaseDate, String mpaa) {
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
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * Gets the mpaa rating
     */
    public String getMpaa() { return mpaa; }

    /**
     * Takes the length in minutes and returns a more readable version in the
     * format: "00hr 00min"
     * @return the formatted string
     */
    public String lengthToString() {
        String hours = String.valueOf(length / 60);
        String minutes = String.valueOf(length % 60);

        return hours + "hr " + minutes + "min";
    }
}
