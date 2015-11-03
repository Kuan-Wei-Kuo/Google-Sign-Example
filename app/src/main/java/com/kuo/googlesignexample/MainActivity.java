package com.kuo.googlesignexample;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final int PROFILE_PIC_SIZE = 400;

    private boolean mIntentInProgress;
    private boolean mSignInClicked;

    private Toolbar toolbar;
    private SignInButton btnSignIn;
    private ImageView personPic;
    private TextView profile;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;

    private String personName = "";
    private String personPhotoUrl  = "";
    private String personGooglePlusProfile  = "";
    private String email  = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        personPic = (ImageView) findViewById(R.id.personPic);
        profile = (TextView) findViewById(R.id.profile);

        toolbar.setTitle("Google Sign Example");
        setSupportActionBar(toolbar);

        btnSignIn.setOnClickListener(onClickListener);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.d("result", result.toString());

        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            mConnectionResult = result;
            if (mSignInClicked) {
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {

        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        personName = currentPerson.getDisplayName();
        personPhotoUrl = currentPerson.getImage().getUrl();
        personGooglePlusProfile = currentPerson.getUrl();
        email = Plus.AccountApi.getAccountName(mGoogleApiClient);

        personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 2) + PROFILE_PIC_SIZE;

        new DownloadImageTask((ImageView) findViewById(R.id.personPic)).execute(personPhotoUrl);

        profile.setText("personName : " + personName + "\n" + "personGooglePlusProfile : " + personGooglePlusProfile + "\n" + "email : " + email);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private SignInButton.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mGoogleApiClient.isConnecting()) {
                mSignInClicked = true;
                resolveSignInError();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}