package ws.isak.bridge.common;

import java.util.ArrayList;

import android.util.Log;

import ws.isak.bridge.model.MemGameData;

/*
 * The UserData class contains creation and accessor methods for the data collected about a particular
 * user of the game including their name (checked against a database(?) of names to avoid collisions
 * as well as a list of the GameData objects containing timing and move selection information about
 * each game that they have played.
 *
 * @author isak
 */

public class UserData {

    private static final String TAG = "UserData";

    private static UserData mInstance = null;

    private String userName;               //FIXME for now userName is TEXT_PRIMARY_KEY

    //TODO private String passWord;             //userName + passWord is used as TEXT_PRIMARY_KEY in UserDataORM?
    //TODO private long userCreateTimeStamp;

    //data from pre game survey
    private String ageRange;
    private String yearsTwitchingRange;
    private String speciesKnownRange;
    private String audibleRecognizedRange;
    private String interfaceExperienceRange;
    private boolean hearingEqualsSeeing;
    private boolean hasUsedSmartPhone;

    //data from post game survey
    private static boolean spectrogramFamiliar;
    private static int hearIsSeeLikert;
    private static int hearIsPredictLikert;

    //data from memory games played
    private MemGameData curMemGame;
    private ArrayList<MemGameData> memGameDataList;

    //data from swap games played
    //TODO private List swapGameDataList<SwapGameData>;   this will cover when the user plays the tile swapping game


    // Constructor - this should be called once when a userData object instance needs to be instantiated
    // the instantiator will pass all relevant nulled values
    public UserData() {
        //Log.d(TAG, "***** CONSTRUCTOR *****");
        //set strings to null
            //pre
        setUserName(null);
        setAgeRange(null);
        setYearsTwitchingRange(null);
        setSpeciesKnownRange(null);
        setAudibleRecognizedRange(null);
        setInterfaceExperienceRange(null);
        //set ints to 0 (Likert null)
            //post
        setHearIsSeeLikert(0);
        setHearIsPredictLikert(0);
        //set booleans to false
            //pre
        setHearingEqualsSeeing(false);
        setHasUsedSmartPhone(false);
            //post
        setSpectrogramFamiliar(false);
        //set game data lists
        initMemGameDataList();
    }

    /*
     * Method getInstance returns an instance of an empty  UserData object - this is called only
     * when a new userData object needs to be created.
     */
    public static UserData getInstance() {
        Log.d(TAG, "method getInstance");
        if (mInstance == null) {
            mInstance = new UserData();
        }
        return mInstance;
    }

    //***** USER SETUP DATA *****

    //[0] set and get the userName string parameter - this is used in part to define the userData object
    public void setUserName(String user) {
        //Log.d(TAG, "method setUserName: user name is: " + user);
        userName = user;
    }

    public String getUserName() {
        //Log.d(TAG, "method getUserName returns: " + userName);
        return userName;
    }

    //***** PRE SURVEY DATA *****

    //[1] set and get the user's ageRange
    public void setAgeRange(String age) {
        //Log.d(TAG, "method setAgeRange: age: " + age);
        ageRange = age;
    }

    public String getAgeRange() {
        //Log.d(TAG, "method getAgeRange: ageRange: " + ageRange);
        return ageRange;
    }

    //[2] set and get yearsTwitchingRange variable
    public void setYearsTwitchingRange(String yearsTwitching) {
        //Log.d(TAG, "method setYearsTwitchingRange: yearsTwitching: " + yearsTwitching);
        yearsTwitchingRange = yearsTwitching;
    }

    public String getYearsTwitchingRange() {
        //Log.d(TAG, "method getYearsTwitchingRange: yearsTwitchingRange: " + yearsTwitchingRange);
        return yearsTwitchingRange;
    }

    //[3] set and get the speciesKnownRange variable
    public void setSpeciesKnownRange(String speciesKnown) {
        //Log.d(TAG, "method getKnownSpeciesRange: speciesKnown: " + speciesKnown);
        speciesKnownRange = speciesKnown;
    }

    public String getSpeciesKnownRange() {
        //Log.d(TAG, "method setKnownSpeciesRange: speciesKnownRange: " + speciesKnownRange);
        return speciesKnownRange;
    }

    //[4] set and get the audibleRecognizedRange
    public void setAudibleRecognizedRange(String audibleRecognized) {
        //Log.d(TAG, "method setAudibleRecognizedRange: audibleRecognized: " + audibleRecognized);
        audibleRecognizedRange = audibleRecognized;
    }

    public String getAudibleRecognizedRange() {
        //Log.d(TAG, "method getAudibleRecognizedRange: audibleRecognizedRange: " + audibleRecognizedRange);
        return audibleRecognizedRange;
    }

    //[5] set and get the interfaceExperienceRange
    public void setInterfaceExperienceRange(String interfaceExperience) {
        //Log.d(TAG, "method setInterfaceExperienceRange: interfaceExperience: " + interfaceExperience);
        interfaceExperienceRange = interfaceExperience;
    }

    public String getInterfaceExperienceRange() {
        //Log.d(TAG, "method getInterfaceExperienceRange: interfaceExperienceRange: " + interfaceExperienceRange);
        return interfaceExperienceRange;
    }

    //[6] set and get the hearingEqualsSeeing boolean
    public void setHearingEqualsSeeing(boolean isHearingSeeing) {
        //Log.d(TAG, "method setHearingEqualsSeeing: isHearingSeeing: " + isHearingSeeing);
        hearingEqualsSeeing = isHearingSeeing;
    }

    public boolean getHearingEqualsSeeing() {
        //Log.d(TAG, "method getHearingEqualsSeeing: hearingEqualsSeeing: " + hearingEqualsSeeing);
        return hearingEqualsSeeing;
    }

    //[7] set and get the hasUsedSmartPhone boolean
    public void setHasUsedSmartPhone(boolean usedSmartPhone) {
        //Log.d(TAG, "method setHasUsedSmartPhone: " + usedSmartPhone);
        hasUsedSmartPhone = usedSmartPhone;
    }

    public boolean getHasUsedSmartphone() {
        //Log.d(TAG, "method getHasUsedSmartphone: hasUsedSmartPhone: " + hasUsedSmartPhone);
        return hasUsedSmartPhone;
    }

    //****** POST SURVEY DATA *****
    //TODO is there a way we can 'force' the user to provide this information?
    //[8] get and set spectrogramFamiliar boolean
    public void setSpectrogramFamiliar(boolean isFamiliar) {
        //Log.d(TAG, "method setSpectrogramFamiliar: isFamiliar: " + isFamiliar);
        spectrogramFamiliar = isFamiliar;
    }

    public boolean getSpectrogramFamiliar() {
        //Log.d(TAG, "method getSpectrogramFamiliar: spectrogramFamiliar: " + spectrogramFamiliar);
        return spectrogramFamiliar;
    }

    //[9] get and set the hearIsSeeLikert integer value
    public void setHearIsSeeLikert(int likertValue) {
        //Log.d(TAG, "method setHearIsSeeLikert: likertValue: " + likertValue);
        hearIsSeeLikert = likertValue;
    }

    public int getHearIsSeeLikert() {
        //Log.d(TAG, "method getHearIsSeeLikert: hearIsSeeLikert: " + hearIsSeeLikert);
        return hearIsSeeLikert;
    }

    //[10] get and set the hearIsPredictLikert integer value
    public void setHearIsPredictLikert(int likertValue) {
        //Log.d(TAG, "method setHearIsPredictLikert: likertValue: " + likertValue);
        hearIsPredictLikert = likertValue;
    }

    public int getHearIsPredictLikert() {
        //Log.d(TAG, "method getHearIsPredictLikert: hearIsPredictLikert: " + hearIsPredictLikert);
        return hearIsPredictLikert;
    }

    //***** MEMORY GAME DATA *****

    /*
     * MemGameData constructor, accessor, mutator follow:
     */
    public void initMemGameDataList() {
        Log.d(TAG, "method initMemGameDataList");
        memGameDataList = new ArrayList<MemGameData>();
    }

    public void appendMemGameData(MemGameData game) {
        Log.d(TAG, "method addToGameDataList: adding game data to list");
        memGameDataList.add(game);        //TODO add try/catch block here
    }

    /*
     * Method queryMemGameData returns a GameData object at position in the list gameDataRecord.
     */
    public MemGameData queryMemGameDataList(int gameDataRecord) {
        Log.d(TAG, "method queryMemGameDataList");
        return memGameDataList.get(gameDataRecord);
    }

    public void setCurMemGame(MemGameData gameData) {
        Log.d(TAG, "method setCurMemGame");
        curMemGame = gameData;
    }

    public MemGameData getCurMemGame() {
        //Log.d(TAG, "method getCurMemGame");
        return curMemGame;
    }

    public int sizeOfMemGameDataList () {
        //
        return memGameDataList.size();
    }
}