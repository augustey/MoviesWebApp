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
import java.util.concurrent.CompletionStage;


public class RecsController extends Controller {
    private final MovieManager movieManager;
    private final Logger logger;

    /**
     * Constructor
     *
     * @param movieManager the movieManager object used
     */
    @Inject
    public RecsController(MovieManager movieManager, CollectionManager collectionManager) {
        this.movieManager = movieManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Loads the recommendation page
     * @return the response body
     */
    public Result loadRecs(Http.Request request) {
        Http.Session session = request.session();

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return ok(views.html.recs.render(user, session));
        }).orElseGet(() ->
                ok(views.html.recs.render(null, session))
                );
    }
}
