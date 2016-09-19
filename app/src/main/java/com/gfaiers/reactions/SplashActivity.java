package com.gfaiers.reactions;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final static String PREFERENCE_SETTINGS = "settings";
    final static String PREFERENCE_SCORES = "scores";
    static CountDownTimer cdTimer;
    static int intHighScoreTen, intHighScoreFifteen;
    static long lngTimesRan;
    static Boolean booFirstPlay = true, booNoRate = false;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Start the timer to load the main activity
        loadGame();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // Load the settings and scores into the game
        SharedPreferences settings = getSharedPreferences(PREFERENCE_SETTINGS, MODE_PRIVATE);
        booFirstPlay = settings.getBoolean("settingFirstPlay", true);
        booNoRate = settings.getBoolean("settingNoRate", false);
        lngTimesRan = settings.getLong("settingTimesRan", 0);
        lngTimesRan ++;
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("settingTimesRan", lngTimesRan);
        editor.apply();
        SharedPreferences scores = getSharedPreferences(PREFERENCE_SCORES, MODE_PRIVATE);
        intHighScoreTen = scores.getInt("scoresTen", 0);
        intHighScoreFifteen = scores.getInt("scoresFifteen", 0);

        TextView textView = (TextView) findViewById(R.id.textViewLoadingMessage);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_secret_achievement));
                // Take to the Play Store and load the app on there
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //Try Google play
                intent.setData(Uri.parse(getResources().getString(R.string.google_play_hangman)));
                if (!MyStartActivity(intent)) {
                    //Market (Google play) app seems not installed, let's try to open a web browser
                    intent.setData(Uri.parse(getResources().getString(R.string.google_play_website_hangman)));
                    if (!MyStartActivity(intent)) {
                        //Well if this also fails, we have run out of options, inform the user.
                        Toast.makeText(SplashActivity.this, getResources().getString(R.string.failed_load), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
        ImageView imageViewAppIcon = (ImageView) findViewById(R.id.imageViewAppIcon);
        Drawable d = imageViewAppIcon.getDrawable();
        if (d != null) d.setCallback(null);
        imageViewAppIcon.setImageDrawable(null);
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

    public void loadGame(){

        cdTimer = new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent intentMain = new Intent(SplashActivity.this, MainActivity.class);
                final int result = 1;
                intentMain.putExtra("intHighScoreTen", intHighScoreTen);
                intentMain.putExtra("intHighScoreFifteen", intHighScoreFifteen);
                intentMain.putExtra("settingFirstPlay", booFirstPlay);
                intentMain.putExtra("settingNoRate",booNoRate);
                intentMain.putExtra("settingTimesRan", lngTimesRan);
                startActivityForResult(intentMain, result);
            }
        }.start();
    }

    private boolean MyStartActivity(Intent aIntent) {
        try
        {
            startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }

}
