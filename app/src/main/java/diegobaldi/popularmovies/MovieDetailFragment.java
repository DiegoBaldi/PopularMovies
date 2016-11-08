package diegobaldi.popularmovies;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import diegobaldi.popularmovies.models.Movie;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = "MovieDetailFragment";

    private Movie movie;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            ImageView backDrop = (ImageView) appBarLayout.findViewById(R.id.backdrop_image);
            Picasso.with(activity).load(movie.backdropURL).into(backDrop);

        }
        if(movie!=null){
            TextView title = (TextView) rootView.findViewById(R.id.movie_title);
            title.setText(movie.title);
            ImageView poster = (ImageView) rootView.findViewById(R.id.poster_thumb);
            Picasso.with(activity).load(movie.posterURL).into(poster);
            TextView rating = (TextView) rootView.findViewById(R.id.user_rating);

            String ratingString = String.valueOf(movie.userRating)+getString(R.string.max_vote);
            Spannable spannable = new SpannableString(ratingString);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this.getActivity(), R.color.colorAccent)), ratingString.indexOf(String.valueOf(movie.userRating)), String.valueOf(movie.userRating).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            rating.setText(spannable, TextView.BufferType.SPANNABLE);
            TextView release = (TextView) rootView.findViewById(R.id.release_date);
            release.setText(dateCooler(movie.releaseDate));
            TextView synopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
            synopsis.setText(movie.synopsis);
        }


        return rootView;
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
