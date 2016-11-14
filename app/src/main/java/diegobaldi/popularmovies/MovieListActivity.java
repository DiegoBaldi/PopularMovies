package diegobaldi.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.util.ArrayList;
import java.util.List;

import diegobaldi.popularmovies.api.TheMovieDBService;
import diegobaldi.popularmovies.data.FavoriteColumns;
import diegobaldi.popularmovies.data.PopularMoviesProvider;
import diegobaldi.popularmovies.models.Movie;
import diegobaldi.popularmovies.models.MovieList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static diegobaldi.popularmovies.R.id.poster;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URL_LOADER = 0;

    private static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185";

    private static final int TWO_PANE_POSTER_WIDTH = 450;
    private static final int POSTER_WIDTH = 160;

    public String sortBy;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private MovieRecyclerViewAdapter mAdapter;

    private ProgressBar mMoviesProgress;
    private TextView mEmptyMovies;

    public String[] mProjection = {
            FavoriteColumns._ID,
            FavoriteColumns.THE_MOVIE_DB_ID,
            FavoriteColumns.TITLE,
            FavoriteColumns.POSTER_URL,
            FavoriteColumns.BACKDROP_URL,
            FavoriteColumns.SYNOPSIS,
            FavoriteColumns.RELEASE_DATE,
            FavoriteColumns.USER_RATING,
            FavoriteColumns.CREATED_AT
    };

    private final int _ID = 0;
    private final int THE_MOVIE_DB_ID = 1;
    private final int TITLE = 2;
    private final int POSTER_URL = 3;
    private final int BACKDROP_URL = 4;
    private final int SYNOPSIS = 5;
    private final int RELEASE_DATE = 6;
    private final int USER_RATING = 7;
    private final int CREATED_AT = 8;

    Retrofit mRetrofit;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private List<Movie> movies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        
        setSharedPreferencesListener();

        Log.d(LOG_TAG, "called OnCreate");

        mMoviesProgress = (ProgressBar) findViewById(R.id.movies_progress);
        mEmptyMovies = (TextView) findViewById(R.id.empty_movies);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey("movies")){
            tryUpdateMovies();
        }
        else{
            sortBy = savedInstanceState.getString("sort_by");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String currentSortBy = prefs.getString(getString(R.string.pref_search_type_key), getString(R.string.pref_search_type_default));
            if(sortBy==null || !currentSortBy.equalsIgnoreCase(sortBy))
                tryUpdateMovies();
            else
                movies = savedInstanceState.getParcelableArrayList("movies");
        }
        View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setSharedPreferencesListener() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals(getString(R.string.pref_search_type_key)))
                    tryUpdateMovies();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList("movies", new ArrayList<>(movies));
        outState.putString("sort_by", sortBy);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void tryUpdateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = prefs.getString(getString(R.string.pref_search_type_key), getString(R.string.pref_search_type_default));
        if(isOnline() && !sortBy.equalsIgnoreCase("favorites")){
            mMoviesProgress.setVisibility(View.VISIBLE);

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            TheMovieDBService theMovieDBService = mRetrofit.create(TheMovieDBService.class);

            Call<MovieList> call=theMovieDBService.getMovies(sortBy, BuildConfig.THE_MOVIE_DB_API_KEY);
            call.enqueue(new Callback<MovieList>() {
                @Override
                public void onResponse(Call<MovieList> call, Response<MovieList> response) {
                    mMoviesProgress.setVisibility(View.GONE);
                    if(response.code()==200) {
                        MovieList movieList = response.body();
                        movies.clear();
                        movies.addAll(movieList.getResults());
                    }
                    if(movies.size()==0)
                        mEmptyMovies.setVisibility(View.VISIBLE);
                    else
                        mEmptyMovies.setVisibility(View.GONE);

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<MovieList> call, Throwable t) {
                    mMoviesProgress.setVisibility(View.GONE);
                }
            });
        } else if(sortBy.equalsIgnoreCase("favorites")){
            getSupportLoaderManager().initLoader(URL_LOADER, null, this).forceLoad();
        }
        else{
            Toast.makeText(this,getString(R.string.no_connection_msg), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //Read in https://developer.android.com/training/material/lists-cards.html to improve performance
        recyclerView.setHasFixedSize(false);

        //Code to set a GridLayoutManager with columns each row instead of the default linearLayout
        mAdapter = new MovieRecyclerViewAdapter(this, movies);
        recyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, getSpanSize());
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private int getSpanSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int maxElements = 1;
        if(mTwoPane){
            maxElements = (int) Math.floor((metrics.widthPixels/metrics.density)/TWO_PANE_POSTER_WIDTH);

        } else{
            maxElements = (int) Math.floor((metrics.widthPixels/metrics.density)/POSTER_WIDTH);
        }
        return maxElements;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        PopularMoviesProvider.Favorites.FAVORITES,        // Table to query
                        mProjection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(sortBy.equalsIgnoreCase("favorites")){
            mMoviesProgress.setVisibility(View.VISIBLE);
            movies.clear();
            if(data.getCount()>0){
                while(data.moveToNext()){
                    movies.add(new Movie(data.getInt(THE_MOVIE_DB_ID),
                            data.getString(POSTER_URL),
                            data.getString(TITLE),
                            data.getString(BACKDROP_URL),
                            data.getString(RELEASE_DATE),
                            data.getString(SYNOPSIS),
                            data.getDouble(USER_RATING)));
                }
            }
            if(movies.size()==0)
                mEmptyMovies.setVisibility(View.VISIBLE);
            else
                mEmptyMovies.setVisibility(View.GONE);

            mMoviesProgress.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        } else {
            data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
    }

    public class MovieRecyclerViewAdapter
            extends RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;

        private Context mContext;

        public MovieRecyclerViewAdapter(Context context, List<Movie> items) {
            mValues = items;
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mMovie = mValues.get(position);

            String posterURL = POSTER_BASE_URL + holder.mMovie.getPosterPath();

            Picasso.with(mContext).load(posterURL).into(holder.mPoster);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable("movie", holder.mMovie);
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra("movie", holder.mMovie);
                        if(sortBy.equalsIgnoreCase("favorites")){
                            startActivity(intent);
                        } else {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MovieListActivity.this, v, "poster_thumb");
                            startActivity(intent, options.toBundle());
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mPoster;
            public Movie mMovie;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mPoster = (ImageView) view.findViewById(poster);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mMovie.toString() + "'";
            }
        }
    }
}
