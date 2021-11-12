package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class ProfileController extends Controller{

    public Result loadProfile(Http.Request request) {
        Http.Session session = request.session();
        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return ok(views.html.profile.render(user, session));
        }).orElseGet(() -> ok(views.html.profile.render(null, session)));
    }
}
