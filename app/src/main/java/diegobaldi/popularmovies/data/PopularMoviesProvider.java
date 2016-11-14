package diegobaldi.popularmovies.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Diego Baldi on 08/11/2016.
 */

@ContentProvider(authority = PopularMoviesProvider.AUTHORITY, database = PopularMoviesDatabase.class)
public final class PopularMoviesProvider {

    public static final String AUTHORITY = "diegobaldi.popularmovies.data.PopularMoviesProvider";

    @TableEndpoint(table = PopularMoviesDatabase.FAVORITES)
    public static class Favorites {

        @ContentUri(
                path = "favorites",
                type = "vnd.android.cursor.dir/list",
                defaultSort = FavoriteColumns.CREATED_AT + " ASC")
        public static final Uri FAVORITES = Uri.parse("content://" + AUTHORITY + "/favorites");
    }
}