package com.jpventura.capstone.boundary;

public interface IGoogleController extends IAndroidController {
    int SIGNED_IN = 0;
    int SIGN_IN_REQUIRED = 1;
    int OPENING = 2;
    int CLOSED = 3;

    void connect();
    void disconnect();
    void revoke();
}
