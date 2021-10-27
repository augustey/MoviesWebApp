package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.CollectionManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * Class for serving a user their list of collections on GET /collections
 * @author Yaqim Auguste (yaa681@rit.edu)
 */
public class CollectionController {
    private final CollectionManager collectionManager;
    private final Logger logger;

    /**
     * Constructor for CollectionController
     * @param collectionManager the CollectionManager used to perform collection related
     *                          database transactions
     */
    @Inject
    public CollectionController(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletionStage<Result> createCollection(Http.Request request, String name) {
        Http.Session session = request.session();
        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return collectionManager.createCollection(user.getUserID(), name).thenApply(collections ->
                    redirect("/collections"));
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/")));
    }

    /**
     * Serve a template containing a table of the user's collection
     * @param request HTTP request sent to the server
     * @return Response containing the collections template
     */
    public CompletionStage<Result> collections(Http.Request request) {
        Http.Session session = request.session();
        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return collectionManager.getCollections(user.getUserID()).thenApply(collections ->
                ok(views.html.collections.render(collections, session)));
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/")));
    }
}
