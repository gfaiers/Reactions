package com.gfaiers.reactions;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    final static String PREFERENCE_SETTINGS = "settings";
    static int intHighScoreTen, intHighScoreFifteen, intGameLength;
    static boolean booFirstPlay, booNoRate;
    static long lngTimesRan;
    private AdView mAdView;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        // Pass the variables that are needed for the game to work and were loaded in the splash activity to the main activity
        intHighScoreTen = getIntent().getIntExtra("intHighScoreTen",0);
        intHighScoreFifteen = getIntent().getIntExtra("intHighScoreFifteen", 0);
        booFirstPlay = getIntent().getBooleanExtra("settingFirstPlay", true);
        booNoRate = getIntent().getBooleanExtra("settingNoRate", false);
        lngTimesRan = getIntent().getLongExtra("settingTimesRan", 0);

        Button buttonNewGameShort = (Button) findViewById(R.id.buttonMainNewGameShort);
        Button buttonNewGameLong = (Button) findViewById(R.id.buttonMainNewGameLong);
        Button buttonHighScores = (Button) findViewById(R.id.buttonMainHighScores);
        Button buttonWebsite = (Button) findViewById(R.id.buttonMainWebsite);


        if ((!booNoRate) && (lngTimesRan % 10 == 0)){
            RateMe(getResources().getString(R.string.enjoyed), getResources().getString(R.string.ok),
                    getResources().getString(R.string.not_yet), getResources().getString(R.string.never));
        }

        buttonNewGameShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentNewGameShort = new Intent(MainActivity.this, GameActivity.class);
                int result = 1;
                intGameLength = 10000;
                intentNewGameShort.putExtra("intGameLength", intGameLength);
                intentNewGameShort.putExtra("intHighScoreTen", intHighScoreTen);
                intentNewGameShort.putExtra("intHighScoreFifteen", intHighScoreFifteen);
                intentNewGameShort.putExtra("settingFirstPlay", booFirstPlay);
                intentNewGameShort.putExtra("settingNoRate",booNoRate);
                intentNewGameShort.putExtra("settingTimesRan", lngTimesRan);
                startActivityForResult(intentNewGameShort, result);
                finish();
            }
        });

        buttonNewGameLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentNewGameLong = new Intent(MainActivity.this, GameActivity.class);
                int result = 1;
                intGameLength = 15000;
                intentNewGameLong.putExtra("intGameLength", intGameLength);
                intentNewGameLong.putExtra("intHighScoreTen", intHighScoreTen);
                intentNewGameLong.putExtra("intHighScoreFifteen", intHighScoreFifteen);
                intentNewGameLong.putExtra("settingFirstPlay", booFirstPlay);
                intentNewGameLong.putExtra("settingNoRate",booNoRate);
                intentNewGameLong.putExtra("settingTimesRan", lngTimesRan);
                startActivityForResult(intentNewGameLong, result);
                finish();
            }
        });
        buttonHighScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentHighScores = new Intent(MainActivity.this, HighScoresActivity.class);
                int result = 1;
                intentHighScores.putExtra("intHighScoreTen", intHighScoreTen);
                intentHighScores.putExtra("intHighScoreFifteen", intHighScoreFifteen);
                startActivityForResult(intentHighScores, result);
            }
        });
        buttonWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.website_url)));
                //startActivity(browserIntent);

                // Take to the Play Store and load the app on there
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //Try Google play
                intent.setData(Uri.parse(getResources().getString(R.string.google_play)));
                if (!MyStartActivity(intent)) {
                    //Market (Google play) app seems not installed, let's try to open a web browser
                    intent.setData(Uri.parse(getResources().getString(R.string.google_play_website)));
                    if (!MyStartActivity(intent)) {
                        //Well if this also fails, we have run out of options, inform the user.
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.failed_load), Toast.LENGTH_SHORT).show();
                    }
                }
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

    // The following is for the pop up message asking to rate the app
    public void RateMe(String strMessage, String strPositive, String strNeutral, String strNegative){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.fragment_dialog);

        ImageView imageViewFirst = (ImageView) dialog.findViewById(R.id.imageViewScore1);
        imageViewFirst.setVisibility(View.GONE);
        ImageView imageViewSecond = (ImageView) dialog.findViewById(R.id.imageViewScore2);
        imageViewSecond.setVisibility(View.GONE);
        ImageView imageViewThird = (ImageView) dialog.findViewById(R.id.imageViewScore3);
        imageViewThird.setVisibility(View.GONE);
        TextView textViewMessageBottom = (TextView) dialog.findViewById(R.id.textViewMessageBottom);
        textViewMessageBottom.setVisibility(View.GONE);
        TextView textViewMessage = (TextView) dialog.findViewById(R.id.textViewMessageTop);
        textViewMessage.setVisibility(View.VISIBLE);
        textViewMessage.setText(strMessage);

        Button buttonPositive = (Button) dialog.findViewById(R.id.buttonPositive);
        buttonPositive.setVisibility(View.VISIBLE);
        buttonPositive.setText(strPositive);
        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User pressed "Yes"
                // Take to the Play Store and load the app on there
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //Try Google play
                intent.setData(Uri.parse(getResources().getString(R.string.google_play)));
                if (!MyStartActivity(intent)) {
                    //Market (Google play) app seems not installed, let's try to open a web browser
                    intent.setData(Uri.parse(getResources().getString(R.string.google_play_website)));
                    if (!MyStartActivity(intent)) {
                        //Well if this also fails, we have run out of options, inform the user.
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.failed_load), Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }
        });

        Button buttonNeutral = (Button) dialog.findViewById(R.id.buttonNeutral);
        buttonNeutral.setVisibility(View.VISIBLE);
        buttonNeutral.setText(strNeutral);
        buttonNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button buttonNegative = (Button) dialog.findViewById(R.id.buttonNegative);
        buttonNegative.setVisibility(View.VISIBLE);
        buttonNegative.setText(strNegative);
        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                booNoRate = true;
                SharedPreferences settings = getSharedPreferences(PREFERENCE_SETTINGS, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("settingNoRate", booNoRate);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.show();
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
