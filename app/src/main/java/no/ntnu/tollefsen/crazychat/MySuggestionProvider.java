package no.ntnu.tollefsen.crazychat;

import android.content.SearchRecentSuggestionsProvider;
import android.util.Log;

/**
 * Created by mikael on 07.09.2016.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "no.ntnu.tollefsen.crazychat.MySuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;  // Configures database to record recent queries

    public MySuggestionProvider() {
        Log.i("Search","MySuggestionProvider");
        setupSuggestions(AUTHORITY,MODE);
    }
}

