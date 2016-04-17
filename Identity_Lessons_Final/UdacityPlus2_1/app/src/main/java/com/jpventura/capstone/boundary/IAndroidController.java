package com.jpventura.capstone.boundary;

import android.content.Intent;

import java.util.Observer;

public interface IAndroidController {
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onCreate(Observer activity);
    void onDestroy(Observer activity);
    void onStart();
    void onStop();
}
