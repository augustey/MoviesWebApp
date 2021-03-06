package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.inject.Inject;

/**
 * Class for doing operations on a movie or movies
 *
 * @author Alex Lee (al3774@rit.edu)
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class MovieManager {
    private final DataSource dataSource;
    private final Logger logger;
    private final String[] categories = {"Title, ReleaseDate", "Title", "ReleaseDate", "S.Name", "C.Name", "D.name", "Genre"};

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

                    String sql = "SELECT M.MovieID AS MovieID, Title, Length, MPAA, ReleaseDate, ROUND(AVG(Rating),1) AS Rating " +
                                 "FROM Movies AS M JOIN Watches AS W ON M.MovieID=W.MovieID " +
                                 "WHERE M.MovieID = %d " +
                                 "GROUP BY M.MovieID;";
                    sql = String.format(sql, movieID);

                    logger.info("Attempting to fetch movie " + movieID + "...");
                    ResultSet results = statement.executeQuery(sql);
                    Movie movie = null;

                    if(results.next()) {
                        String title = results.getString("Title");
                        int length = results.getInt("Length");
                        Date releaseDate = results.getDate("ReleaseDate");
                        String mpaa = results.getString("MPAA");
                        double rating = results.getDouble("Rating");

                        movie = new Movie(movieID, title, length, releaseDate, mpaa, rating);
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
                    String incrementQuery = "UPDATE watches SET timesplayed = timesplayed + 1, lastwatched=CURRENT_TIMESTAMP " +
                                            "WHERE userid=%d AND movieid=%d";
                    incrementQuery = String.format(incrementQuery, userID, movieID);

                    // increment watched
                    statement.executeUpdate(incrementQuery);
                }
                else {
                    // If entry doesn't already exist
                    logger.info("Movie not found, creating entry");

                    String createWatchedQuery = "INSERT INTO watches (userid, movieid) " +
                            "VALUES(%d, %d)";
                    createWatchedQuery = String.format(createWatchedQuery, userID, movieID);
                    statement.executeUpdate(createWatchedQuery);
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

                        statement.executeUpdate(rateQuery);
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


    /**
     * Get rating of a movie
     * @param movieID the movie to get the rating of
     * @return Completion stage containing the rating
     */
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
     * Search database for movies based on a query and search parameters
     * @param query The search term
     * @param searchCategoryID The category to search in (Title, Studio, Release Date, Cast Members, Directors, Genre)
     * @param sortCategoryID The category to sort by (Default, Title, Release Date, Genre)
     * @param ascending Whether the results should be ascending or descending
     * @return A LinkedHashSet of movies matching the search parameters
     */
    public CompletionStage<LinkedHashSet<Movie>> searchMovies(String query, int searchCategoryID, int sortCategoryID, boolean ascending) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    String searchCategory = categories[searchCategoryID%(categories.length-1)+1];
                    String sortCategory = categories[sortCategoryID%categories.length];
                    String order = ascending ? "ASC" : "DESC";
                    Statement movieStatement = conn.createStatement();
                    Statement personStatement = conn.createStatement();
                    String sql = "SELECT M.MovieID AS MovieID, Title, Length, MPAA, ROUND(AVG(Rating),1) AS Rating "+
                                 "FROM Movies AS M JOIN Watches AS W ON M.MovieID=W.MovieID "+
                                 "JOIN CastMembers AS P1 ON M.MovieID = P1.MovieID "+
                                 "JOIN Directors AS P2 ON M.MovieID = P2.MovieID "+
                                 "JOIN People AS C ON C.PersonID = P1.PersonID "+
                                 "JOIN People AS D ON D.PersonID = P2.PersonID "+
                                 "JOIN Genre AS G ON M.MovieID=G.MovieID "+
                                 "JOIN StudioMovies AS T ON M.MovieID=T.MovieID "+
                                 "JOIN Studios AS S ON S.StudioID=T.StudioID "+
                                 "WHERE LOWER(%s::VARCHAR) LIKE '%%%s%%' "+
                                 "GROUP BY M.MovieID, %s "+
                                 "ORDER BY (%s) %s;";
                    sql = String.format(sql, searchCategory, query.toLowerCase(), sortCategory, sortCategory, order);
                    LinkedHashSet<Movie> movies = new LinkedHashSet<>();
                    ResultSet movieResults = movieStatement.executeQuery(sql);

                    logger.info("Retrieving movies...");

                    while(movieResults.next()) {
                        int movieID = movieResults.getInt("MovieID");
                        String title = movieResults.getString("Title");
                        int length = movieResults.getInt("Length");
                        String mpaa = movieResults.getString("MPAA");
                        double rating = movieResults.getDouble("Rating");
                        String directors = "";
                        String castMembers = "";
                        sql = "SELECT Name FROM Directors AS D, People AS P WHERE MovieID=%d AND D.PersonID=P.PersonID;";
                        sql = String.format(sql, movieID);
                        ResultSet personResults = personStatement.executeQuery(sql);

                        while(personResults.next()) {
                            directors += personResults.getString("Name") +", ";
                        }

                        personResults.close();

                        sql = "SELECT Name FROM CastMembers AS C, People AS P WHERE MovieID=%d AND C.PersonID=P.PersonID;";
                        sql = String.format(sql, movieID);
                        personResults = personStatement.executeQuery(sql);

                        while(personResults.next()) {
                            castMembers += personResults.getString("Name") +", ";
                        }

                        personResults.close();

                        directors = directors.substring(0, directors.length()-2);
                        castMembers = castMembers.substring(0, castMembers.length()-2);

                        Movie movie = new Movie(movieID, title, length, null, mpaa, rating, directors, castMembers);

                        movies.add(movie);
                    }

                    logger.info("Successfully retrieved all movies.");
                    
                    personStatement.close();
                    movieResults.close();
                    movieStatement.close();

                    return movies;
                })
        );
    }

    /**
     * Get the 20 most popular movies in the last 90 days
     * @return List of most popular movies
     */
    public CompletionStage<List<Movie>> get90DayRolling() {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    String sql = "SELECT Movies.MovieID, Title, ReleaseDate, Length, MPAA "+
                                 "FROM Movies JOIN Watches ON Movies.MovieID = Watches.MovieID "+
                                 "WHERE LastWatched >= CURRENT_TIMESTAMP - '90d'::INTERVAL "+
                                 "GROUP BY Movies.MovieID "+
                                 "ORDER BY (COALESCE(AVG(Rating), 0)*COUNT(UserID), SUM(TimesPlayed)) DESC "+
                                 "LIMIT 20;";
                    List<Movie> movies = new ArrayList<>();
                    Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql);

                    while(results.next()) {
                        int movieID = results.getInt("MovieID");
                        String title = results.getString("Title");
                        Date releaseDate = results.getDate("ReleaseDate");
                        int length = results.getInt("Length");
                        String mpaa = results.getString("MPAA");
                        Movie movie = new Movie(movieID, title, length, releaseDate, mpaa);
                        movies.add(movie);
                    }

                    results.close();
                    statement.close();

                    return movies;
                })
        );
    }

    /**
     * Get the 20 most popular movies in the last 90 days
     * @return List of most popular movies
     */
    public CompletionStage<List<Movie>> getFriendTopMovies() {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    String sql = "SELECT Movies.MovieID, Title, ReleaseDate, Length, MPAA "+
                                 "FROM Movies JOIN Watches ON Movies.MovieID = Watches.MovieID "+
                                 "JOIN Follows ON Watches.UserID = FollowedUserID "+
                                 "WHERE FollowerUserID=3 "+
                                 "GROUP BY Movies.movieid "+
                                 "ORDER BY (COALESCE(AVG(Rating), 0), SUM(TimesPlayed)) DESC "+
                                 "LIMIT 20;";
                    List<Movie> movies = new ArrayList<>();
                    Statement statement = conn.createStatement();
                    ResultSet results = statement.executeQuery(sql);

                    while(results.next()) {
                        int movieID = results.getInt("MovieID");
                        String title = results.getString("Title");
                        Date releaseDate = results.getDate("ReleaseDate");
                        int length = results.getInt("Length");
                        String mpaa = results.getString("MPAA");
                        Movie movie = new Movie(movieID, title, length, releaseDate, mpaa);
                        movies.add(movie);
                    }

                    results.close();
                    statement.close();

                    return movies;
                })
        );
    }

    /**
     * Gets the top 5 most popular movies released in the last month
     * @return The top 5 list
     */
    public CompletionStage<List<Movie>> getTop5LastMonth() {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();

                    List<Movie> top5 = new ArrayList<>();

                    logger.info("Getting top 5 movies in the last month");
                    String getTop5Query = """
                            SELECT movies.movieid, movies.title, movies.length, movies.releasedate, movies.mpaa,
                            AVG(watches.rating) as rating
                            FROM movies
                            JOIN watches ON movies.movieid = watches.movieid
                            WHERE movies.releasedate >= CURRENT_DATE - INTERVAL '1 month'
                            GROUP BY movies.movieid
                            ORDER BY rating DESC
                            LIMIT 5;
                            """;
                    ResultSet results = statement.executeQuery(getTop5Query);

                    while(results.next()) {
                        int movieID = results.getInt("movieID");
                        String title = results.getString("Title");
                        int length = results.getInt("Length");
                        Date releaseDate = results.getDate("ReleaseDate");
                        String mpaa = results.getString("MPAA");
                        double rating = results.getDouble("rating");

                        Movie movie = new Movie(movieID, title, length, releaseDate, mpaa, rating);

                        top5.add(movie);
                    }

                    return top5;
                })
        );
    }

    /**
     * Gets 10 movie recommendations based on watch history and the history of similar users
     *
     * @param userID the user to get recommendations for
     * @return the recommended movies list
     */
    public CompletionStage<List<Movie>> getForYou(int userID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();

                    List<Movie> forYou = new ArrayList<>();

                    logger.info("Getting top genre");
                    String getGenresQuery = """
                            SELECT genre.genre, COUNT(*) as count
                            FROM genre
                            JOIN movies ON genre.movieid = movies.movieid
                            JOIN watches ON genre.movieid = watches.movieid
                            WHERE watches.userid = %d
                            GROUP BY genre.genre
                            ORDER BY count DESC;
                            """;
                    getGenresQuery = String.format(getGenresQuery, userID);
                    ResultSet genreResult = statement.executeQuery(getGenresQuery);

                    String firstGenre = null;
                    String secondGenre = null;

                    // Get the results
                    if(genreResult.next()) {
                        firstGenre = firstGenre = genreResult.getString("genre");
                        logger.info("First genre: " + firstGenre);
                    }
                    if(genreResult.next()) {
                        secondGenre = genreResult.getString("genre");
                        logger.info("Second genre: " + secondGenre);
                    }

                    String topGenreQuery = """
                            SELECT movies.movieid, movies.title, movies.length, movies.releasedate,
                            movies.mpaa, AVG(watches.rating) as rating
                            FROM movies
                            JOIN genre ON movies.movieid = genre.movieid
                            JOIN watches ON movies.movieid = watches.movieid
                            WHERE genre = '%s'
                            GROUP BY movies.movieid
                            ORDER BY rating DESC;
                            """;

                    /*
                    Get top 2 genres and select movies based on that
                     */
                    if(firstGenre != null) {
                        String firstGenreQuery = String.format(topGenreQuery, firstGenre);
                        ResultSet firstGenreResult = statement.executeQuery(firstGenreQuery);

                        // Add 3 movies from the top genre
                        for(int i = 0; i < 3; i++) {
                            if(firstGenreResult.next()) {
                                int movieID = firstGenreResult.getInt("movieID");
                                String title = firstGenreResult.getString("Title");
                                int length = firstGenreResult.getInt("Length");
                                Date releaseDate = firstGenreResult.getDate("ReleaseDate");
                                String mpaa = firstGenreResult.getString("MPAA");

                                Movie movie = new Movie(movieID, title, length, releaseDate, mpaa);

                                forYou.add(movie);
                            }
                        }
                    }
                    if(secondGenre != null) {
                        String secondGenreQuery = String.format(topGenreQuery, secondGenre);
                        ResultSet secondGenreResult = statement.executeQuery(secondGenreQuery);

                        // Add 2 movies from the 2nd genre
                        for(int i = 0; i < 2; i++) {
                            if (secondGenreResult.next()) {
                                int movieID = secondGenreResult.getInt("movieID");
                                String title = secondGenreResult.getString("Title");
                                int length = secondGenreResult.getInt("Length");
                                Date releaseDate = secondGenreResult.getDate("ReleaseDate");
                                String mpaa = secondGenreResult.getString("MPAA");

                                Movie movie = new Movie(movieID, title, length, releaseDate, mpaa);

                                forYou.add(movie);
                            }
                        }
                    }


                    return forYou;
                })
        );
    }
}
