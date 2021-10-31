package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.typesafe.config.Config;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class for creating connections to a PostgreSQL server
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class PostgreSQLDataSource implements DataSource{
    private final Config config;
    private final Logger logger;
    private Session session;
    private Connection conn;
    private static Object lock = new Object();

    /**
     * Constructor for PostgreSQLDataSource.
     * @param config Configuration class containing configurations for SSH tunnel and PostgreSQL database.
     */
    @Inject
    PostgreSQLDataSource(Config config) {
        this.config = config.getConfig("db_config");
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Connect to the external database and execute some code provided with a connection.
     * @param callable Callback function that executes code with a connection.
     * @param <T> Any value that the calling class expects.
     * @return any value of type T that the calling class expects.
     */
    @Override
    public <T> T withConnection(DataSourceCallable<T> callable) {
        synchronized (lock) {
            String username = config.getString("username");
            String password = config.getString("password");
            String host = config.getString("host");
            int lport = config.getInt("lport");
            int rport = config.getInt("rport");
            String databaseName = config.getString("database");
            String driver = config.getString("driver");
            T result = null;

            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, host, 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
                session.connect();

                logger.info("Tunnel Connected...");

                int assigned_port = session.setPortForwardingL(lport, "localhost", rport);

                logger.info("Port Forwarded...");

                String url = "jdbc:postgresql://localhost:" + assigned_port + "/" + databaseName;

                Class.forName(driver);
                conn = DriverManager.getConnection(url, username, password);

                logger.info("Database Connected Successfully!");

                conn.setAutoCommit(false);
                result = callable.call(conn);
                conn.commit();
            } catch (Exception e) {
                logger.error(e.toString());
            } finally {
                close();
            }

            return result;
        }
    }

    /**
     * Close the SSH session and PostgresSQL database connection.
     */
    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
        catch(Exception e) {
            logger.error(e.toString());
        }
    }
}
