package diegobaldi.popularmovies.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by Diego Baldi on 08/11/2016.
 */

public interface FavoriteColumns {
    @DataType(INTEGER) @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(INTEGER) @NotNull @Unique(onConflict = ConflictResolutionType.REPLACE)
    String THE_MOVIE_DB_ID = "the_movie_db_id";

    @DataType(TEXT) @NotNull @Unique(onConflict = ConflictResolutionType.REPLACE)
    String TITLE = "title";

    @DataType(TEXT) @NotNull
    String POSTER_URL = "poster_url";

    @DataType(INTEGER) @NotNull
    String CREATED_AT = "created_at";
}
