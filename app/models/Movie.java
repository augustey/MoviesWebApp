package models;

import java.util.Date;
import java.util.Objects;

/**
 * Represents one movie
 *
 * @author Alex lee (al3774@rit.edu)
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class Movie {
    private int movieID;
    private String title;
    private int length;
    private Date releaseDate;
    private String mpaa;
    private double rating;
    private String directors;
    private String castMembers;

    /**
     * Constructor for movie
     * @param movieID Movie id of the movie
     * @param title Title of the movie
     * @param length Length of the movie
     * @param releaseDate Release date of the movie
     * @param mpaa MPAA rating of the movie
     * @param rating Average user rating of the movie
     * @param directors Movie's directors
     * @param castMembers Movie's cast members
     */
    public Movie(int movieID, String title, int length, Date releaseDate, String mpaa, double rating, String directors, String castMembers) {
        this.movieID = movieID;
        this.title = title;
        this.length = length;
        this.releaseDate = releaseDate;
        this.mpaa = mpaa;
        this.rating = rating;
        this.directors = directors;
        this.castMembers = castMembers;
    }

    /**
     * Constructor for movie independent of derived attributes
     * @param movieID Movie id of the movie
     * @param title Title of the movie
     * @param length Length of the movie
     * @param releaseDate Release date of the movie
     * @param mpaa MPAA rating of the movie
     */
    public Movie(int movieID, String title, int length, Date releaseDate, String mpaa) {
        this(movieID, title, length, releaseDate, mpaa, 0, null, null);
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
     * Gets the movieID
     */
    public int getMovieID() {
        return movieID;
    }

    /**
     * Gets the rating
     */
    public double getRating() {
        return rating;
    }

    /**
     * Gets the directors
     */
    public String getDirectors() {
        return directors;
    }

    /**
     * Gets the cast members
     */
    public String getCastMembers() {
        return castMembers;
    }

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

    /**
     * Equals method
     * @param o other object
     * @return whether the two are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return movieID == movie.movieID;
    }

    /**
     * Hash method
     */
    @Override
    public int hashCode() {
        return Objects.hash(movieID);
    }
}
