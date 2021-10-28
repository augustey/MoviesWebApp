package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import models.Movie;
import models.MovieManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

/**
 * This controller handles HTTP requests for videos
 *
 * @author Alex Lee     al3774@rit.edu
 */
public class MovieController extends Controller {
    private final MovieManager movieManager;
    private final Logger logger;

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public MovieController(MovieManager movieManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Loads a movie's page
     * @param movieID the id of the movie to load
     * @return the response body
     */
    public CompletionStage<Result> loadMovie(Http.Request request, int movieID) {
        Http.Session session = request.session();

        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            logger.info("Attempting to find movie...");
            return movieManager.getMovie(movieID)
                    .thenApply(movie -> {
                        logger.info("Found movie");
                        return ok(views.html.movie.render(user, movie, session));
                    });
        }).orElseGet(() -> {
            return movieManager.getMovie(movieID)
                    .thenApply(movie -> {
                        logger.info("Found movie");
                        return ok(views.html.movie.render(null, movie, session));
                    });
        });


    }
}
