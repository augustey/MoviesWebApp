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
     * @param dataSource
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
     * TODO Get rating, rate? (should rating have default of 0) get video, search video
     */
}
