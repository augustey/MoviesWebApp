package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.CollectionManager;
import models.MovieManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class RecommendationsController extends Controller {
    private final MovieManager movieManager;
    private final Logger logger;
    private final String TOP_ROLLING = "Top 20 Movies in the Last 90 days";
    private final String FRIEND_TOP = "Top 20 Movies Based on Your Friends' Activity";
    private final String TOP_5_MONTHLY = "Top 5 Movies in the Last Month";
    private final String FOR_YOU = "Recommendations For You";

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public RecommendationsController(MovieManager movieManager, CollectionManager collectionManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Display a list of the 20 most popular movies in the last 90 days
     * @param request the request from the client
     * @return a template displaying the results
     */
    public CompletionStage<Result> topMoviesRolling(Http.Request request) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.get90DayRolling().thenApply(movies ->
                    ok(views.html.recommendations.render(user, movies, TOP_ROLLING, session))
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(ok(views.html.recommendations.render(null, null, "", session)))
        );

    }

    /**
     * Display a list of the 20 most popular movies among the current user's friends
     * @param request the request from the client
     * @return a template displaying the results
     */
    public CompletionStage<Result> friendTopMovies(Http.Request request) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.getFriendTopMovies().thenApply(movies ->
                    ok(views.html.recommendations.render(user, movies, FRIEND_TOP, session))
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(ok(views.html.recommendations.render(null, null, "", session)))
        );
    }

    /**
     * Display a list of the 5 most popular movies this month
     * @param request the request from the client
     * @return a template displaying the results
     */
    public CompletionStage<Result> top5LastMonth(Http.Request request) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.getTop5LastMonth().thenApply(movies ->
                    ok(views.html.recommendations.render(user, movies, TOP_5_MONTHLY, session))
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(ok(views.html.recommendations.render(null, null, "", session)))
        );
    }

    /**
     * Display a list of the 5 most popular movies this month
     * @param request the request from the client
     * @return a template displaying the results
     */
    public CompletionStage<Result> forYou(Http.Request request) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return movieManager.getForYou(user.getUserID()).thenApply(movies ->
                    ok(views.html.recommendations.render(user, movies, FOR_YOU, session))
            );
        }).orElseGet(() ->
                CompletableFuture.completedFuture(ok(views.html.recommendations.render(null, null, "", session)))
        );
    }
}
