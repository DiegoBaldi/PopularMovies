package diegobaldi.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Diego Baldi on 28/09/2016.
 */
public class Movie implements Parcelable {
    public final int id;
    public final String title;
    public final String posterURL;
    public final String backdropURL;
    public final String synopsis;
    public Double userRating;
    public final String releaseDate;

    public Movie(int id, String title, String posterURL, String backdropURL, String synopsis, double userRating, String releaseDate) {
        this.id = id;
        this.title = title;
        this.posterURL = "http://image.tmdb.org/t/p/w185"+posterURL;
        this.backdropURL = "http://image.tmdb.org/t/p/w500"+backdropURL;
        this.synopsis = synopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        posterURL = in.readString();
        backdropURL = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(posterURL);
        parcel.writeString(backdropURL);
        parcel.writeString(synopsis);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseDate);
    }
}
