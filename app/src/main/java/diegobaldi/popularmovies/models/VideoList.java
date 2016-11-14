package diegobaldi.popularmovies.models;

import java.util.List;

/**
 * Created by diego on 10/11/2016.
 */

public class VideoList {
    public int id;
    public List<Video> results;

    public List<Video> getResults(){
        return results;
    }
}
