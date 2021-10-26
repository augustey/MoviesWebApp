package controllers;
import models.Movie;
import models.MovieManager;
import play.mvc.*;
import javax.inject.Inject;

/**
 * This controller handles HTTP requests for videos
 *
 * @author Alex Lee     al3774@rit.edu
 */
public class MovieController extends Controller {
    private final MovieManager movieManager;

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public MovieController(MovieManager movieManager) {
        this.movieManager = movieManager;
    }

    /**
     * Loads a movie's page
     * @param id the id of the movie to load
     * @return the response body
     */
    public Result loadMovie(Http.Request request, int movieID) {
        Http.Session session = request.session();

        return null;
    }
}
