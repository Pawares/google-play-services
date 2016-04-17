package com.jpventura.capstone;

import android.content.Intent;

public interface IAndroidController {
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onCreate();
    void onDestroy();
    void onStart();
    void onStop();
}
