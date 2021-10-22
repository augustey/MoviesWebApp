package models;

import java.sql.Connection;

/**
 * Functional interface for DataSource Callback.
 * @author Yaqim Auguste (yaa6681@rit.edu)
 * @param <T> return type of callback.
 */
public interface DataSourceCallable<T> {
    /**
     * Execute some code with a database connection.
     * @param conn The connection object representing a connection to a PostgreSQL database.
     * @return Any value that the calling class expects.
     * @throws Exception An exception that is raised when executing the callback.
     */
    T call(Connection conn) throws Exception;
}
