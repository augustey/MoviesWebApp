package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import models.CollectionManager;
import models.MovieManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;
import util.Message;

import java.util.Map;
import java.util.NoSuchElementException;
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
    private final CollectionManager collectionManager;

    private final static String MOVIE_ERROR = "movie_error";
    private final static String MOVIE_SUCCESS = "movie_success";
    private final static String PLAY_RESULT = "playResult";
    private final static String PLAY_SUCCESSFUL = "Successfully played movie!";
    private final static String PLAY_FAILED = "User not logged in, movie not played.";

    private final static String RATING_RESULT = "ratingResult";
    private final static String RATING_SUCCESSFUL = "Rating was successful!";
    private final static String RATING_FAILED = "User not logged in, rating failed.";

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public MovieController(MovieManager movieManager, CollectionManager collectionManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.collectionManager = collectionManager;
    }

    /**
     * Loads a movie's page
     * @param movieID the id of the movie to load
     * @return the response body
     */
    public CompletionStage<Result> loadMovie(Http.Request request, int movieID) {
        Http.Session session = request.session();

        // results from play or rate calls
        String error = request.flash().get(MOVIE_ERROR).orElse("");
        String success = request.flash().get(MOVIE_SUCCESS).orElse("");
        final Message message = error.equals("") ? Message.info(success) : Message.error(error);

        logger.info("Attempting to find movie...");

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.getMovie(movieID).thenCombine(collectionManager.getCollections(user.getUserID()), (movie, collections) -> {
                logger.info("Found movie");

                return ok(views.html.movie.render(user, movie, message, collections, session));
            });
        }).orElseGet(() ->
                movieManager.getMovie(movieID).thenApply(movie ->
                    ok(views.html.movie.render(null, movie, message, null, session))
                )
        );
    }

    /**
     * Handles watching movies
     * @param movieID the ID of the movie
     * @return the response body
     */
    public CompletionStage<Result> movieWatched(Http.Request request, int movieID) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.playMovie(user.getUserID(), movieID).thenApply(x ->
                redirect("/movie/" + movieID)
                .flashing(MOVIE_SUCCESS, PLAY_SUCCESSFUL)
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(redirect(request.uri())
                .flashing(MOVIE_ERROR, PLAY_FAILED))
        );
    }

    /**
     * Handles rating movies
     * @param movieID the ID of the movie
     * @return the response body
     */
    public CompletionStage<Result> movieRated(Http.Request request, int movieID) {
        Map<String, String[]> params = request.body().asFormUrlEncoded();
        int rating = Integer.parseInt(params.get("rating")[0]);

        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.rateMovie(rating, user.getUserID(), movieID).thenApply(x ->
                redirect("/movie/" + movieID)
                .flashing(MOVIE_SUCCESS, RATING_SUCCESSFUL)
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(redirect(request.uri())
                .flashing(MOVIE_ERROR, RATING_FAILED))
        );
    }
}
