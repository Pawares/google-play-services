package com.jpventura.capstone;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.lang.ref.WeakReference;

public class GoogleController implements ConnectionCallbacks, IGoogleController, OnConnectionFailedListener {
    public static final int REQUEST_CODE = 0x90091e;

    private static final Object sLock = new Object();
    private static GoogleController sGoogleController;

    private WeakReference<MainActivity> mActivity;
    private GoogleApiClient mGoogleApiClient;
    private int mSignInProgress;
    private ConnectionResult mConnectionResult;

    public static synchronized IGoogleController instance(MainActivity activity) {
        if (null == sGoogleController) {
            synchronized (sLock) {
                sGoogleController = new GoogleController(activity);
            }
        }
        return sGoogleController;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE != requestCode) return;

        if (resultCode == Activity.RESULT_OK) {
            // If the error resolution was successful we should continue
            // processing errors.
            setSessionState(SIGN_IN_REQUIRED);
        } else {
            // If the error resolution was not successful or the user canceled,
            // we should stop processing errors.
            setSessionState(SIGNED_IN);
        }

        // If Google Play services resolved the issue with a dialog then
        // onStart is not called so we need to re-attempt connection here.
        mGoogleApiClient.connect();
    }

    @Override
    public void onCreate() {
        Log.e("ventura", "Controller.onCreate()");
        // addObserver(mActivity.get());
        mGoogleApiClient = buildApiClient();

        if (mGoogleApiClient.isConnected()) {
            setSessionState(SIGNED_IN);
        } else if (mConnectionResult == null) {
            setSessionState(CLOSED);
        }
    }

    @Override
    public void onDestroy() {
        mOnChangeListener = null;
        Log.e("ventura", "Controller.onDestroy()");
    }

    @Override
    public void onStart() {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private IGoogleController.OnChangeListener mOnChangeListener;

    @Override
    public void setOnChangeListener(IGoogleController.OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    @Override
    public void onConnected(Bundle bundle) {
        setSessionState(SIGNED_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mSignInProgress != OPENING) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mConnectionResult = connectionResult;

            if (mSignInProgress == SIGN_IN_REQUIRED) {
                // STATE_SIGNING_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                connect();
            }
        }

        setSessionState(CLOSED);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void connect() {
        if (mGoogleApiClient.isConnecting() || (null == mConnectionResult)) return;

        // We have an intent which will allow our user to sign in or
        // resolve an error.  For example if the user needs to
        // select an account to sign in with, or if they need to consent
        // to the permissions your app is requesting.
        try {
            // Send the pending intent that we stored on the most recent
            // OnConnectionFailed callback.  This will allow the user to
            // resolve the error currently preventing our connection to
            // Google Play services.
            setSessionState(OPENING);
            mConnectionResult.startResolutionForResult(mActivity.get(), REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            // The intent was canceled before it was sent.  Attempt to connect to
            // get an updated ConnectionResult.
            setSessionState(SIGN_IN_REQUIRED);
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void disconnect() {
        // We only process button clicks when GoogleApiClient is not transitioning
        // between connected and not connected.
        if (mGoogleApiClient.isConnecting()) return;

        // We clear the default account on sign out so that Google Play
        // services will not return an onConnected callback without user
        // interaction.
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }

    @Override
    public void revoke() {
        // We only process button clicks when GoogleApiClient is not transitioning
        // between connected and not connected.
        if (mGoogleApiClient.isConnecting()) return;

        // After we revoke permissions for the user with a GoogleApiClient
        // instance, we must discard it and create a new one.
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        // Our sample has caches no user data from Google+, however we
        // would normally register a callback on revokeAccessAndDisconnect
        // to delete user data so that we comply with Google developer
        // policies.
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
        mGoogleApiClient = buildApiClient();
        mGoogleApiClient.connect();
    }

    private GoogleController(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    private GoogleApiClient buildApiClient() {
        return new GoogleApiClient.Builder(mActivity.get())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope("email"))
                .build();
    }

    private void setSessionState(int sessionState) {
        mSignInProgress = sessionState;
        if (null != mOnChangeListener) {
            mOnChangeListener.onChange(mSignInProgress);
        }
    }
}
