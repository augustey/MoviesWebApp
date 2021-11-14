package controllers;

import models.MovieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

/**
 * Class for searching database for movies
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class MovieSearchController {
    private final MovieManager movieManager;
    private final Logger logger;

    /**
     * Constructor for MovieSearchController
     * @param movieManager the MovieManager used to perform movie related
     *                     database transactions
     */
    @Inject
    public MovieSearchController(MovieManager movieManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Display the resulting set of movies from a complex query
     * @param request the request from the client
     * @param query The search term
     * @param searchCategory The search category (i.e title, genre, release date)
     * @param sortCategory The sort parameter  (i.e. title, genre, release date)
     * @param ascending whether the resulting set should be sorted ascending or descending
     * @return
     */
    public CompletionStage<Result> search(Http.Request request, String query, int searchCategory, int sortCategory, boolean ascending) {
        Http.Session session = request.session();
        return movieManager.searchMovies(query, searchCategory, sortCategory, ascending).thenApply(movies ->
            ok(views.html.moviesearch.render(movies, query.equals("N/A") ? "" : query, session))
        );
    }
}
