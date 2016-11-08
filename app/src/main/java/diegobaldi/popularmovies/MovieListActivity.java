package diegobaldi.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import diegobaldi.popularmovies.models.Movie;

import static diegobaldi.popularmovies.R.id.poster;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    private static final int TWO_PANE_POSTER_WIDTH = 450;
    private static final int POSTER_WIDTH = 160;

    public String sortBy;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private MovieRecyclerViewAdapter mAdapter;
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
        if(isOnline()){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            sortBy = prefs.getString(getString(R.string.pref_search_type_key), getString(R.string.pref_search_type_default));
            new FetchMoviesTask().execute(sortBy);
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

            Picasso.with(mContext).load(holder.mMovie.posterURL).into(holder.mPoster);

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
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MovieListActivity.this, v, "poster_thumb");
                        intent.putExtra("movie", holder.mMovie);
                        startActivity(intent, options.toBundle());
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


    //Method taken from the Sunshine App's lessons
    private class FetchMoviesTask extends AsyncTask<String, Void,  Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            String language = "en-US";

            try {
                // Construct the URL for the The Movie Database query
                // Possible parameters are available at TMDB's API page, at
                // https://developers.themoviedb.org/3/movies
                final String MOVIE_DB_BASE_URL = "https://api.themoviedb.org/3/movie/"+params[0]+"?";
                final String API_KEY_PARAM = "api_key";
                final String LANGUAGE_PARAM = "language";

                Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .appendQueryParameter(LANGUAGE_PARAM, language)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to The Movie DB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        //Method taken from the Sunshine App's lessons
        private Movie[] getMoviesFromJson(String moviesJsonStr)
        throws  JSONException{
            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_LIST = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "title";
            final String TMDB_POSTER = "poster_path";
            final String TMDB_BACKDROP = "backdrop_path";
            final String TMDB_SYNOPSIS = "overview";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_USER_RATING = "vote_average";

            JSONObject forecastJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = forecastJson.getJSONArray(TMDB_LIST);

            Movie[] movies = new Movie[movieArray.length()];
            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject movieData = movieArray.getJSONObject(i);
                movies[i] = new Movie(movieData.getInt(TMDB_ID),
                    movieData.getString(TMDB_TITLE),
                    movieData.getString(TMDB_POSTER),
                    movieData.getString(TMDB_BACKDROP),
                    movieData.getString(TMDB_SYNOPSIS),
                    movieData.getDouble(TMDB_USER_RATING),
                    movieData.getString(TMDB_RELEASE_DATE));
            }
            return movies;
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                movies.clear();
                Collections.addAll(movies, result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
