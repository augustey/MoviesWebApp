package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import models.Movie;
import models.MovieManager;
import models.User;
import play.libs.Json;
import play.mvc.*;

import java.util.concurrent.CompletionStage;
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
     * @param movieID the id of the movie to load
     * @return the response body
     */
    public Result loadMovie(Http.Request request, int movieID) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            final User user = Json.fromJson(userNode, User.class);

            movieManager.getMovie(movieID)
                    .thenApply(movie -> {
                        return ok(views.html.movie.render(user, movie, session));
                    });
        });

/* OLD WAY, no user
        return movieManager.getMovie(movieID)
                .thenApply(movie -> {
                    return ok(views.html.movie.render(user, movie, session));
                });

 */
    }
}
