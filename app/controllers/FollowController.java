package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.AccountManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.redirect;

/**
 * Class for handling following and unfollowing a user
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class FollowController {
    private final AccountManager accountManager;
    private final Logger logger;

    /**
     * Constructor for FollowController
     * @param accountManager AccountManager object used for various user related behaviors
     */
    @Inject
    public FollowController(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Follow a user
     * @param request HTTP Request
     * @param followedID user to follow
     * @return A redirect to the users page
     */
    public CompletionStage<Result> follow(Http.Request request, int followedID) {
        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);
            int userID = user.getUserID();

            return accountManager.followUser(userID, followedID).thenApply(result ->
                redirect("/users")
            );
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/users")));
    }

    /**
     * unfollow a user
     * @param request HTTP Request
     * @param followedID user to unfollow
     * @return A redirect to the users page
     */
    public CompletionStage<Result> unfollow(Http.Request request, int followedID) {
        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);
            int userID = user.getUserID();

            return accountManager.unfollowUser(userID, followedID).thenApply(result ->
                    redirect("/users")
            );
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/users")));
    }
}
