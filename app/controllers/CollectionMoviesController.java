package controllers;

import models.CollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

/**
 * This controller handles routes and actions related to a specific user collection
 * @author Yaqim Auguste (yaa6681@rit.edu)
 */
public class CollectionMoviesController {
    private final CollectionManager collectionManager;
    private final Logger logger;
    private final String SUCCESSFUL_ADD = "Movie successfully added to collection!";

    /**
     * Constructor for CollectionMoviesController
     * @param collectionManager CollectionManager used for executing collection related database operations
     */
    @Inject
    public CollectionMoviesController(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Add a movie to a collection
     * @param request The request from the client
     * @param collectionID The collection to insert into
     * @param movieID The movie to add
     * @return A redirect to last page
     */
    public CompletionStage<Result> add(Http.Request request, int collectionID, int movieID) {
        return collectionManager.insertIntoCollection(collectionID, movieID).thenApply(
                result -> redirect("/movie/"+movieID).flashing(MovieController.MOVIE_SUCCESS, SUCCESSFUL_ADD)
        );
    }

    /**
     * REmove a movie to a collection
     * @param request The request from the client
     * @param collectionID The collection to insert into
     * @param movieID The movie to add
     * @return A redirect to last page
     */
    public CompletionStage<Result> remove(int collectionID, int movieID) {
        return collectionManager.deleteFromCollection(collectionID, movieID).thenApply(
                result -> redirect("/collections/"+collectionID)
        );
    }

    /**
     * Show the collections page for a specific collection
     * @param collectionID The id of teh collection to display
     * @return A template containing the collectionMovies page
     */
    public CompletionStage<Result> collectionMovies(Http.Request request, int collectionID) {
        Http.Session session = request.session();
        return collectionManager.getCollectionMovies(collectionID).thenApply(movies ->
                ok(views.html.collectionmovies.render(collectionID, movies, session))
        );
    }
}
