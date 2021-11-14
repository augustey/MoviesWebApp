package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.AccountManager;
import models.CollectionManager;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ProfileController extends Controller{
    private final AccountManager accountManager;
    private final Logger logger;

    @Inject
    public ProfileController(AccountManager accountManager) {
        this.accountManager = accountManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletionStage<Result> loadProfile(Http.Request request) {
        Http.Session session = request.session();
        return session.get(SignInController.USER_KEY).map(userJson -> {
            JsonNode userNode = Json.parse(userJson);
            User user = Json.fromJson(userNode, User.class);

            return accountManager.getFollowers(user.getUserID()).thenCombine(accountManager.getFollowing(user.getUserID()),
                    (followers, following) -> {return ok(views.html.profile.render(user, followers, following, session)});
        }).orElseGet(() -> CompletableFuture.completedFuture(ok(views.html.profile.render(null, null, session))));
    }
}
