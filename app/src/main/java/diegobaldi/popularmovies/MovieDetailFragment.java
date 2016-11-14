package diegobaldi.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import diegobaldi.popularmovies.adapters.ReviewsAdapter;
import diegobaldi.popularmovies.adapters.VideosAdapter;
import diegobaldi.popularmovies.api.TheMovieDBService;
import diegobaldi.popularmovies.data.FavoriteColumns;
import diegobaldi.popularmovies.data.PopularMoviesProvider;
import diegobaldi.popularmovies.models.Movie;
import diegobaldi.popularmovies.models.Review;
import diegobaldi.popularmovies.models.ReviewList;
import diegobaldi.popularmovies.models.Video;
import diegobaldi.popularmovies.models.VideoList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static diegobaldi.popularmovies.MovieListActivity.BASE_URL;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = "MovieDetailFragment";

    private TheMovieDBService mTheMovieDBService;

    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;

    private List<Video> videos = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    private ProgressBar mVideoProgress;
    private ProgressBar mReviewProgress;

    private TextView mEmptyVideos;
    private TextView mEmptyReviews;

    private Movie movie;

//    private OnDataPass mDataPasser;

    private String mTrailerURL;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || getActivity().findViewById(R.id.movie_detail_container)!=null){
            setHasOptionsMenu(true);
        }
        if(savedInstanceState!=null && savedInstanceState.containsKey("movie")){
            movie = savedInstanceState.getParcelable("movie");
        }else if (getArguments().containsKey("movie")) {
            // Load the movie content.
            movie = getArguments().getParcelable("movie");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelable("movie", movie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        mVideoProgress = (ProgressBar) rootView.findViewById(R.id.videos_progress);
        mReviewProgress = (ProgressBar) rootView.findViewById(R.id.reviews_progress);

        mEmptyReviews = (TextView) rootView.findViewById(R.id.empty_reviews);
        mEmptyVideos = (TextView) rootView.findViewById(R.id.empty_videos);

        RecyclerView videosRecyclerView = (RecyclerView) rootView.findViewById(R.id.videos_rv);
        videosRecyclerView.setNestedScrollingEnabled(false);
        videosRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(linearLayoutManager);
        mVideosAdapter = new VideosAdapter(getActivity(), videos);
        videosRecyclerView.setAdapter(mVideosAdapter);

        RecyclerView reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_rv);
        reviewsRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManagerReviews = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        reviewsRecyclerView.setLayoutManager(linearLayoutManagerReviews);
        mReviewsAdapter = new ReviewsAdapter(getActivity(), reviews);
        reviewsRecyclerView.setAdapter(mReviewsAdapter);

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            ImageView backDrop = (ImageView) appBarLayout.findViewById(R.id.backdrop_image);
            Picasso.with(activity).load(String.format(getString(R.string.backdrop_url),movie.getBackdropPath())).into(backDrop);

        }
        if(movie!=null){
            TextView title = (TextView) rootView.findViewById(R.id.movie_title);
            title.setText(movie.getTitle());
            ImageView poster = (ImageView) rootView.findViewById(R.id.poster_thumb);
            Picasso.with(activity).load(String.format(getString(R.string.poster_url),movie.getPosterPath())).into(poster);
            TextView rating = (TextView) rootView.findViewById(R.id.user_rating);

            String ratingString = String.valueOf(movie.getVoteAverage())+getString(R.string.max_vote);
            Spannable spannable = new SpannableString(ratingString);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this.getActivity(), R.color.colorAccent)), ratingString.indexOf(String.valueOf(movie.getVoteAverage())), String.valueOf(movie.getVoteAverage()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            rating.setText(spannable, TextView.BufferType.SPANNABLE);
            TextView release = (TextView) rootView.findViewById(R.id.release_date);
            release.setText(dateCooler(movie.getReleaseDate()));
            TextView synopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
            synopsis.setText(movie.getOverview());

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            mTheMovieDBService = retrofit.create(TheMovieDBService.class);
            if(isOnline()){
                getReviews();
                getVideos();
            } else {
                Toast.makeText(getActivity(),getString(R.string.no_connection_msg), Toast.LENGTH_SHORT).show();
            }
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_share){
            if(mTrailerURL.equalsIgnoreCase("")){
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");

                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, movie.getTitle());
                share.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_without_trailer), movie.getTitle()));
                startActivity(Intent.createChooser(share, getString(R.string.share_title_chooser)));
            } else {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, movie.getTitle());
                share.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_trailer), mTrailerURL));
                startActivity(Intent.createChooser(share, getString(R.string.share_trailer_chooser)));
            }
        } else if(id == R.id.action_favorite){
            new Thread(new Runnable() {
                @Override public void run() {
                    Long tsLong = System.currentTimeMillis()/1000;
                    Integer ts = (int) (long) tsLong;
                    ContentValues cv = new ContentValues();
                    cv.put(FavoriteColumns.THE_MOVIE_DB_ID, movie.getId());
                    cv.put(FavoriteColumns.TITLE, movie.getTitle());
                    cv.put(FavoriteColumns.POSTER_URL, movie.getPosterPath());
                    cv.put(FavoriteColumns.BACKDROP_URL, movie.getBackdropPath());
                    cv.put(FavoriteColumns.SYNOPSIS, movie.getOverview());
                    cv.put(FavoriteColumns.USER_RATING, movie.getVoteAverage());
                    cv.put(FavoriteColumns.RELEASE_DATE, movie.getReleaseDate());
                    cv.put(FavoriteColumns.CREATED_AT, ts);
                    final Uri uri_inserted = getActivity().getContentResolver().insert(PopularMoviesProvider.Favorites.FAVORITES, cv);
                    if(uri_inserted!=null){
                        final String id = uri_inserted.getLastPathSegment();
                        Snackbar snackbar = Snackbar
                                .make(getView(), "Movie added to favorites!", Snackbar.LENGTH_LONG)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        getActivity().getContentResolver().delete(PopularMoviesProvider.Favorites.FAVORITES, "_ID = ?", new String[]{id});
                                    }
                                });

                        // Changing message text color
                        snackbar.setActionTextColor(Color.RED);
                        snackbar.show();
                    }
                }
            }).start();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline(){
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void getReviews(){
        mReviewProgress.setVisibility(View.VISIBLE);
        Call<ReviewList> call= mTheMovieDBService.getReviews(movie.getId(), BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<ReviewList>() {
            @Override
            public void onResponse(Call<ReviewList> call, Response<ReviewList> response) {
                mReviewProgress.setVisibility(View.GONE);
                if(response.code()==200) {
                    ReviewList reviewList = response.body();
                    reviews.clear();
                    reviews.addAll(reviewList.getResults());
                }
                if(reviews.size()==0)
                    mEmptyReviews.setVisibility(View.VISIBLE);
                else
                    mEmptyReviews.setVisibility(View.GONE);

                mReviewsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ReviewList> call, Throwable t) {
                mReviewProgress.setVisibility(View.GONE);
            }
        });
    }
    public void getVideos(){
        mVideoProgress.setVisibility(View.VISIBLE);
        Call<VideoList> call= mTheMovieDBService.getVideos(movie.getId(), BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<VideoList>() {
            @Override
            public void onResponse(Call<VideoList> call, Response<VideoList> response) {
                mVideoProgress.setVisibility(View.GONE);
                if(response.code()==200) {
                    VideoList videoList = response.body();
                    videos.clear();
                    videos.addAll(videoList.getResults());
                }
                if(videos.size()==0)
                    mEmptyVideos.setVisibility(View.VISIBLE);
                else{
                    mEmptyVideos.setVisibility(View.GONE);
                    mTrailerURL = String.format(getActivity().getString(R.string.trailer_link), videos.get(0).getKey());
                }
                mVideosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<VideoList> call, Throwable t) {
                mVideoProgress.setVisibility(View.GONE);
            }
        });
    }

    // Method to beautify the date string
    private String dateCooler(String date){
        String outputDate = date;
        //TMDB give us the date formatted as yyyy-MM-dd
        SimpleDateFormat inputFormatter = new SimpleDateFormat(getString(R.string.tmdb_date_format), Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat(getString(R.string.app_release_date_format), Locale.getDefault());
        try {
            Date inputDate = inputFormatter.parse(date);
            outputDate = outputFormat.format(inputDate);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "date parsing failed: "+date);
        }
        return outputDate;
    }
}
