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
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

/**
 * Class for display and searching for lists of users on /Users
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class UsersController {
    private final AccountManager accountManager;
    private final Logger logger;

    /**
     * Constructor for UsersController
     * @param accountManager AccountManager object used for various user related behaviors
     */
    @Inject
    public UsersController(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Display the list of users based on a query
     * @param request HTTP request
     * @return a response containing the users template
     */
    public CompletionStage<Result> users(Http.Request request) {
        Http.Session session = request.session();
        int  userID;

        try {
            JsonNode userNode = Json.parse(session.get(SignInController.USER_KEY).get());
            User user = Json.fromJson(userNode, User.class);
            userID = user.getUserID();
        }
        catch (Exception e) {
            userID = -1;
        }

        logger.info("USERID: "+userID);

        return accountManager.getAllUsers(userID).thenApply(users ->
            ok(views.html.users.render(users, session))
        );
    }
}
