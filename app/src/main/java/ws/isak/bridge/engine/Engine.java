package ws.isak.bridge.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import android.media.MediaPlayer;

import android.os.AsyncTask;
import android.os.Handler;

import android.util.Log;

import android.widget.Toast;
import android.widget.ImageView;

import ws.isak.bridge.R;

import ws.isak.bridge.common.Audio;
import ws.isak.bridge.common.MatchCardData;
import ws.isak.bridge.common.SwapCardData;
import ws.isak.bridge.common.Memory;
import ws.isak.bridge.common.Shared;

import ws.isak.bridge.engine.ScreenController.Screen;

import ws.isak.bridge.events.EventObserverAdapter;

import ws.isak.bridge.events.engine.MatchFlipDownCardsEvent;
import ws.isak.bridge.events.engine.MatchGameWonEvent;
import ws.isak.bridge.events.engine.PlayCardAudioEvent;
import ws.isak.bridge.events.engine.MatchHidePairCardsEvent;

import ws.isak.bridge.events.engine.SwapGameWonEvent;
import ws.isak.bridge.events.engine.SwapPauseRowAudioEvent;
import ws.isak.bridge.events.engine.SwapPlayRowAudioEvent;
import ws.isak.bridge.events.engine.SwapSelectedCardsEvent;
import ws.isak.bridge.events.ui.MatchStartEvent;
import ws.isak.bridge.events.ui.SwapStartEvent;

import ws.isak.bridge.events.ui.MatchDifficultySelectedEvent;
import ws.isak.bridge.events.ui.SwapDifficultySelectedEvent;

import ws.isak.bridge.events.ui.MatchNextGameEvent;
import ws.isak.bridge.events.ui.MatchResetBackgroundEvent;
import ws.isak.bridge.events.ui.MatchThemeSelectedEvent;
import ws.isak.bridge.events.ui.MatchBackGameEvent;
import ws.isak.bridge.events.ui.MatchFlipCardEvent;

import ws.isak.bridge.themes.MatchTheme;
import ws.isak.bridge.themes.MatchThemes;
import ws.isak.bridge.ui.PopupManager;

import ws.isak.bridge.model.GameState;
import ws.isak.bridge.model.MatchGameData;
import ws.isak.bridge.model.SwapGameData;
import ws.isak.bridge.model.MatchBoardConfiguration;
import ws.isak.bridge.model.SwapBoardConfiguration;
import ws.isak.bridge.model.MatchGame;
import ws.isak.bridge.model.SwapGame;
import ws.isak.bridge.model.MatchBoardArrangement;
import ws.isak.bridge.model.SwapBoardArrangement;

import ws.isak.bridge.utils.Clock;
import ws.isak.bridge.utils.ImageScaling;
import ws.isak.bridge.utils.SwapTileCoordinates;

/*
 * Class Engine contains the core behavior of the app.
 *
 * @author isak
 */

public class Engine extends EventObserverAdapter {

	private static final String TAG = "Engine";
	private static Engine mInstance = null;			//instance of Engine for current use of app
	private MatchGame mPlayingMatchGame = null;		//instance of MatchGame for current game being played
    private SwapGame mPlayingSwapGame = null;
    private MatchGameData currentMatchGameData;
	private SwapGameData currentSwapGameData;
    private int mFlippedId = -1;					//id of the tile (? or event?) with the card being flipped
	private int mToFlip = -1;

    private int mSelectedID = -1;

	private ScreenController mScreenController;
	private MatchTheme mSelectedMatchTheme;
	private ImageView mBackgroundImage;
	private Handler mHandler;
    private Random randomIndex = new Random();

	private Engine() {
        Log.d (TAG, "***** Constructor *****");
        Log.d (TAG, "calling ScreenController.getInstance");
		mScreenController = ScreenController.getInstance();
		mHandler = new Handler();
	}

	public static Engine getInstance() {
        Log.d (TAG, "method getInstance called for Engine");
		if (mInstance == null) {
			mInstance = new Engine();
		}
		return mInstance;
	}

	public void start() {
        Log.d (TAG, " *******: method start: Shared.eventBus @: " + Shared.eventBus);
        //start event listeners
        Shared.eventBus.listen(MatchStartEvent.TYPE, this);
        Shared.eventBus.listen(SwapStartEvent.TYPE, this);
        //difficultyLevel select event listeners
		Shared.eventBus.listen(MatchDifficultySelectedEvent.TYPE, this);
		Shared.eventBus.listen(SwapDifficultySelectedEvent.TYPE, this);

        Shared.eventBus.listen(MatchFlipCardEvent.TYPE, this);
		Shared.eventBus.listen(MatchThemeSelectedEvent.TYPE, this);
		Shared.eventBus.listen(MatchBackGameEvent.TYPE, this);
		Shared.eventBus.listen(MatchNextGameEvent.TYPE, this);
		Shared.eventBus.listen(MatchResetBackgroundEvent.TYPE, this);
		Shared.eventBus.listen(PlayCardAudioEvent.TYPE, this);
	}

	public void stop() {
		mPlayingMatchGame = null;
		mBackgroundImage.setImageDrawable(null);
		mBackgroundImage = null;
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;

        //start event unlisten
        Shared.eventBus.unlisten(MatchStartEvent.TYPE, this);
        Shared.eventBus.unlisten(SwapStartEvent.TYPE, this);
        //difficultyLevel select event unlisten
        Shared.eventBus.unlisten(MatchDifficultySelectedEvent.TYPE, this);
        Shared.eventBus.unlisten(SwapDifficultySelectedEvent.TYPE, this);

		Shared.eventBus.unlisten(MatchFlipCardEvent.TYPE, this);
		Shared.eventBus.unlisten(MatchThemeSelectedEvent.TYPE, this);
		Shared.eventBus.unlisten(MatchBackGameEvent.TYPE, this);
		Shared.eventBus.unlisten(MatchNextGameEvent.TYPE, this);
		Shared.eventBus.unlisten(MatchResetBackgroundEvent.TYPE, this);
		Shared.eventBus.unlisten(PlayCardAudioEvent.TYPE, this);

		mInstance = null;
	}

	@Override
	public void onEvent(MatchResetBackgroundEvent event) {
		Drawable drawable = mBackgroundImage.getDrawable();
		if (drawable != null) {
			((TransitionDrawable) drawable).reverseTransition(2000);
		} else {
			new AsyncTask<Void, Void, Bitmap>() {

				@Override
				protected Bitmap doInBackground(Void... params) {
					Bitmap bitmap = ImageScaling.scaleDown(R.drawable.background, ImageScaling.screenWidth(), ImageScaling.screenHeight());
					return bitmap;
				}
				protected void onPostExecute(Bitmap bitmap) {
                    //
					mBackgroundImage.setImageBitmap(bitmap);
				}
			}.execute();
		}
	}

	@Override
	public void onEvent(MatchStartEvent event) {
        Log.d (TAG, "override onEvent for MatchStartEvent: calling screen controller to open THEME_SELECT_MATCH screen");
        PopupManager.closePopup();
		mScreenController.openScreen(Screen.THEME_SELECT_MATCH);
	}

	@Override
    public void onEvent (SwapStartEvent event) {
        Log.d (TAG, "override onEvent for SwapStarEvent: Calling screen controller to open DIFFICULTY_SWAP");
        PopupManager.closePopup();
        mScreenController.openScreen(Screen.DIFFICULTY_SWAP);
    }

	@Override
	public void onEvent(MatchThemeSelectedEvent event) {
		mSelectedMatchTheme = event.matchTheme;
		mScreenController.openScreen(Screen.DIFFICULTY_MATCH);
		AsyncTask<Void, Void, TransitionDrawable> task = new AsyncTask<Void, Void, TransitionDrawable>() {

			@Override
			protected TransitionDrawable doInBackground(Void... params) {
				Bitmap bitmap = ImageScaling.scaleDown(R.drawable.background, ImageScaling.screenWidth(), ImageScaling.screenHeight());
				Bitmap backgroundImage = MatchThemes.getBackgroundImage(mSelectedMatchTheme);
				backgroundImage = ImageScaling.crop(backgroundImage, ImageScaling.screenHeight(), ImageScaling.screenWidth());
				Drawable backgrounds[] = new Drawable[2];
				backgrounds[0] = new BitmapDrawable(Shared.context.getResources(), bitmap);
				backgrounds[1] = new BitmapDrawable(Shared.context.getResources(), backgroundImage);
				TransitionDrawable imageCrossFader = new TransitionDrawable(backgrounds);
				return imageCrossFader;
			}

			@Override
			protected void onPostExecute(TransitionDrawable result) {
				super.onPostExecute(result);
				mBackgroundImage.setImageDrawable(result);
				result.startTransition(2000);
			}
		};
		task.execute();
	}

	@Override
	public void onEvent(MatchDifficultySelectedEvent event) {
		mFlippedId = -1;
		mPlayingMatchGame = new MatchGame();
        mPlayingMatchGame.matchTheme = mSelectedMatchTheme;
		mPlayingMatchGame.matchBoardConfiguration = new MatchBoardConfiguration(event.difficulty, mSelectedMatchTheme);
		mToFlip = mPlayingMatchGame.matchBoardConfiguration.numTiles;
        Shared.currentMatchGame = mPlayingMatchGame;

		// arrange board
		arrangeMatchBoard();

        //instantiating the currentMatchGameData object - some fields default to 0 || null
        currentMatchGameData = new MatchGameData();
        currentMatchGameData.setThemeID(Shared.currentMatchGame.matchTheme.themeID);
        currentMatchGameData.setGameDifficulty(Shared.currentMatchGame.matchBoardConfiguration.difficulty);
        currentMatchGameData.setGameDurationAllocated(Shared.currentMatchGame.matchBoardConfiguration.time);
        currentMatchGameData.setMixerState(Audio.MIX);
        //check setup of matchGameData - these should return current states
        Log.d (TAG, "******* New MatchGameData Instantiated *******");
        Log.d (TAG, "event MatchDifficultySelectedEvent: create currentMatchGameData: currentMatchGameData.getUserPlayingName : " + currentMatchGameData.getUserPlayingName());
        Log.d (TAG, "                                                     : currentMatchGameData.getThemeID : " + currentMatchGameData.getThemeID());
        Log.d (TAG, "                                                     : currentMatchGameData.getGameDifficulty: " + currentMatchGameData.getGameDifficulty());
        Log.d (TAG, "                                                     : currentMatchGameData.getGameDurationAllocated: " + currentMatchGameData.getGameDurationAllocated());
        Log.d (TAG, "                                                     : currentMatchGameData.getMixerState : " + currentMatchGameData.getMixerState());
        //these should reflect that the game is not yet started (don't check start timeStamp as it hasn't been used?)
        Log.d (TAG, "                             			              : currentMatchGameData.isGameStarted: " + currentMatchGameData.isGameStarted());
        Log.d (TAG, "                                                     : currentMatchGameData.getGameStartTimestamp: " + currentMatchGameData.getGameStartTimestamp());
        Log.d (TAG, "                                                     : currentMatchGameData.numPlayDurationsRecorded: " + currentMatchGameData.sizeOfPlayDurationsArray());
        Log.d (TAG, "                                                     : currentMatchGameData.numTurnDurationsRecorded: " + currentMatchGameData.sizeOfTurnDurationsArray());
        Log.d (TAG, "                                                     : currentMatchGameData.numCardSelectionsRecorded: " + currentMatchGameData.sizeOfCardSelectionArray());
        Log.d (TAG, "                             			              : currentMatchGameData.getNumTurnsTaken: " + currentMatchGameData.getNumTurnsTaken());

        //debug Shared.userData
        Log.d (TAG, " ******* : Shared.userData @ : " + Shared.userData);
        Log.d (TAG, " ******* : userData.getCurMatchGame @ : " + Shared.userData.getCurMatchGame());

        Shared.userData.setCurMatchGame(currentMatchGameData);
        // start the screen - This call to screen controller causes the screen controller to select
        // a new MatchGameFragment from the screen controller.  Opening the new MatchGameFragment leads to a
        // call to buildBoard() a private method in the MatchGame Fragment. buildBoard calls setBoard in
        // the BoardView ui class. setBoard in BoardView propagates through a local buildBoard method
        // and eventually calls addTile for each of the tiles on the board to be built.   This leads
        // to a thread for each tile which calls getMatchTileBitmap.
		mScreenController.openScreen(Screen.GAME_MATCH);
    }

	private void arrangeMatchBoard() {
		MatchBoardConfiguration matchBoardConfiguration = mPlayingMatchGame.matchBoardConfiguration;
		MatchBoardArrangement matchBoardArrangement = new MatchBoardArrangement();

		// list all n tiles  {0,1,2,...n-1} /
		List<Integer> tileIDs = new ArrayList<Integer>();
		for (int i = 0; i < matchBoardConfiguration.numTiles; i++) {
			tileIDs.add(i);
		}
		// shuffle
		// result {4,10,2,39,...}
		Collections.shuffle(tileIDs);

		// map the paired tiles to each other as well as the card for each pair of tiles
		matchBoardArrangement.pairs = new HashMap<Integer, Integer>();
		matchBoardArrangement.cardObjs = new HashMap<Integer, MatchCardData>();
		int j = 0;
		for (int i = 0; i < tileIDs.size(); i++) {		//Iterate over all of the tiles
			if (i + 1 < tileIDs.size()) {				//check that we haven't filled all tile pairs
				// take pairs of tile IDs in order from the shuffled list and insert into pairs HashMap
				matchBoardArrangement.pairs.put(tileIDs.get(i), tileIDs.get(i + 1));
				// and ensure that the mapping is bi-directional
				matchBoardArrangement.pairs.put(tileIDs.get(i + 1), tileIDs.get(i));
				// map each of the paired tile IDs to the same card object ID
				matchBoardArrangement.cardObjs.put(tileIDs.get(i), Shared.matchCardDataList.get(j));
				matchBoardArrangement.cardObjs.put(tileIDs.get(i + 1), Shared.matchCardDataList.get(j));
				//debug report: state of tile id's paired on board, and card id for the tile pair
				Log.d (TAG, "method arrangeBoard: Map Tile Pairs: Tile id1: " + tileIDs.get(i) + " |  Tile id2: " + tileIDs.get(i + 1) + " | Mapped Card id: " + Shared.matchCardDataList.get(j).getCardID());
				//Log.d (TAG, "method arrangeBoard: Mapping cardObjs to IDs: ID is: " + tileIDs.get(i) + " | Card Object ID is: " + mSelectedMatchTheme.cardObjs.get(j).getCardID());
				//Log.d (TAG, "method arrangeBoard: 		Card Object Image URI 1 is : " + mSelectedMatchTheme.cardObjs.get(j).getImageURI1());
				//Log.d (TAG, "method arrangeBoard: 		Card Object Image URI 2 is : " + mSelectedMatchTheme.cardObjs.get(j).getImageURI2());
				//Log.d (TAG, "method arrangeBoard: 		Card Object Audio URI is : " + mSelectedMatchTheme.cardObjs.get(j).getAudioURI());
				i++;
				j++;
			}
		}
		mPlayingMatchGame.matchBoardArrangement = matchBoardArrangement;
	}

    @Override
    public void onEvent(SwapDifficultySelectedEvent event) {
        mSelectedID = -1;
        mPlayingSwapGame = new SwapGame();
        mPlayingSwapGame.swapBoardConfiguration = new SwapBoardConfiguration(event.difficulty);

        Shared.currentSwapGame = mPlayingSwapGame;

        // arrange board
        arrangeSwapBoard();

        //instantiating the currentSwapGameData object - some fields default to 0 || null
        currentSwapGameData = new SwapGameData();
        currentSwapGameData.setGameDifficulty(Shared.currentSwapGame.swapBoardConfiguration.getSwapDifficulty());
        currentSwapGameData.setGameDurationAllocated(Shared.currentSwapGame.swapBoardConfiguration.time);
        Shared.userData.setCurSwapGame(currentSwapGameData);
        mScreenController.openScreen(Screen.FINISHED); //FIXME to POST_SURVEY? or PopupWon when ready
    }

    private void arrangeSwapBoard() {
        SwapBoardConfiguration swapBoardConfiguration = mPlayingSwapGame.swapBoardConfiguration;
        SwapBoardArrangement swapBoardArrangement = new SwapBoardArrangement();

        // select Shared.currentSwapGame.difficultyLevel number of species without duplicates
        List <Integer> targetSpeciesIDs = new ArrayList<Integer>(swapBoardConfiguration.getSwapDifficulty());
        for (int i = 0; i < swapBoardConfiguration.getNumSpecies(); i++) {
            Log.d (TAG, "method arrangeSwapBoard: i: " + i + " | difficultyLevel = numSpecies: " + swapBoardConfiguration.getSwapDifficulty());
            int  tempSpeciesIndex = randomIndex.nextInt(10);       //10 is currently the max number of species - TODO set in xml
            Log.d (TAG, "method arrangeSwapBoard:" + " tempSpeciesIndex: " + tempSpeciesIndex);
            for (int j = 0; j < i; j++) {
                if (targetSpeciesIDs.get(j) == tempSpeciesIndex) {
                    Log.d (TAG, "... targetSpeciesID.get(j): " + targetSpeciesIDs.get(j) +
                            " == tempSpeciesIndex: " + tempSpeciesIndex +
                            " | NOT ADDING DUPLICATE TARGET SPECIES");
                    i--;
                    break;
                }
            }
            targetSpeciesIDs.add(tempSpeciesIndex);
            Log.d (TAG, "method arrangeSwapBoard: @ position i: " + i + " targetSpeciesID: " + targetSpeciesIDs.get(i));
        }
        Log.d (TAG, "method arrangeSwapBoard: Target Species Selected: ");
        for (int k = 0; k < targetSpeciesIDs.size(); k++) {
            Log.d (TAG, "                   : k: " + k + " | targetSpeciesID(k): " + targetSpeciesIDs.get(k));
        }
        //create a list for the active cards (all cards for each target species)
        List <SwapCardData> activeCardList = new ArrayList<SwapCardData>(swapBoardConfiguration.difficultyLevel * 4);
        //iterate over Shared.swapCardDataList and
        for (int l = 0; l < Shared.swapCardDataList.size(); l++) {
            // iterate over targetSpecies ID list
            for (int m = 0; m < targetSpeciesIDs.size(); m++) {
                //if the current card in the data list has the same ID as we are looking for in the species list
                if (Shared.swapCardDataList.get(l).getCardID().getSwapCardSpeciesID() == targetSpeciesIDs.get(m)) {
                    //append that card to the active list
                    Log.d (TAG, "method arrangeSwapBoard: adding card to active list: l: " + l + " | m: " + m +
                            " | Shared.swapCardDataList.get(i).getCardID(): < " +
                            Shared.swapCardDataList.get(l).getCardID().getSwapCardSpeciesID() + " , " +
                            Shared.swapCardDataList.get(l).getCardID().getSwapCardSegmentID() +
                            " > | targetSpeciesID.get(m): " + targetSpeciesIDs.get(m) + "| Adding card to active list...");
                    activeCardList.add(Shared.swapCardDataList.get(l));
                }
            }
        }
        Log.d (TAG, " method arrangeSwapBoard: activeCardList prior to shuffling...");
        for (int n = 0; n < activeCardList.size(); n++) {
            Log.d (TAG, "                    : n: " + n + " | activeCardList(n) cardID: < " +
                    activeCardList.get(n).getCardID().getSwapCardSpeciesID() + " , " +
                    activeCardList.get(n).getCardID().getSwapCardSegmentID() + " >");
        }
        //shuffle all the active cards
        Collections.shuffle (activeCardList);
        Log.d (TAG, " method arrangeSwapBoard: activeCardList after shuffling...");
        for (int p = 0; p < activeCardList.size(); p++) {
            Log.d (TAG, "                    : p: " + p + " | activeCardList(p) cardID: < " +
                    activeCardList.get(p).getCardID().getSwapCardSpeciesID() + " , " +
                    activeCardList.get(p).getCardID().getSwapCardSegmentID() + " >");
        }
        //iterate over numTiles
        for (int q = 0; q < swapBoardConfiguration.numTiles; q++) {
            Log.d (TAG, "method arrangeSwapBoard: iterating to place cards: i: " + q + " | numTiles: " +
                    swapBoardConfiguration.numTiles + " | difficultyLevel: " + swapBoardConfiguration.difficultyLevel);
            //a SwapTileCoordinates object
            SwapTileCoordinates tileCoords = new SwapTileCoordinates(-1 ,-1); //FIXME make less of a kludge - these coords are off the board
            tileCoords.setSwapCoordRow ((int) Math.floor(q / swapBoardConfiguration.swapNumTilesInRow));
            tileCoords.setSwapCoordCol (q % swapBoardConfiguration.swapNumTilesInRow);
            //having set the Row and Column coordinates print them out
            Log.d (TAG, "method arrangeSwapBoard: inserted tileCoords: i: " + q + " row: " +
                    tileCoords.getSwapCoordRow() + " col: " + tileCoords.getSwapCoordCol());
            //create the Mapping between tileCoords and the SwapCard object in the activeCardList.
            Log.d (TAG, "method arrangeSwapBoard: tileCoords: < " + tileCoords.getSwapCoordRow() + " , " +
                    tileCoords.getSwapCoordCol() + " > | activeCardList.get(i): " + activeCardList.get(q) +
                    " | cardID <species, segment>: < " + activeCardList.get(q).getCardID().getSwapCardSpeciesID() +
                    " , " + activeCardList.get(q).getCardID().getSwapCardSegmentID() + " >");
            swapBoardArrangement.setCardOnBoard (tileCoords, activeCardList.get(q));
        }
        mPlayingSwapGame.swapBoardArrangement = swapBoardArrangement;

    }

	// Override method onEvent when the event being passed is a MatchFlipCardEvent.
	@Override
	public void onEvent(MatchFlipCardEvent event) {

		Log.d (TAG, "onEvent MatchFlipCardEvent: event.id is: " + event.id + " *** AT START OF method ***");
		int id = event.id;
        Log.d (TAG, "                     : Card ID is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(id).getCardID());
        Log.d (TAG, "                     : species is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(id).getSpeciesName());

		if (mFlippedId == -1) {		//This is -1 when no cards are flipped up
			mFlippedId = id;		//set id of flipped up card to id of tile from event
			Log.i(TAG, "onEvent MatchFlipCardEvent: mFlippedId == -1: now set mFlippedId to: " + id);
			Log.d(TAG, "			waiting for second tile to be selected...");
		}
		else {		//this code covers behaviour when the second tile of a pair is clicked
			//check if mFlippedId is set from first card and compare to id of this tile flipped at event.
			if (mPlayingMatchGame.matchBoardArrangement.isPair(mFlippedId, id)) {
				 Log.i(TAG, "onEvent MatchFlipCardEvent: mFlippedId != -1 (one card is already flipped): and isPair: mFlippedID is: " + mFlippedId + ", " + id + " returns true");
				// send event - hide id1, id2
				Shared.eventBus.notify(new MatchHidePairCardsEvent(mFlippedId, id), 1000);//TODO make delay variable and incorporate into total game time

                //display the matched species name in a toast to the user
                Toast.makeText(Shared.context, mPlayingMatchGame.matchBoardArrangement.cardObjs.get(id).getSpeciesName(), Toast.LENGTH_LONG).show();

                Log.d (TAG, "onEvent MatchFlipCardEvent: isPair returned TRUE: calling MatchHidePairCardsEvent");
                // play music
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Log.d (TAG, "onEvent MatchFlipCardEvent: isPair returns TRUE: calling Audio.playCorrect()");
						//play the correct match audio
                        Audio.playCorrect();
					}
				}, 1000);		//TODO instead of fixed delay 1000ms use duration of sample
				mToFlip -= 2;			//remaining number of tiles to flip..
				if (mToFlip == 0) {		//when this gets to 0, we have flipped all pairs and can compute the score
					int passedSeconds = (int) (Clock.getInstance().getPassedTime() / 1000);
					Clock.getInstance().pause();
					long totalTimeInMillis = mPlayingMatchGame.matchBoardConfiguration.time;
                    int totalTime = (int) Math.ceil((double) totalTimeInMillis / 1000); //TODO is this enough or should we convert all to long ms
					GameState gameState = new GameState();
					mPlayingMatchGame.gameState = gameState;
					// remained seconds
					gameState.remainingTimeInSeconds = totalTime - passedSeconds;

					// calculate stars and score from the amount of time that has elapsed as a ratio
					// of total time allotted for the game.  When calculating this we still have incorporated
                    // the time based on the difficultyLevel as well as the time to play back the samples
					if (passedSeconds <= totalTime / 2) {gameState.achievedStars = 3; }
					else if (passedSeconds <= totalTime - totalTime / 5) {gameState.achievedStars = 2; }
					else if (passedSeconds < totalTime) {gameState.achievedStars = 1; }
					else {gameState.achievedStars = 0;}
					// calculate the score
					gameState.achievedScore = mPlayingMatchGame.matchBoardConfiguration.difficulty * gameState.remainingTimeInSeconds * mPlayingMatchGame.matchTheme.themeID; //FIXME - what is themeID doing here??
					// save to memory
					Memory.saveMatch(mPlayingMatchGame.matchTheme.themeID, mPlayingMatchGame.matchBoardConfiguration.difficulty, gameState.achievedStars);
					//trigger the MatchGameWonEvent
					Shared.eventBus.notify(new MatchGameWonEvent(gameState), 1200);     //TODO what is 1200 doing here? convert to xml
				}
			} else {
				Log.d(TAG, "onEvent MatchFlipCardEvent: mFlippedID != -1: and !isPair: mFlippedID is:  " + mFlippedId);
				Log.d(TAG, "onEvent: MatchFlipCardEvent: Flip: all down");
				// send event - flip all down
				Shared.eventBus.notify(new MatchFlipDownCardsEvent(), 1000);        //TODO ∆1000 to xml - what does this do?
			}
			mFlippedId = -1;
			Log.d(TAG, "onEvent MatchFlipCardEvent: reset mFlippedId to -1 check: " + mFlippedId);
		}
	}

	public void onEvent (SwapSelectedCardsEvent event) {

        // start of SwapSelectedCardsEvent
        Log.d (TAG, "onEvent SwapSelectedCardsEvent: event.id1: " + event.id1 + " event.id2: " + event.id2 + " *** AT START OF method ***");
        SwapTileCoordinates card1Coords = event.id1;
        SwapTileCoordinates card2Coords = event.id2;
        SwapCardData card1Data = mPlayingSwapGame.swapBoardArrangement.getSwapCardDataFromCoords(card1Coords);
        SwapCardData card2Data = mPlayingSwapGame.swapBoardArrangement.getSwapCardDataFromCoords(card2Coords);
        Log.d (TAG, "onEventSwapSelectedCardsEvent: card1Coords: " + card1Coords + " | card2Coords: " + card2Coords + " | card1Data ID: " + card1Data.getCardID() + " | card2Data ID: " + card2Data.getCardID());

        //Swap the coordinates associated with the two SwapCardData objects
        switchTileCoordinates(card1Coords, card2Coords);

        //push the new cards back into the Map
        mPlayingSwapGame.swapBoardArrangement.setCardOnBoard(card1Coords, card1Data);
        mPlayingSwapGame.swapBoardArrangement.setCardOnBoard(card2Coords, card2Data);

        //TODO what do I need to do to either animate their swapping or at least redraw all the cards on the board?

        //Check if game is won
        boolean winning = true;     //TODO is this safe to default to true?
        for (int i = 0; i < mPlayingSwapGame.swapBoardConfiguration.getSwapDifficulty(); i++ ) {        //for each row on the board
            for (int j = 0; j < 4; j++) {       //for each tile in row
                SwapTileCoordinates targetCoords = new SwapTileCoordinates(i, j);
                SwapCardData cardOnTile = mPlayingSwapGame.swapBoardArrangement.getSwapCardDataFromCoords(targetCoords);
                if (cardOnTile.getCardID().getSwapCardSpeciesID() != i || cardOnTile.getCardID().getSwapCardSegmentID() != j) {
                    winning = false;
                }
            }
        }
        if (winning) {
            int passedSeconds = (int) (Clock.getInstance().getPassedTime() / 1000);
            Clock.getInstance().pause();
            long totalTimeInMillis = mPlayingSwapGame.swapBoardConfiguration.time;
            int totalTime = (int) Math.ceil((double) totalTimeInMillis / 1000); //TODO is this enough or should we convert all to long ms
            GameState gameState = new GameState();
            mPlayingSwapGame.gameState = gameState;
            // remained seconds
            gameState.remainingTimeInSeconds = totalTime - passedSeconds;

            // calculate stars and score from the amount of time that has elapsed as a ratio
            // of total time allotted for the game.  When calculating this we still have incorporated
            // the time based on the difficultyLevel as well as the time to play back the samples
            if (passedSeconds <= totalTime / 2) {gameState.achievedStars = 3; }
            else if (passedSeconds <= totalTime - totalTime / 5) {gameState.achievedStars = 2; }
            else if (passedSeconds < totalTime) {gameState.achievedStars = 1; }
            else {gameState.achievedStars = 0;}
            // calculate the score
            gameState.achievedScore = mPlayingSwapGame.swapBoardConfiguration.difficultyLevel * gameState.remainingTimeInSeconds;
            // save to memory
            Memory.saveSwap(mPlayingSwapGame.swapBoardConfiguration.difficultyLevel, gameState.achievedStars);
            //trigger the MatchGameWonEvent
            Shared.eventBus.notify(new SwapGameWonEvent(gameState), 1200);      //TODO what is 1200 doing here? convert to xml
        }
	}

    private void switchTileCoordinates (SwapTileCoordinates tile1, SwapTileCoordinates tile2) {
        //create temp coordinates, initialized to off the board
        SwapTileCoordinates temp = new SwapTileCoordinates(-1, -1);
        //copy tile 2 to temp
        temp.setSwapCoordRow (tile2.getSwapCoordRow());
        temp.setSwapCoordCol (tile2.getSwapCoordCol());
        //copy tile 1 to tile 2
        tile2.setSwapCoordRow (tile1.getSwapCoordRow());
        tile2.setSwapCoordCol (tile1.getSwapCoordCol());
        //copy temp to tile 1
        tile1.setSwapCoordRow (temp.getSwapCoordRow());
        tile1.setSwapCoordCol (temp.getSwapCoordCol());
    }

	public void onEvent (PlayCardAudioEvent event) {
		int id = event.id;
		Log.i (TAG, "onEvent PlayCardAudioEvent: event.id: " + id);
		playTileAudio(id);
	}

	/*
	 * Method playTileAudio is called when a tile is turned over and the game mode involves hearing
	 * the audio of the bird / spectrogram in question.  In order to play the correct audio, the
	 * method takes input of:
	 * 	@param tileID
	 * which is the the integer tile ID  associated with the given tile.  If the audio
	 * is turned off and the mode expects playback, a toast is sent to the user.
	 */

	private void playTileAudio(int tileID) {
		/*
        Log.d (TAG, "method playTileAudio: tileID: " + tileID);
		Log.d (TAG, "					 : curCardOnTile is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getCardID());
		Log.d (TAG, " 					 : curCardOnTile.getAudioURI is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getAudioURI());
		Log.d (TAG, " 					 : curCardOnTile.getImageURI1 is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getImageURI1());
		Log.d (TAG, " 					 : curCardOnTile.getPairedImageDiffer is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getPairedImageDiffer());
		Log.d (TAG, " 					 : curCardOnTile.getFirstImageUsed is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getFirstImageUsed());
		Log.d (TAG, " 					 : curCardOnTile.getImageURI2 is: " + mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getImageURI2());
        */
        if (!Audio.OFF) {
            String audioResourceName = mPlayingMatchGame.matchBoardArrangement.cardObjs.get(tileID).getAudioURI().substring(MatchThemes.URI_AUDIO.length());
            Log.d (TAG, "                    : audioResourceName: " + audioResourceName);
            int audioResourceId = Shared.context.getResources().getIdentifier(audioResourceName, "raw", Shared.context.getPackageName());
            Log.d (TAG, "                    : audioResourceId: " + audioResourceId);
            MediaPlayer curTileAudio = MediaPlayer.create (Shared.context, audioResourceId);
            curTileAudio.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer curTileAudio) {
						Log.d (TAG, "method playTileAudio: overriding onCompletion");
						Audio.setIsAudioPlaying(false);
                        Log.d (TAG, "method playTileAudio: Audio.setIsAudioPlaying(false) called. Audio.getIsAudioPlaying(): " + Audio.getIsAudioPlaying());
						curTileAudio.reset();
						curTileAudio.release();
						curTileAudio = null;
					}
            });
            curTileAudio.start();
			Audio.setIsAudioPlaying (true);
            Log.d (TAG, "method playTileAudio: curTileAudio.start() called, Audio.getIsAudioPlaying(): " + Audio.getIsAudioPlaying());
            long sampleDuration = Audio.getAudioDuration (audioResourceId);
            Log.d (TAG, "                    : sampleDuration: " + sampleDuration);

		}
		else {
			Toast.makeText(Shared.context, "Please turn on game audio to play in this mode, you can do this under settings", Toast.LENGTH_SHORT).show();
            mScreenController.openScreen(Screen.MENU_MATCH);
		}
	}

	@Override
    public void onEvent(SwapPauseRowAudioEvent event) {
        //FIXME - all the work

    }

    @Override
    public void onEvent(SwapPlayRowAudioEvent event) {
        //FIXME - all the work

    }

    @Override
    public void onEvent(MatchNextGameEvent event) {
        PopupManager.closePopup();
        int difficulty = mPlayingMatchGame.matchBoardConfiguration.difficulty;
        if (mPlayingMatchGame.gameState.achievedStars == 3 && difficulty < 3) {  //TODO set these numbers in values.xml?
            difficulty++;
        }
        Shared.eventBus.notify(new MatchDifficultySelectedEvent(difficulty));
    }

    @Override
    public void onEvent(MatchBackGameEvent event) {
        PopupManager.closePopup();
        mScreenController.openScreen(Screen.DIFFICULTY_MATCH);
        //TODO verify that adding the following lines to reset the difficultyLevel on backGameEvent worked [initially yes]
        int difficulty = mPlayingMatchGame.matchBoardConfiguration.difficulty;
        Shared.eventBus.notify (new MatchDifficultySelectedEvent(difficulty));
    }

	public MatchGame getActiveMatchGame() {
		//Log.d (TAG, "method getActiveGame");
		return mPlayingMatchGame;
	}

	public SwapGame getActiveSwapGame() {
        Log.d (TAG, "method getActiveSwapGame");
        return mPlayingSwapGame;
    }

	public MatchTheme getSelectedTheme() {
		//Log.d (TAG, "method getSelectedTheme);
		return mSelectedMatchTheme;
	}

	public void setBackgroundImageView(ImageView backgroundImage) {
		//Log.d (TAG, "method setBackgroundImageView");
		mBackgroundImage = backgroundImage;
	}
}