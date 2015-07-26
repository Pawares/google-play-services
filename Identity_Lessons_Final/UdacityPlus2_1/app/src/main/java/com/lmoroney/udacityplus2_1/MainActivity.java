/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmoroney.udacityplus2_1;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lmoroney.udacityplus2_1.common.GoogleConnection;
import com.lmoroney.udacityplus2_1.common.State;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends ActionBarActivity implements Observer, View.OnClickListener {

    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;

    private GoogleConnection googleConnection;

    private static final String TAG = "ventura";

    @Override
    public void update(Observable observable, Object data) {
        if (observable != googleConnection) {
            return;
        }

        switch ((State) data) {
            case CREATED:
                dialog.dismiss();
                onSignedOutUI();
                break;
            case OPENING:
                dialog.show();
                break;
            case OPENED:
                dialog.dismiss();
                // Update the user interface to reflect that the user is signed in.
                mSignInButton.setEnabled(false);
                mSignOutButton.setEnabled(true);
                mRevokeButton.setEnabled(true);

                // We are signed in!
                // Retrieve some profile information to personalize our app for the user.
                try {
                    String emailAddress = googleConnection.getAccountName();
                    mStatus.setText(String.format("Signed In to My App as %s", emailAddress));
                } catch (Exception ex) {
                    String exception = ex.getLocalizedMessage();
                    String exceptionString = ex.toString();
                }

                break;
            case CLOSED:
                dialog.dismiss();
                onSignedOutUI();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
        mStatus = (TextView) findViewById(R.id.sign_in_status);

        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);

        googleConnection = GoogleConnection.getInstance(this);
        googleConnection.addObserver(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        googleConnection.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        googleConnection.deleteObserver(this);
        googleConnection.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GoogleConnection.REQUEST_CODE == requestCode) {
            googleConnection.onActivityResult(resultCode);
        }
    }

    private void onSignedOutUI() {
        // Update the UI to reflect that the user is signed out.
        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);

        mStatus.setText("Signed out");

    }

    @Override
    public void onClick(View v) {

            // We only process button clicks when GoogleApiClient is not transitioning
            // between connected and not connected.
            switch (v.getId()) {
                case R.id.sign_in_button:
                    mStatus.setText("Signing In");
                    googleConnection.connect();
                    break;
                case R.id.sign_out_button:
                    googleConnection.disconnect();
                    break;
                case R.id.revoke_access_button:
                    googleConnection.revokeAccessAndDisconnect();
                    break;
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AlertDialog dialog;
}
