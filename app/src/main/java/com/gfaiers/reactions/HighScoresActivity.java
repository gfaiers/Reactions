package com.gfaiers.reactions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class HighScoresActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static int intHighScoreTen, intHighScoreFifteen;
    static String strHighScoreTen, strHighScoreFifteen;
    private AdView mAdView;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);
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
        strHighScoreTen = Integer.toString(intHighScoreTen);
        strHighScoreFifteen = Integer.toString(intHighScoreFifteen);
        TextView textViewTen = (TextView) findViewById(R.id.textViewHighScoresTen);
        textViewTen.setText(strHighScoreTen);
        TextView textViewFifteen = (TextView) findViewById(R.id.textViewHighScoresFifteen);
        textViewFifteen.setText(strHighScoreFifteen);

        Button buttonBack = (Button) findViewById(R.id.buttonHighScoresBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button buttonLeaderboardTen = (Button) findViewById(R.id.buttonHighScoresTen);
        buttonLeaderboardTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getResources().getString(R.string.leaderboard_ten_seconds)), 1);
            }
        });

        Button buttonLeaderboardFifteen = (Button) findViewById(R.id.buttonHighScoresFifteen);
        buttonLeaderboardFifteen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getResources().getString(R.string.leaderboard_fifteen_seconds)), 1);
            }
        });

        Button buttonAchievements = (Button) findViewById(R.id.buttonHighScoresAchievements);
        buttonAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),1);
            }
        });
    }

    @Override
    protected void onPause(){
        if (mAdView != null){
            mAdView.pause();
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
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy(){
        if (mAdView != null){
            mAdView.destroy();
        }
        super.onDestroy();
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

    // Call when the sign-in button is clicked
    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    // Call when the sign-out button is clicked
    private void signOutClicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
    }

}
