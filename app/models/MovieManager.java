package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Message;

import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.inject.Inject;

/**
 * Class for doing operations on a movie or movies
 *
 * @author Alex Lee     al3774@rit.edu
 */
public class MovieManager {
    private final DataSource dataSource;
    private final Logger logger;
    private final Message VIDEO_DNE = Message.error("Video does not exist");

    /**
     * Constructor for MovieManager
     * @param dataSource sql data source
     */
    @Inject
    public MovieManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    /**
     * Gets a selected movie from the database based on its ID
     * @param movieID the movie's ID
     * @return an object representation of a movie if found, null if not
     */
    public CompletionStage<Movie> getMovie(int movieID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();

                    String sql = "SELECT * FROM movies WHERE movieid = %d;";
                    sql = String.format(sql, movieID);

                    logger.info("Attempting to fetch movie " + movieID + "...");
                    ResultSet results = statement.executeQuery(sql);
                    Movie movie = null;

                    if(results.next()) {
                        String title = results.getString("title");
                        int length = results.getInt("length");
                        Timestamp releaseDate = results.getTimestamp("releaseDate");
                        String mpaa = results.getString("mpaa");

                        movie = new Movie(movieID, title, length, releaseDate, mpaa);
                    }

                    if(movie == null) {
                        logger.info("Unable to find movie " + movieID);
                    }
                    else {
                        logger.info("Movie " + movieID + " found!");
                    }

                    results.close();
                    statement.close();

                    return movie;
                })
        );
    }

    /**
     * Play the movie. Updates the database by incrementing the movie's watch count.
     *
     * @param movieID the id of the movie to play
     * @return CompletableStage for asynchronous code
     */
    public CompletionStage<Void> playMovie(int userID, int movieID) {
        return CompletableFuture.supplyAsync(() ->
            dataSource.withConnection(conn -> {
                Statement statement = conn.createStatement();

                // Check if watched exists
                String checkExistsQuery = "SELECT * FROM watches WHERE userid=%d AND movieid=%d";
                checkExistsQuery = String.format(checkExistsQuery, userID, movieID);

                ResultSet checkExistsResult = statement.executeQuery(checkExistsQuery);

                logger.info("Attempting to play movie...");

                if (checkExistsResult.next()) {
                    logger.info("Movie found, incrementing times played");
                    String incrementQuery = "UPDATE watches SET timesplayed = timesplayed + 1 " +
                            "WHERE userid=%d AND movieid=%d";
                    incrementQuery = String.format(incrementQuery, userID, movieID);

                    // increment watched
                    statement.executeQuery(incrementQuery);
                }
                else {
                    // If entry doesn't already exist
                    logger.info("Movie not found, creating entry");

                    String createWatchedQuery = "INSERT INTO watches (userid, movieid) " +
                            "VALUES(%d, %d)";
                    createWatchedQuery = String.format(createWatchedQuery, userID, movieID);
                    statement.executeQuery(createWatchedQuery);
                }

                checkExistsResult.close();
                statement.close();

                return null;
            })
        );
    }

    /**
     * Rate a movie. The rating must be between 1 and 5
     *
     * @param rating the score
     * @param userID the user that is rating
     * @param movieID the movie that is rated
     * @return CompletableStage for asynchronous code
     */
    public CompletionStage<Message> rateMovie(int rating, int userID, int movieID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    // Check for bounding
                    if(rating < 1 || rating > 5) {
                        return Message.error("Rating must be between 1 and 5!");
                    }

                    Statement statement = conn.createStatement();
                    // The message that will be returned
                    Message message;

                    // Check if watched exists
                    String checkExistsQuery = "SELECT * FROM watches WHERE userid=%d AND movieid=%d";
                    checkExistsQuery = String.format(checkExistsQuery, userID, movieID);

                    ResultSet checkExistsResult = statement.executeQuery(checkExistsQuery);

                    logger.info("Attempting to rate movie...");

                    if (checkExistsResult.next()) {
                        // If entry found
                        logger.info("Movie found, applying rating");


                        String rateQuery = "UPDATE watches SET rating = %d " +
                                "WHERE userid=%d AND movieid=%d";
                        rateQuery = String.format(rateQuery, rating, userID, movieID);

                        statement.executeQuery(rateQuery);
                        message = Message.info("Movie rated successfully!");
                    }
                    else {
                        // If entry doesn't already exist, should not be able to rate
                        logger.info("Movie not found, cannot rate");

                        message = Message.error("You can't rate a movie you haven't watched!");
                    }

                    checkExistsResult.close();
                    statement.close();

                    return message;
                })
        );
    }


    public CompletionStage<Double> getRating(int movieID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    // message to be returned
                    double result;

                    String getRatingsQuery = "SELECT COUNT(rating) as count, SUM(rating) as sum" +
                            " FROM watches WHERE movieid=%d";
                    getRatingsQuery = String.format(getRatingsQuery, movieID);

                    ResultSet getRatingsResult = statement.executeQuery(getRatingsQuery);

                    logger.info("Attempting to get movie rating...");

                    if (getRatingsResult.next()) {
                        logger.info("Rating found");
                        result = getRatingsResult.getDouble("sum") /
                                getRatingsResult.getDouble("count");
                    }
                    else {
                        // If no ratings
                        logger.error("Movie has no ratings!");
                        result = 0; // Will need to check for 0
                    }

                    getRatingsResult.close();
                    statement.close();

                    return result;
                })
        );
    }

    /**
     * TODO search video
     */
}
