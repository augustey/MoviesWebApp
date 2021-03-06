package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.AccountManager;
import play.libs.Json;
import play.mvc.*;
import util.Message;

import javax.inject.Inject;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * Class for handling sign in requests and storing user info in a session
 * on GET /signin and POST /signin
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class SignInController {
    private final AccountManager accountManager;
    public static final String USER_KEY = "user";
    private final Message SIGNIN_FAIL = Message.error("Username or password was incorrect");

    /**
     * Constructor for the Sign in controller
     * @param accountManager AccountManager object used for various user related behaviors
     */
    @Inject
    SignInController(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * Display the sign in page on GET /signin
     * @return a response containing the sign in page content
     */
    public Result signIn(Http.Request request, boolean error) {
        Message message = error ? SIGNIN_FAIL : Message.info("");
        return ok(views.html.signin.render(message, request.session()));
    }

    /**
     * Verify a user on POST /signin and if the credentials are valid, store their information in
     * a session.
     * @param request HTTP request
     * @return A redirect to the homepage upon successful sign in otherwise the home page
     */
    public CompletionStage<Result> verify(Http.Request request) {
        Map<String, String[]> params = request.body().asFormUrlEncoded();
        String username = params.get("username")[0];
        String password = params.get("password")[0];

        return accountManager.getUser(username, password).thenApply(user -> {
            if(user != null) {
                JsonNode userNode = Json.toJson(user);

                return redirect("/").addingToSession(request, USER_KEY, userNode.toString());
            }
            else {
                return redirect("/signin?error=1");
            }
        });
    }
}
