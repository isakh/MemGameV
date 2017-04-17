package ws.isak.bridge.ui;

import java.util.Locale;
import android.util.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ws.isak.bridge.R;
import ws.isak.bridge.common.Shared;

/*
 * Class SwapDifficultyView creates the view for the swap difficultyLevel screen
 *
 * @author isak
 */

public class SwapDifficultyView extends LinearLayout {

    public static final String TAG = "DifficultyView";
    private ImageView mTitle;

    /*
     * Constructor DifficultyView sets context
     */
    public SwapDifficultyView(Context context) {
        this(context, null);
        Log.d (TAG, "constructor");
    }

    /*
     * Overloaded constructor sets context and attributes
     */
    public SwapDifficultyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d (TAG, "overloaded constructor, includes AttributeSet");
        LayoutInflater.from(context).inflate(R.layout.swap_difficulty_view, this, true);
        setOrientation(LinearLayout.VERTICAL);
        mTitle = (ImageView) findViewById(R.id.swap_difficulty_title);
    }

    /*
     * Method setMatchDifficulty
     */
    public void setSwapDifficulty(int difficulty, int stars) {
        Log.d (TAG, "method setMatchDifficulty");
        String titleResource = String.format(Locale.ENGLISH, "button_difficulty_%d_star_%d", difficulty, stars);
        int drawableResourceId = Shared.context.getResources().getIdentifier(titleResource, "drawable", Shared.context.getPackageName());
        mTitle.setImageResource(drawableResourceId);
    }

}
