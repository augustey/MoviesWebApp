package controllers;

import play.mvc.*;
import static play.mvc.Results.redirect;

/**
 * Controller for logging out a user session
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class LogoutController {

    /**
     * Logout the user from the session
     * @return redirect response to homepage
     */
    public Result logout() {
        return redirect("/").withNewSession();
    }
}
