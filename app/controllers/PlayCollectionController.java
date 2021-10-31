package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.CollectionManager;
import models.Movie;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import util.Message;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * Class for playing a collection
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class PlayCollectionController {
    private final CollectionManager collectionManager;
    private final Logger logger;

    /**
     * Constructor for PlayCollectionController
     * @param collectionManager the CollectionManager used to perform collection related
     *                          database transactions
     */
    @Inject
    public PlayCollectionController(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    /**
     * Play a collection
     * @param request A request from the client
     * @param collectionID The collection currently playing
     * @param page The page of the collection
     * @return
     */
    public CompletionStage<Result> playCollection(Http.Request request, int collectionID, int page) {
        Http.Session session = request.session();
        String error = request.flash().get(MovieController.MOVIE_ERROR).orElse("");
        String success = request.flash().get(MovieController.MOVIE_SUCCESS).orElse("");

        final Message message = error.equals("") ? Message.info(success) : Message.error(error);

        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return collectionManager.getCollectionMovies(collectionID).thenApply(movies -> {
                boolean inRange = page > 0 && page <= movies.size();
                Movie mov = inRange ? movies.get(page-1) : null;
                return ok(views.html.playcollection.render(request, collectionID, page, inRange, mov, message, session));
            });
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/")));
    }
}
