package com.ivoronyuk.test.socketapp;

import android.app.Application;
import android.content.Context;

import com.ivoronyuk.test.socketapp.model.History;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class App extends Application {
    // FIXME: 1/12/18 only for testing
    private static Context mContext;
    private static History mHistory;

    public static Context getContext() {
        return mContext;
    }

    public static History getHistory() {
        return mHistory;
    }

    public static void setHistory(History history) {
        mHistory = history;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mHistory = new History();
    }
}
