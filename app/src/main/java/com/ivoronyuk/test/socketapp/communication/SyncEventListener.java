package com.ivoronyuk.test.socketapp.communication;

import com.ivoronyuk.test.socketapp.communication.ActionSync;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public interface SyncEventListener {
    void onDataSync(ActionSync actionSync);
}
