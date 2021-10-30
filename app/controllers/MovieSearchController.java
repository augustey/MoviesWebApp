package controllers;

import models.MovieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.*;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

public class MovieSearchController extends Controller {
    private final MovieManager movieManager;
    private final Logger logger;

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public MovieSearchController(MovieManager movieManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Loads the movie search page
     * @param searchKey the string used to search
     * @return the response body
     */
    public CompletionStage<Result> searchMovie(Http.Request request, String searchKey) {
        Http.Session session = request.session();

        return movieManager.searchMovie(searchKey).thenApply(movieList -> {
            return ok(views.html.moviesearch.render(movieList, session));
        });
    }

}
