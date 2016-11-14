package diegobaldi.popularmovies.models;

import java.util.List;

/**
 * Created by diego on 10/11/2016.
 */

public class ReviewList {
    public int id;
    public int page;
    public List<Review> results;
    public int totalPages;
    public int totalResults;

    public List<Review> getResults(){
        return results;
    }
}
