package ws.isak.bridge.events;

import android.util.Log;

import ws.isak.bridge.events.engine.MatchFlipDownCardsEvent;
import ws.isak.bridge.events.engine.MatchGameWonEvent;
import ws.isak.bridge.events.engine.MatchHidePairCardsEvent;
import ws.isak.bridge.events.engine.PlayCardAudioEvent;

import ws.isak.bridge.events.ui.MatchBackGameEvent;
import ws.isak.bridge.events.ui.MatchFlipCardEvent;
import ws.isak.bridge.events.ui.MatchDifficultySelectedEvent;
import ws.isak.bridge.events.ui.MatchNextGameEvent;
import ws.isak.bridge.events.ui.MatchResetBackgroundEvent;
import ws.isak.bridge.events.ui.MatchThemeSelectedEvent;
import ws.isak.bridge.events.ui.MatchStartEvent;
import ws.isak.bridge.events.ui.SwapStartEvent;

/*
 * Class EventObserverAdapter defines the behavior of each eventObserver onEvent method
 * for all of the possible events in the case that the event triggers an
 * UnsupportedOperationExcepetion.
 *
 * @author isak
 */

public class EventObserverAdapter implements EventObserver {

    private static final String TAG = "EventObserverAdapter";

	public void onEvent(MatchFlipCardEvent event) {
        Log.d (TAG, "method onEvent: MatchFlipCardEvent");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchDifficultySelectedEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchHidePairCardsEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchFlipDownCardsEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchStartEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

    @Override
    public void onEvent(SwapStartEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
    }

	@Override
	public void onEvent(MatchThemeSelectedEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchGameWonEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchBackGameEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchNextGameEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(MatchResetBackgroundEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(PlayCardAudioEvent event) {
        //Log.d (TAG, "");
        throw new UnsupportedOperationException();
	}
}