package diegobaldi.popularmovies;

import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import diegobaldi.popularmovies.data.FavoriteColumns;
import diegobaldi.popularmovies.data.PopularMoviesProvider;
import diegobaldi.popularmovies.models.Movie;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity {

    private Movie movie;
    View mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_to_favorite);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        final Uri uri_inserted = getContentResolver().insert(PopularMoviesProvider.Favorites.FAVORITES, cv);
                        if(uri_inserted!=null){
                            final String id = uri_inserted.getLastPathSegment();
                            Snackbar snackbar = Snackbar
                                    .make(mCoordinatorLayout, "Movie added to favorites!", Snackbar.LENGTH_LONG)
                                    .setAction("CANCEL", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            getContentResolver().delete(PopularMoviesProvider.Favorites.FAVORITES, "_ID = ?", new String[]{id});
                                        }
                                    });

                            // Changing message text color
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                        }
                    }
                }).start();
            }
        });

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            movie = getIntent().getParcelableExtra("movie");
            arguments.putParcelable("movie", movie);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Bundle arguments = new Bundle();
            movie = savedInstanceState.getParcelable("movie");
            arguments.putParcelable("movie", movie);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelable("movie", movie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back

            //https://developer.android.com/reference/android/support/v4/app/FragmentActivity.html#supportFinishAfterTransition()
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
