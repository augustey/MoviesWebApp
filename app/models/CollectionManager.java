package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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

    public CompletionStage<Void> createCollection(int userID, String name) {
        return CompletableFuture.supplyAsync(() ->
                dataSource.withConnection(conn -> {
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
