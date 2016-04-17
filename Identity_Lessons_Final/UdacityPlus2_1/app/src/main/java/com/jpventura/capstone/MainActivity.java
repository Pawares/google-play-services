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
package com.jpventura.capstone;

import android.accounts.Account;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jpventura.capstone.boundary.IGoogleController;
import com.jpventura.capstone.controller.GoogleController;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends ActionBarActivity implements Observer, View.OnClickListener {
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;

    private IGoogleController mGoogleController;

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

        mGoogleController = GoogleController.instance(this);
        mGoogleController.onCreate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleController.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleController.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mGoogleController.onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGoogleController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                mGoogleController.connect();
                break;
            case R.id.sign_out_button:
                mGoogleController.disconnect();
                break;
            case R.id.revoke_access_button:
                mGoogleController.revoke();
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

    @Override
    public void update(Observable controller, Object state) {
        Account account;
        switch ((Integer) state) {
            case IGoogleController.SIGNED_IN:
                // Update the user interface to reflect that the user is signed in.
                mSignInButton.setEnabled(false);
                mSignOutButton.setEnabled(true);
                mRevokeButton.setEnabled(true);

                // We are signed in!
                // Retrieve some profile information to personalize our app for the user.
                try {
                    String emailAddress = "qq coisa"; // Plus.AccountApi.getAccountName(mGoogleApiClient);
                    mStatus.setText(String.format("Signed In to My App as %s", emailAddress));
                } catch(Exception ex) {
                    String exception = ex.getLocalizedMessage();
                    String exceptionString = ex.toString();
                }
                break;
            case IGoogleController.SIGN_IN_REQUIRED:
                break;
            case IGoogleController.OPENING:
                mStatus.setText("Signing In");
                break;
            case IGoogleController.CLOSED:
                // Update the UI to reflect that the user is signed out.
                mSignInButton.setEnabled(true);
                mSignOutButton.setEnabled(false);
                mRevokeButton.setEnabled(false);
                mStatus.setText("Signed out");
                break;
        }
    }
}
