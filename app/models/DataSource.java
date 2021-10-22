package models;

import com.google.inject.ImplementedBy;

/**
 * Interface for a data source used to access postgreSQL databases.
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
@ImplementedBy(PostgreSQLDataSource.class)
public interface DataSource {
    /**
     * Execute some callback with a connection.
     * @param callable Callback function that executes code with a connection.
     * @param <T> Any value the calling class expects
     * @return any value of type T that the calling class expects.
     */
    <T> T withConnection(DataSourceCallable<T> callable);

    /**
     * Close the connection
     */
    void close();
}
