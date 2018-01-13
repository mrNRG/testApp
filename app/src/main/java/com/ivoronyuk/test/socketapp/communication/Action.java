package com.ivoronyuk.test.socketapp.communication;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class Action<T> {

    ActionType mType;
    T mData;

    public Action(ActionType type, T data) {
        mType = type;
        mData = data;
    }

    public ActionType getAction() {
        return mType;
    }

    public T getData() {
        return mData;
    }
}
