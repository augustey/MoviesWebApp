package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.libs.Json;
import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class HomeController extends Controller {

    /**
     * Render the index page
     * @return The response body
     */
    public Result index(Http.Request request) {
        return request.session().get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return ok(views.html.index.render(user));
        }).orElseGet(() -> ok(views.html.index.render(null)));
    }

}
