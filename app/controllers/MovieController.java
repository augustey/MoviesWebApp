package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import models.MovieManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;

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

    private final static String PLAY_RESULT = "playResult";
    private final static String PLAY_SUCCESSFUL = "Sucessfully played movie!";
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

        // results from play or rate calls
        String playResult;
        String ratingResult;

        try{
            playResult = session.get(PLAY_RESULT).get();
            ratingResult = session.get(RATING_RESULT).get();
        }
        catch(NoSuchElementException e){
            playResult = null;
            ratingResult = null;
        }

        final String finalPlayResult = playResult;
        final String finalRatingResult = ratingResult;

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);



            logger.info("Attempting to find movie...");

            return movieManager.getMovie(movieID)
                    .thenApply(movie -> {
                        logger.info("Found movie");

                        return  ok(views.html.movie.render(user, movie, session, finalPlayResult, finalRatingResult))
                                .removingFromSession(request, PLAY_RESULT, RATING_RESULT);
                    });

        }).orElseGet(() -> {
            return movieManager.getMovie(movieID)
                    .thenApply(movie -> {
                        logger.info("Found movie");
                        return ok(views.html.movie.render(null, movie, session, finalPlayResult, finalRatingResult))
                                .removingFromSession(request, PLAY_RESULT, RATING_RESULT);
                    });
        });
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

            return movieManager.playMovie(user.getUserID(), movieID)
                    .thenApply(x -> {
                        logger.info("Found movie");
                        return  redirect("/movie/" + movieID)
                                .addingToSession(request, PLAY_RESULT, PLAY_SUCCESSFUL);
                    });
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/movie/" + movieID)
                .addingToSession(request, PLAY_RESULT, PLAY_FAILED)));
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

            return movieManager.rateMovie(rating, user.getUserID(), movieID)
                    .thenApply(x -> {
                       logger.info("Movie rated");

                       return redirect("/movie/" + movieID)
                               .addingToSession(request, RATING_RESULT, RATING_SUCCESSFUL);
                    });
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/movie/" + movieID)
                .addingToSession(request, RATING_RESULT, RATING_FAILED)));
    }
}
