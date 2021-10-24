package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.AccountManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.redirect;

/**
 * Controller for logging out a user session
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class LogoutController {
    private final AccountManager accountManager;
    private final Logger logger;

    /**
     * Constructor for LogoutController
     * @param accountManager AccountManger for performing user related database operations
     */
    @Inject
    LogoutController(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Logout the user from the session
     * @return redirect response to homepage
     */
    public CompletionStage<Result> logout(Http.Request request) {
        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return accountManager.setLastAccess(user.getUsername()).thenApply(result -> {
                logger.info("Successfully logged out user "+user.getUsername());
                return redirect("/").withNewSession();
            });
        }).orElseGet(() -> CompletableFuture.completedFuture(redirect("/")));
    }
}
