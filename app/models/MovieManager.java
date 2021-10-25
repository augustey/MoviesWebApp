package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

/**
 * Class for doing operations on a movie or movies
 *
 * @author Alex Lee     al3774@rit.edu
 */
public class MovieManager {
    private final DataSource dataSource;
    private final Logger logger;

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
     * TODO Get rating,
     */
}
