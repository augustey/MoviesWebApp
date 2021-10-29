package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Class for handling collection related database operations
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class CollectionManager {
    private final DataSource dataSource;
    private final Logger logger;

    /**
     * Constructor for CollectionManager
     * @param dataSource DataSource used to perform database operations with
     */
    @Inject
    CollectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletionStage<List<Movie>> getCollectionMovies(int collectionID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT * FROM Movies AS M, CollectionMovies AS C WHERE M.MovieID=C.MovieID AND C.CollectionID=%d;";
                    sql = String.format(sql, collectionID);
                    List<Movie> movies = new ArrayList<>();
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Attempting to retrieve movies for CollectionID:"+collectionID+"...");

                    while(results.next()) {
                        int movieID = results.getInt("MovieID");
                        String title = results.getString("Title");
                        int length = results.getInt("Length");
                        String mpaa = results.getString("MPAA");
                        Date releaseDate = results.getDate("ReleaseDate");

                        Movie movie = new Movie(movieID, title, length, releaseDate, mpaa);

                        movies.add(movie);
                    }

                    logger.info("Successfully retrieved "+movies.size()+" movies for CollectionID: "+collectionID);

                    return movies;
                })
        );
    }

    /**
     * Delete a collection
     * @param collectionID The id of the collection to delete
     * @return A completion stage for asynchronous execution handling
     */
    public CompletionStage<Void> deleteCollection(int collectionID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "DELETE FROM Collections WHERE CollectionID=%d;";
                    sql = String.format(sql, collectionID);

                    logger.info("Deleting collection "+collectionID+"...");

                    statement.executeUpdate(sql);
                    statement.close();

                    return null;
                })
        );
    }

    /**
     * get a single collection based on the collectionID
     * @param collectionID The id of the collection to retrieve
     * @return A completion stage containing the retrieved collection value object
     */
    public CompletionStage<MovieCollection> getSingleCollection(int collectionID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT * FROM Collections WHERE CollectionID=%d";
                    sql = String.format(sql, collectionID);
                    MovieCollection col = null;
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Attempting to retrieve collection:"+collectionID+"...");

                    while(results.next()) {
                        String name = results.getString("Name");
                        col = new MovieCollection(collectionID, name, 0, 0);
                        logger.info("Successfully retrieved "+col.getName()+"!");
                    }

                    if(col == null)
                        logger.info("Unable to retrieve collection with id "+collectionID);

                    statement.close();

                    return col;
                })
        );
    }

    /**
     * Update a collections name to the one specified
     * @param collectionID The collection to update
     * @param name The new name of the collection
     * @return A completion stage for asynchronous execution handling
     */
    public CompletionStage<Void> updateCollectionName(int collectionID, String name) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    if(name.strip().equals(""))
                        return null;

                    Statement statement = conn.createStatement();
                    String sql = "UPDATE Collections SET Name='%s' WHERE CollectionID=%d;";
                    sql = String.format(sql, name, collectionID);

                    logger.info("Changing collection "+collectionID+" name to "+name+"...");

                    statement.executeUpdate(sql);
                    logger.info("Successfully changed colelction name");


                    statement.close();
                    return null;
                })
        );
    }

    /**
     * Create a new collection with a given name
     * @param userID User who owns the collection
     * @param name The name of the new collection
     * @return A completion stage for asynchronous execution handling
     */
    public CompletionStage<Void> createCollection(int userID, String name) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    if(name.strip().equals(""))
                        return null;

                    Statement statement = conn.createStatement();
                    String sql = "INSERT INTO Collections (UserID, Name) VALUES(%d, '%s');";
                    sql = String.format(sql, userID, name);

                    logger.info("Creating new collection "+name+"...");

                    statement.executeUpdate(sql);

                    logger.info("Successfully created new collection "+name+"...");

                    statement.close();
                    return null;
                })
        );
    }

    /**
     * Get all the collections owned by a particular user.
     * @param userID UserID corresponding to the user
     * @return A List of MovieCollection value objects
     */
    public CompletionStage<List<MovieCollection>> getCollections(int userID) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
                    Statement statement = conn.createStatement();
                    String sql = "SELECT C.CollectionID, C.Name, COUNT(M.MovieID) AS Total, COALESCE(SUM(M.Length),0) AS Length "+
                                 "FROM Collections AS C "+
                                 "LEFT JOIN CollectionMovies AS S ON C.CollectionID=S.CollectionID "+
                                 "LEFT JOIN Movies AS M ON M.MovieID=S.MovieID "+
                                 "WHERE UserID=%d "+
                                 "GROUP BY C.CollectionID, C.Name "+
                                 "ORDER BY C.Name;";
                    sql = String.format(sql, userID);
                    List<MovieCollection> collections = new ArrayList<>();
                    ResultSet results = statement.executeQuery(sql);

                    logger.info("Attempting to retrieve UserID:"+userID+" collections...");

                    while(results.next()) {
                        int collectionID = results.getInt("CollectionID");
                        String name = results.getString("Name");
                        int total = results.getInt("Total");
                        int length = results.getInt("Length");
                        MovieCollection col = new MovieCollection(collectionID, name, total, length);

                        collections.add(col);
                    }

                    logger.info("Successfully retrieved "+collections.size()+" collections for UserID: "+userID);

                    return collections;
                })
        );
    }
}