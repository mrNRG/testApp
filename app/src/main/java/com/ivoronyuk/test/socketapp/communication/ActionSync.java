package com.ivoronyuk.test.socketapp.communication;

import com.ivoronyuk.test.socketapp.model.History;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class ActionSync {

    private final History history;

    public ActionSync(History history) {
        this.history = history;
    }

    public History getHistory() {
        return history;
    }
}
