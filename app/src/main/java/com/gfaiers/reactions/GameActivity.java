package com.gfaiers.reactions;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    private static final int[] BUTTON_IDS = {
            R.id.imageButton1, R.id.imageButton2, R.id.imageButton3, R.id.imageButton4, R.id.imageButton5,
            R.id.imageButton6, R.id.imageButton7, R.id.imageButton8, R.id.imageButton9, R.id.imageButton10,
            R.id.imageButton11, R.id.imageButton12, R.id.imageButton13, R.id.imageButton14, R.id.imageButton15,
            R.id.imageButton16, R.id.imageButton17, R.id.imageButton18, R.id.imageButton19, R.id.imageButton20,
            R.id.imageButton21, R.id.imageButton22, R.id.imageButton23, R.id.imageButton24, R.id.imageButton25
    };

    final static String PREFERENCE_SCORES = "scores";
    private AdView mAdView;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    static int intPressed = 0, intActive = 0, intScore = 0, intAdd = 0, intCounter = 4,
            intGameLengthMS = 0, intGameLength = 0, intHighScore, intCount = R.drawable.three,
            intHighScoreTen, intHighScoreFifteen;
    static String strCounter = "", strGameLength = "", strScore = "";
    static boolean booLong = false, booBadTouch = false, booFirstPlay, booNoRate, booTempDismiss;
    static long lngTimesRan;

    Timer locTimer;
    CountDownTimer gTimer, stTimer;
    LocationTimer locationTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        MobileAds.initialize(this, getResources().getString(R.string.banner_ad_unit_id_java));
        mAdView = (AdView) findViewById(R.id.adViewBanner);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        intHighScoreTen = getIntent().getIntExtra("intHighScoreTen",0);
        intHighScoreFifteen = getIntent().getIntExtra("intHighScoreFifteen", 0);
        intGameLengthMS = getIntent().getIntExtra("intGameLength",0);
        booFirstPlay = getIntent().getBooleanExtra("settingFirstPlay", true);
        booNoRate = getIntent().getBooleanExtra("settingNoRate", false);
        lngTimesRan = getIntent().getLongExtra("settingTimesRan", 0);
        booTempDismiss = getIntent().getBooleanExtra("booTempDismiss", false);

        booLong = (intGameLengthMS == 15000);
        if (booLong) {
            intHighScore = intHighScoreFifteen;
        } else {
            intHighScore = intHighScoreTen;
        }
        intGameLength = intGameLengthMS / 1000;
        // Handle which imageButton is clicked
        for (int id: BUTTON_IDS){
            final ImageButton imageButton = (ImageButton) findViewById(id);
            if (imageButton != null) {
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (imageButton.getId()){
                            case R.id.imageButton1: intPressed = 1; break;
                            case R.id.imageButton2: intPressed = 2; break;
                            case R.id.imageButton3: intPressed = 3; break;
                            case R.id.imageButton4: intPressed = 4; break;
                            case R.id.imageButton5: intPressed = 5; break;
                            case R.id.imageButton6: intPressed = 6; break;
                            case R.id.imageButton7: intPressed = 7; break;
                            case R.id.imageButton8: intPressed = 8; break;
                            case R.id.imageButton9: intPressed = 9; break;
                            case R.id.imageButton10: intPressed = 10; break;
                            case R.id.imageButton11: intPressed = 11; break;
                            case R.id.imageButton12: intPressed = 12; break;
                            case R.id.imageButton13: intPressed = 13; break;
                            case R.id.imageButton14: intPressed = 14; break;
                            case R.id.imageButton15: intPressed = 15; break;
                            case R.id.imageButton16: intPressed = 16; break;
                            case R.id.imageButton17: intPressed = 17; break;
                            case R.id.imageButton18: intPressed = 18; break;
                            case R.id.imageButton19: intPressed = 19; break;
                            case R.id.imageButton20: intPressed = 20; break;
                            case R.id.imageButton21: intPressed = 21; break;
                            case R.id.imageButton22: intPressed = 22; break;
                            case R.id.imageButton23: intPressed = 23; break;
                            case R.id.imageButton24: intPressed = 24; break;
                            case R.id.imageButton25: intPressed = 25; break;
                            default:
                                break;
                        }
                        imageButtonClicked();
                    }
                });
            }
        }
    }

    @Override
    protected void onPause(){
        if (mAdView != null){
            mAdView.pause();
        }
        if (locationTimer != null) {
            locationTimer.cancel();
            locationTimer = null;
        }
        if (stTimer != null) {
            stTimer.cancel();
            stTimer = null;
        }
        if (gTimer != null) {
            gTimer.cancel();
            gTimer = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mAdView != null){
            mAdView.resume();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();

        // Import the images to the ImageButtons
        for (int id: BUTTON_IDS) {
            final ImageButton imageButton = (ImageButton) findViewById(id);
            if (imageButton != null){
                imageButton.setImageResource(R.drawable.zero_blue);
                imageButton.setVisibility(View.GONE);
            }
        }

        newGame();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();

        // Handle the memory management of the images
        for (int id: BUTTON_IDS) {
            final ImageButton imageButton = (ImageButton) findViewById(id);
            if (imageButton != null){
                Drawable d = imageButton.getDrawable();
                if (d != null) d.setCallback(null);
                imageButton.setImageDrawable(null);
            }
        }
        ImageView imageViewCountDown = (ImageView) findViewById(R.id.imageViewCountDown);
        Drawable d = imageViewCountDown.getDrawable();
        if (d != null) d.setCallback(null);
        imageViewCountDown.setImageDrawable(null);
    }

    @Override
    protected void onDestroy(){
        if (mAdView != null){
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle connectionHint){

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        if (mResolvingConnectionFailure) {
            return;
        }
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getResources().getString(R.string.sign_in_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.sign_in_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.sign_in_failure);
            }
        }
    }

    protected void newGame(){
        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        strGameLength = Integer.toString(intGameLength);
        textViewTime.setText(strGameLength);
        TextView textViewScore = (TextView) findViewById(R.id.textViewScore);
        strScore = Integer.toString(intScore);
        textViewScore.setText(strScore);
        resetVariables();
        ImageView imageViewCountDown = (ImageView) findViewById(R.id.imageViewCountDown);
        imageViewCountDown.setVisibility(View.VISIBLE);
        imageViewCountDown.setImageResource(intCount);
        stTimer = new CountDownTimer(2500,500) {
            @Override
            public void onTick(long l) {

                switch(intCounter){
                    case 4: intCount = R.drawable.three ; break;
                    case 3: intCount = R.drawable.two ; break;
                    case 2: intCount = R.drawable.one; break;
                    case 1: intCount = R.drawable.zero_red; break;
                    default: break;
                }
                // Make label display contents of strCounter
                ImageView imageViewCountDown = (ImageView) findViewById(R.id.imageViewCountDown);
                imageViewCountDown.setImageResource(intCount);
                intCounter --;
            }

            @Override
            public void onFinish() {

                for (int id: BUTTON_IDS) {
                    final ImageButton imageButton = (ImageButton) findViewById(id);
                    if (imageButton != null){
                        imageButton.setVisibility(View.VISIBLE);
                        imageButton.setImageResource(R.drawable.zero_blue);
                    }
                }

                ImageView imageViewCountDown = (ImageView) findViewById(R.id.imageViewCountDown);
                Drawable d = imageViewCountDown.getDrawable();
                if (d != null) d.setCallback(null);
                imageViewCountDown.setImageDrawable(null);
                imageViewCountDown.setVisibility(View.GONE);

                strGameLength = Integer.toString(intGameLength);
                TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
                textViewTime.setText(strGameLength);
                strCounter = "";
                intAdd = 9;
                locTimer = new Timer();
                locationTimer = new LocationTimer();
                locTimer.schedule(locationTimer, 0, 125);
                newLocation();
                gTimer = new CountDownTimer(intGameLengthMS, 1000) {
                    @Override
                    public void onTick(long l) {
                        intGameLength --;
                        strGameLength = Integer.toString(intGameLength);
                        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
                        textViewTime.setText(strGameLength);
                    }

                    @Override
                    public void onFinish() {
                        // END THE GAME
                        if (locationTimer != null) {
                            locationTimer.cancel();
                            locationTimer = null;
                        }
                        for (int id: BUTTON_IDS) {
                            final ImageButton imageButton = (ImageButton) findViewById(id);
                            if (imageButton != null){
                                Drawable d = imageButton.getDrawable();
                                if (d != null) d.setCallback(null);
                                imageButton.setImageDrawable(null);
                                imageButton.setVisibility(View.GONE);
                            }
                        }

                        intGameLength --;
                        strGameLength = Integer.toString(intGameLength);
                        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
                        textViewTime.setText(strGameLength);

                        if (intScore > intHighScore) {
                            SharedPreferences scores = getSharedPreferences(PREFERENCE_SCORES, MODE_PRIVATE);
                            SharedPreferences.Editor editor = scores.edit();
                            if (booLong) {
                                editor.putInt("scoresFifteen", intScore);
                            } else {
                                editor.putInt("scoresTen", intScore);
                            }
                            editor.apply();
                            intHighScore = intScore;
                            endMessage(getResources().getString(R.string.game_finished_high_score), getResources().getString(R.string.game_finished_play_again), getResources().getString(R.string.ok), getResources().getString(R.string.back), intScore);
                        } else {
                            endMessage(getResources().getString(R.string.game_finished_low_score), getResources().getString(R.string.game_finished_play_again), getResources().getString(R.string.ok), getResources().getString(R.string.back), intScore);
                        }
                        // DISPLAY Dialog Fragment stating the score, and if they want to play again
                        // If new top score, display new top score message
                        if (booLong) {
                            // Fifteen seconds game
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_75_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_750_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_7500_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_75000_seconds), 1);
                            if (!booBadTouch) {
                                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_perfect_15_seconds));
                            }
                            Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fifteen_seconds), intScore);
                        } else {
                            // Ten seconds game
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_50_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_500_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_5000_seconds), 1);
                            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_50000_seconds), 1);
                            if (!booBadTouch) {
                                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_perfect_10_seconds));
                            }
                            Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_ten_seconds), intScore);
                        }
                    }
                }.start();
            }
        }.start();
    }

    protected void resetVariables(){
        intGameLength = intGameLengthMS / 1000;
        intPressed = 0;
        intActive = 0;
        intScore = 0;
        intAdd = 0;
        intCounter = 4;
        intCount = R.drawable.three;
        strCounter = "3";
        strGameLength = "";
        strScore = "";
        booBadTouch = false;
    }

    private void newLocation() {
        // This is called when the right button has been clicked
        int intRand = (int) (Math.random() * 25) + 1;
        int intCountButtons = 0;
        for (int id: BUTTON_IDS) {
            intCountButtons ++;
            final ImageButton imageButton = (ImageButton) findViewById(id);
            if (imageButton != null){
                // Set the button that had the counting image as default
                // Set the new button as counting down
                if (intCountButtons == intActive) imageButton.setImageResource(R.drawable.zero_blue);
                if (intCountButtons == intRand) imageButton.setImageResource(R.drawable.nine);
            }
        }
        intActive = intRand;
        intAdd = 9;
        if (locationTimer != null) {
            locationTimer.cancel();
            locationTimer = null;
            locTimer = new Timer();
            locationTimer = new LocationTimer();
            locTimer.schedule(locationTimer, 0, 125);
        }
    }

    private void imageButtonClicked() {

        // If the right button is pressed, then reward, else punish
        if (intPressed == intActive){
            // Right button is pressed
            // Add the points they've earned
            intScore = intScore + intAdd;
            // Pick a new different location
            newLocation();
        } else {
            // Wrong button is pressed
            // Remove the points they would have gained
            intScore = intScore - intAdd;
            booBadTouch = true;
        }
        strScore = Integer.toString(intScore);
        TextView textViewScore = (TextView) findViewById(R.id.textViewScore);
        textViewScore.setText(strScore);
    }

    protected void endMessage(String strMessageTop, String strMessageBottom, String strPositive, String strNegative, int intEndMessageScore) {

        final Dialog dialog = new Dialog(GameActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fragment_dialog);

        // Section for displaying score with images
        ImageView imageViewScore1 = (ImageView) dialog.findViewById(R.id.imageViewScore1);
        ImageView imageViewScore2 = (ImageView) dialog.findViewById(R.id.imageViewScore2);
        ImageView imageViewScore3 = (ImageView) dialog.findViewById(R.id.imageViewScore3);
        imageViewScore1.setVisibility(View.GONE);
        imageViewScore2.setVisibility(View.GONE);
        imageViewScore3.setVisibility(View.GONE);
        char chrFirst, chrSecond, chrThird;
        int intFirst, intSecond, intThird;
        if (intEndMessageScore >= 100) {
            // Display score in all 3 squares
            chrFirst = String.valueOf(intEndMessageScore).charAt(0);
            chrSecond = String.valueOf(intEndMessageScore).charAt(1);
            chrThird = String.valueOf(intEndMessageScore).charAt(2);
            imageViewScore1.setVisibility(View.VISIBLE);
            imageViewScore2.setVisibility(View.VISIBLE);
            imageViewScore3.setVisibility(View.VISIBLE);
        } else if (intEndMessageScore >= 10) {
            // Display score in 2 squares
            chrFirst = 0;
            chrSecond = String.valueOf(intEndMessageScore).charAt(0);
            chrThird = String.valueOf(intEndMessageScore).charAt(1);
            imageViewScore2.setVisibility(View.VISIBLE);
            imageViewScore3.setVisibility(View.VISIBLE);
        } else if (intEndMessageScore >= 1) {
            // Display score in 1 of the squares
            chrFirst = 0;
            chrSecond = 0;
            chrThird = String.valueOf(intEndMessageScore).charAt(0);
            imageViewScore3.setVisibility(View.VISIBLE);
        } else {
            // Score is 0 or less display no score
            chrFirst = 0;
            chrSecond = 0;
            chrThird = 0;
        }
        // Now we have the each digit split down to the individual integers
        intFirst = Character.getNumericValue(chrFirst);
        intSecond = Character.getNumericValue(chrSecond);
        intThird = Character.getNumericValue(chrThird);
        switch (intFirst) {
            case 0: imageViewScore2.setImageResource(R.drawable.zero_blue); break;
            case 1: imageViewScore1.setImageResource(R.drawable.one); break;
            case 2: imageViewScore1.setImageResource(R.drawable.two); break;
            case 3: imageViewScore1.setImageResource(R.drawable.three); break;
            case 4: imageViewScore1.setImageResource(R.drawable.four); break;
            case 5: imageViewScore1.setImageResource(R.drawable.five); break;
            case 6: imageViewScore1.setImageResource(R.drawable.six); break;
            case 7: imageViewScore1.setImageResource(R.drawable.seven); break;
            case 8: imageViewScore1.setImageResource(R.drawable.eight); break;
            case 9: imageViewScore1.setImageResource(R.drawable.nine); break;
            default: break;
        }
        switch (intSecond) {
            case 0: imageViewScore2.setImageResource(R.drawable.zero_blue); break;
            case 1: imageViewScore2.setImageResource(R.drawable.one); break;
            case 2: imageViewScore2.setImageResource(R.drawable.two); break;
            case 3: imageViewScore2.setImageResource(R.drawable.three); break;
            case 4: imageViewScore2.setImageResource(R.drawable.four); break;
            case 5: imageViewScore2.setImageResource(R.drawable.five); break;
            case 6: imageViewScore2.setImageResource(R.drawable.six); break;
            case 7: imageViewScore2.setImageResource(R.drawable.seven); break;
            case 8: imageViewScore2.setImageResource(R.drawable.eight); break;
            case 9: imageViewScore2.setImageResource(R.drawable.nine); break;
            default: break;
        }
        switch (intThird) {
            case 0: imageViewScore3.setImageResource(R.drawable.zero_blue); break;
            case 1: imageViewScore3.setImageResource(R.drawable.one); break;
            case 2: imageViewScore3.setImageResource(R.drawable.two); break;
            case 3: imageViewScore3.setImageResource(R.drawable.three); break;
            case 4: imageViewScore3.setImageResource(R.drawable.four); break;
            case 5: imageViewScore3.setImageResource(R.drawable.five); break;
            case 6: imageViewScore3.setImageResource(R.drawable.six); break;
            case 7: imageViewScore3.setImageResource(R.drawable.seven); break;
            case 8: imageViewScore3.setImageResource(R.drawable.eight); break;
            case 9: imageViewScore3.setImageResource(R.drawable.nine); break;
            default: break;
        }
        TextView textViewTop = (TextView) dialog.findViewById(R.id.textViewMessageTop);
        textViewTop.setVisibility(View.VISIBLE);
        textViewTop.setText(strMessageTop);

        TextView textViewBottom = (TextView) dialog.findViewById(R.id.textViewMessageBottom);
        textViewBottom.setVisibility(View.VISIBLE);
        textViewBottom.setText(strMessageBottom);

        Button buttonPositive = (Button) dialog.findViewById(R.id.buttonPositive);
        buttonPositive.setVisibility(View.VISIBLE);
        buttonPositive.setText(strPositive);
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetVariables();
                ImageView imageViewScore1 = (ImageView) dialog.findViewById(R.id.imageViewScore1);
                Drawable d = imageViewScore1.getDrawable();
                if (d != null) d.setCallback(null);
                imageViewScore1.setImageDrawable(null);

                ImageView imageViewScore2 = (ImageView) dialog.findViewById(R.id.imageViewScore2);
                Drawable d1 = imageViewScore2.getDrawable();
                if (d1 != null) d1.setCallback(null);
                imageViewScore2.setImageDrawable(null);

                ImageView imageViewScore3 = (ImageView) dialog.findViewById(R.id.imageViewScore3);
                Drawable d2 = imageViewScore3.getDrawable();
                if (d2 != null) d2.setCallback(null);
                imageViewScore3.setImageDrawable(null);

                dialog.dismiss();
                newGame();
            }
        });

        Button buttonNeutral = (Button) dialog.findViewById(R.id.buttonNeutral);
        buttonNeutral.setVisibility(View.GONE);

        Button buttonNegative = (Button) dialog.findViewById(R.id.buttonNegative);
        buttonNegative.setVisibility(View.VISIBLE);
        buttonNegative.setText(strNegative);
        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetVariables();
                ImageView imageViewScore1 = (ImageView) dialog.findViewById(R.id.imageViewScore1);
                Drawable d = imageViewScore1.getDrawable();
                if (d != null) d.setCallback(null);
                imageViewScore1.setImageDrawable(null);

                ImageView imageViewScore2 = (ImageView) dialog.findViewById(R.id.imageViewScore2);
                Drawable d1 = imageViewScore2.getDrawable();
                if (d1 != null) d1.setCallback(null);
                imageViewScore2.setImageDrawable(null);

                ImageView imageViewScore3 = (ImageView) dialog.findViewById(R.id.imageViewScore3);
                Drawable d2 = imageViewScore3.getDrawable();
                if (d2 != null) d2.setCallback(null);
                imageViewScore3.setImageDrawable(null);

                dialog.dismiss();
                Intent intentMain = new Intent(GameActivity.this, MainActivity.class);
                int result = 1;
                if (booLong) {
                    intentMain.putExtra("booTempDismiss", booTempDismiss);
                    intentMain.putExtra("intHighScoreTen", intHighScoreTen);
                    intentMain.putExtra("intHighScoreFifteen", intHighScore);
                    intentMain.putExtra("settingFirstPlay", booFirstPlay);
                    intentMain.putExtra("settingNoRate",booNoRate);
                    intentMain.putExtra("settingTimesRan", lngTimesRan);
                } else {
                    intentMain.putExtra("booTempDismiss", booTempDismiss);
                    intentMain.putExtra("intHighScoreTen", intHighScore);
                    intentMain.putExtra("intHighScoreFifteen", intHighScoreFifteen);
                    intentMain.putExtra("settingFirstPlay", booFirstPlay);
                    intentMain.putExtra("settingNoRate",booNoRate);
                    intentMain.putExtra("settingTimesRan", lngTimesRan);
                }
                startActivityForResult(intentMain, result);
            }
        });
        dialog.show();
    }

    class LocationTimer extends TimerTask {
        @Override
        public void run(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int intImage = 0;
                    // Set the value of intAdd, and change the image depending on it's value
                    switch (intAdd) {
                        case 9:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.eight);
                                    }
                                }
                            }
                            break;
                        case 8:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.seven);
                                    }
                                }
                            }
                            break;
                        case 7:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.six);
                                    }
                                }
                            }
                            break;
                        case 6:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.five);
                                    }
                                }
                            }
                            break;
                        case 5:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.four);
                                    }
                                }
                            }
                            break;
                        case 4:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.three);
                                    }
                                }
                            }
                            break;
                        case 3:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.two);
                                    }
                                }
                            }
                            break;
                        case 2:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.one);
                                    }
                                }
                            }
                            break;
                        case 1:
                            intAdd--;
                            for (int id : BUTTON_IDS) {
                                intImage++;
                                final ImageButton imageButton = (ImageButton) findViewById(id);
                                if (imageButton != null) {
                                    if (intImage == intActive) {
                                        // Change the image to the next lower number
                                        imageButton.setImageResource(R.drawable.zero_red);
                                    }
                                }
                            }
                            break;
                        case 0:
                            intAdd--;
                            newLocation();
                            break;
                    }
                }
            });
        }
    }
}
