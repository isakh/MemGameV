package ws.isak.bridge.engine;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ws.isak.bridge.R;
import ws.isak.bridge.common.Shared;
import ws.isak.bridge.events.ui.MatchResetBackgroundEvent;
import ws.isak.bridge.fragments.MatchDifficultySelectFragment;
import ws.isak.bridge.fragments.MatchGameFragment;
import ws.isak.bridge.fragments.MatchMenuFragment;
import ws.isak.bridge.fragments.SwapMenuFragment;
import ws.isak.bridge.fragments.MatchThemeSelectFragment;
import ws.isak.bridge.fragments.UserSetupFragment;
import ws.isak.bridge.fragments.PreSurveyFragment;
import ws.isak.bridge.fragments.GameSelectFragment;
import ws.isak.bridge.fragments.PostSurveyFragment;
import ws.isak.bridge.fragments.FinishedFragment;

/*
 * Class ScreenController instantiates a list of currently openedScreens and a fragmentManager
 * ... //TODO
 *
 * @author isak
 */

public class ScreenController {

    public static final String TAG = "ScreenController";

	private static ScreenController mInstance = null;
	private static List<Screen> openedScreens = new ArrayList<Screen>();
	private FragmentManager mFragmentManager;

	private ScreenController() {
        //
        Log.d (TAG, "constructor does nothing");
	}

	public static ScreenController getInstance() {
        //Log.d (TAG, "method getInstance of ScreenController");
		if (mInstance == null) {
			mInstance = new ScreenController();
		}
		return mInstance;
	}

	public enum Screen {
        USER_SETUP,
        PRE_SURVEY,
        SELECT_GAME,            //choose between memory game and swap game
		MENU_MEM,               //menu allows choices of audio playback
        MENU_SWAP,              //TODO - audio playback is required?? so maybe some resolution parameters?THEME_SELECT_MEM,
        THEME_SELECT_MEM,
        DIFFICULTY_MEM,         //three levels of difficulty available
        DIFFICULTY_SWAP,        //TODO - start with two levels
        GAME_MEM,
        GAME_SWAP,              //TODO
        POST_SURVEY,             //FIXME should we have different ones for each game? and/or one for all?
        FINISHED
    }
	
	public static Screen getLastScreen() {
        Log.d (TAG, "method getLastScreen");
		return openedScreens.get(openedScreens.size() - 1);
	}

	public void openScreen(Screen screen) {
        Log.d (TAG, "Method openScreen: creating mFragmentManager");
		mFragmentManager = Shared.activity.getSupportFragmentManager();
		
		if (screen == Screen.GAME_MEM && openedScreens.get(openedScreens.size() - 1) == Screen.GAME_MEM) {
			openedScreens.remove(openedScreens.size() - 1);
		} else if (screen == Screen.DIFFICULTY_MEM && openedScreens.get(openedScreens.size() - 1) == Screen.GAME_MEM) {
			openedScreens.remove(openedScreens.size() - 1);
			openedScreens.remove(openedScreens.size() - 1);
		}
		Fragment fragment = getFragment(screen);
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.commit();
		openedScreens.add(screen);
	}

	public boolean onBack() {               //FIXME lots of new options for how far back to go with added screens
		if (openedScreens.size() > 0) {
			Screen screenToRemove = openedScreens.get(openedScreens.size() - 1);
			openedScreens.remove(openedScreens.size() - 1);
			if (openedScreens.size() == 0) {
				return true;
			}
			Screen screen = openedScreens.get(openedScreens.size() - 1);
			openedScreens.remove(openedScreens.size() - 1);
			openScreen(screen);
			//menu/theme select is where we go back to when at difficulty select/game for memory matching game
            if ((screen == Screen.THEME_SELECT_MEM || screen == Screen.MENU_MEM) && (screenToRemove == Screen.DIFFICULTY_MEM || screenToRemove == Screen.GAME_MEM)) {
				Shared.eventBus.notify(new MatchResetBackgroundEvent());
            }
            //back from SelectGame should be limited?
            if (screen == Screen.SELECT_GAME) {
                //allow user to change pre survey data
                openScreen(Screen.PRE_SURVEY);
            }
            if (screen == Screen.MENU_SWAP) {       //TODO || Screen.MENU_MEM?
                openScreen(Screen.SELECT_GAME);
            }
			return false;
		}
		return true;
	}

	private Fragment getFragment(Screen screen) {
		switch (screen) {
            case USER_SETUP:
                Log.d (TAG, "method getFragment: case USER_SETUP");
                return new UserSetupFragment();
            case PRE_SURVEY:
                Log.d (TAG, "method getFragment: case PRE_SURVEY");
                return new PreSurveyFragment();
            case SELECT_GAME:
                Log.d (TAG, "method getFragment: case SELECT_GAME");
                return new GameSelectFragment();
		    case MENU_MEM:
                Log.d (TAG, "method getFragment: case MENU_MEM");
                return new MatchMenuFragment();
            case MENU_SWAP:
                Log.d (TAG, "method getFragment: case MENU_SWAP");
                return new SwapMenuFragment();
            case THEME_SELECT_MEM:
                Log.d (TAG, "method getFragment: case THEME_SELECT_MEM");
                return new MatchThemeSelectFragment();
            case DIFFICULTY_MEM:
                Log.d (TAG, "method getFragment: case DIFFICULTY_MEM");
                return new MatchDifficultySelectFragment();
            //case DIFFICULTY_SWAP:
            //    return new SwapDifficultyFragment();
            case GAME_MEM:
	    		return new MatchGameFragment();
            //case GAME_SWAP:
            //    return new SwapGameFragment();
            case POST_SURVEY:
                return new PostSurveyFragment();
            case FINISHED:
                return new FinishedFragment();
		    default:
			    break;
		}
		return null;
	}
}
