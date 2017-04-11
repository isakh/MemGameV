package ws.isak.memgamev.themes;

import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Bitmap;

import ws.isak.memgamev.common.Shared;
import ws.isak.memgamev.utils.Utils;
import ws.isak.memgamev.common.CardData;
import ws.isak.memgamev.R;

/*
 * The MatchThemes class describes each of the themes.  From the MatchTheme description, each will have a name
 * and ID, a boolean to reflect whether paired image files for the matchTheme are duplicates of the same
 * image file or pairs of images of the same species, and a list of card objects that contain the
 * Urls for the image(s) and audio that match the species described by the card.
 *
 * TODO should we come up with a text list of species of interest so that when image and audio files
 * TODO ... are created for each locale, we can run a find/replace on file names with a list of the
 * TODO ... relevant species for the locale??
 *
 * @author isak
 */

public class MatchThemes {

	private static final String TAG = "MatchThemes";
    public static String URI_DRAWABLE = "drawable://";
    public static String URI_AUDIO = "raw://";

    public static MatchTheme createBlankTheme() {
        MatchTheme matchTheme = new MatchTheme();
        matchTheme.themeID = 0;
        matchTheme.name = Shared.context.getString(R.string.match_themes_blank_name);
        matchTheme.pairedImagesDiffer = false;
        matchTheme.backgroundImageUrl = URI_DRAWABLE + "back_blank";
        matchTheme.cardObjs = new ArrayList<CardData>();
        Collections.copy(Shared.cardDataList, matchTheme.cardObjs);      //FIXME this isn't necessary - can replace all instances of matchTheme.cardObjs with Shared.cardDataList
        return matchTheme;
    }

	public static MatchTheme createBirdsTheme() {
		MatchTheme matchTheme = new MatchTheme();
		matchTheme.themeID = 1;
		matchTheme.name = Shared.context.getString(R.string.match_themes_birds_name);
		matchTheme.pairedImagesDiffer = true;
		matchTheme.backgroundImageUrl = URI_DRAWABLE + "back_birds";
		matchTheme.cardObjs = new ArrayList<CardData>();		//ArrayList of type CardData
        Collections.copy(Shared.cardDataList, matchTheme.cardObjs);
		return matchTheme;
	}

	public static MatchTheme createSpectrogramsTheme() {
		MatchTheme matchTheme = new MatchTheme();
		matchTheme.themeID = 2;
		matchTheme.name = Shared.context.getString(R.string.match_themes_spectrograms_name);
		matchTheme.pairedImagesDiffer = false;
		matchTheme.backgroundImageUrl = URI_DRAWABLE + "back_spectrograms";
		matchTheme.cardObjs = new ArrayList<CardData>();		//ArrayList of CardData objects
        Collections.copy(Shared.cardDataList, matchTheme.cardObjs);
		return matchTheme;
	}
	
	public static Bitmap getBackgroundImage(MatchTheme matchTheme) {
		String drawableResourceName = matchTheme.backgroundImageUrl.substring(MatchThemes.URI_DRAWABLE.length());
		int drawableResourceId = Shared.context.getResources().getIdentifier(drawableResourceName, "drawable", Shared.context.getPackageName());
		Bitmap bitmap = Utils.scaleDown(drawableResourceId, Utils.screenWidth(), Utils.screenHeight());
		return bitmap;
	}
}