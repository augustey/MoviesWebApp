package controllers;

import models.MovieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

public class MovieSearchController {
    private final MovieManager movieManager;
    private final Logger logger;

    @Inject
    public MovieSearchController(MovieManager movieManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletionStage<Result> search(Http.Request request, String query, int searchCategory, int sortCategory, boolean ascending) {
        Http.Session session = request.session();
        return movieManager.searchMovies(query, searchCategory, sortCategory, ascending).thenApply(movies ->
            ok(views.html.moviesearch.render(movies, query.equals("N/A") ? "" : query, session))
        );
    }
}
