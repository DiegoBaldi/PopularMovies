package diegobaldi.popularmovies.api;

import diegobaldi.popularmovies.models.MovieList;
import diegobaldi.popularmovies.models.ReviewList;
import diegobaldi.popularmovies.models.VideoList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by diego on 10/11/2016.
 */

public interface TheMovieDBService {

    @GET("movie/{typeOrCategory}")
    Call<MovieList> getMovies(@Path("typeOrCategory") String categories, @Query("api_key") String apiKey);

    @GET("movie/{movieId}/videos")
    Call<VideoList> getVideos(@Path("movieId") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{movieId}/reviews")
    Call<ReviewList> getReviews(@Path("movieId") int movieId, @Query("api_key") String apiKey);
}
