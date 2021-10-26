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
     * TODO Get rating, get video, search video
     */
}
