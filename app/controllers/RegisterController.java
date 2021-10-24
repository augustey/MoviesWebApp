package controllers;

import models.AccountManager;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import util.Message;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * Handler for registration on GET /register and POST /register
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class RegisterController {
    private final AccountManager accountManager;

    /**
     * Constructor for RegisterController
     * @param accountManager AccountManger object for performing user related database operations
     */
    @Inject
    RegisterController(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * Display the register page on GET /register
     * @param request Request object
     * @return Resposnse containing html content
     */
    public Result register(Request request) {
        Http.Session session = request.session();
        String errorKey = Message.Type.ERROR.toString();

        return session.get(errorKey).map(error ->
            ok(views.html.register.render(Message.error(error), session)).removingFromSession(request, errorKey)
        ).orElseGet(() -> ok(views.html.register.render(Message.info(""), session)));

    }

    /**
     * Attempt to create a new user based on information from a submitted form
     * @param request Request object
     * @return A redirect to either home or register
     */
    public CompletionStage<Result> createUser(Request request) {
        Map<String, String[]> params = request.body().asFormUrlEncoded();
        String username = params.get("username")[0];
        String password = params.get("password")[0];
        String email = params.get("email")[0];
        String firstName = params.get("firstname")[0];
        String lastName = params.get("lastname")[0];

        return accountManager.createUser(username,password, email, firstName, lastName)
        .thenApply(message -> {
            String messageType = message.getType().toString();
            String messageText = message.getText();

            if(message.succeeded())
                return redirect("/");
            else
                return redirect("/register").addingToSession(request, messageType, messageText);
        });
    }
}
