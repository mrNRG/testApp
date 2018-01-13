package com.ivoronyuk.test.socketapp.communication.client;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public interface ConnectionEventListener {
    void onConnected();

    void onDisconnected();

    void onServerBusy();

    void setHost();
}
