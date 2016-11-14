package diegobaldi.popularmovies.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Diego Baldi on 08/11/2016.
 */

@Database(version = PopularMoviesDatabase.VERSION)
public final class PopularMoviesDatabase {

    public static final int VERSION = 2;

    @Table(FavoriteColumns.class) public static final String FAVORITES = "favorites";
}
