package ws.isak.memgamev.events.engine;

import android.util.Log;

import ws.isak.memgamev.events.AbstractEvent;
import ws.isak.memgamev.events.EventObserver;

/**
 * When a matched pair of cards is selected, hide them from the screen.
 *
 * @author  isak
 */
public class HidePairCardsEvent extends AbstractEvent {

	public final String TAG = "HidePairCardsEvent";

	public static final String TYPE = HidePairCardsEvent.class.getName();

	public int id1;
	public int id2;

	public HidePairCardsEvent(int id1, int id2) {
		Log.d (TAG, "constructor method HidePairCardsEvent: MATCH!!!: param id1 is: " + id1 + " param id2 is: " + id2);
		this.id1 = id1;
		this.id2 = id2;
	}

	@Override
	protected void fire(EventObserver eventObserver) {
		eventObserver.onEvent(this);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
