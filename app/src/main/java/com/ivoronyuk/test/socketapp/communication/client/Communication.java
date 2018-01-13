package com.ivoronyuk.test.socketapp.communication.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivoronyuk.test.socketapp.App;
import com.ivoronyuk.test.socketapp.communication.Action;
import com.ivoronyuk.test.socketapp.communication.ActionSync;
import com.ivoronyuk.test.socketapp.communication.ActionType;
import com.ivoronyuk.test.socketapp.communication.SocketListener;
import com.ivoronyuk.test.socketapp.communication.SyncEventListener;
import com.ivoronyuk.test.socketapp.communication.server.ServerService;
import com.ivoronyuk.test.socketapp.model.Column;
import com.ivoronyuk.test.socketapp.model.ColumnType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class Communication {

    private ServerService mServer;
    private Client mClient;
    private Gson mGson;

    private List<ConnectionEventListener> mConnectionEventListeners = new ArrayList<>();
    private List<SyncEventListener> mSyncEventListeners = new ArrayList<>();
    private SocketListener clientListener = new SocketListener() {
        @Override
        public void onMessageReceived(final String input) {
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(App.getContext(), "CLIENT RECEIVE" + input, Toast.LENGTH_LONG).show();
//                }
//            });

            try {
                Action action = mGson.fromJson(input, Action.class);
                switch (action.getAction()) {
                    case DISCONNECTED:
                        fireOnDisconnectedAction();
                        break;
                    case CONNECTED:
                        fireOnConnectedAction();
                        break;
                    case BUSY:
                        fireOnBusyAction();
                        break;
                    case SYNC:
                        Type syncType = new TypeToken<Action<ActionSync>>() {
                        }.getType();
                        Action<ActionSync> actionSync = mGson.fromJson(input, syncType);

                        fireOnSyncAction(actionSync.getData());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private SocketListener serverListener = new SocketListener() {
        @Override
        public void onMessageReceived(final String input) {
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(App.getContext(), "SERVER RECEIVE" + input, Toast.LENGTH_LONG).show();
//                }
//            });

            try {
                Action action = mGson.fromJson(input, Action.class);
                switch (action.getAction()) {
                    case DISCONNECTED:
                        break;
                    case CONNECTED:
                        fireOnSetHost();
                        sendSyncAction(new ActionSync(App.getHistory()));
                        break;
                    case BUSY:
                        break;
                    case SYNC:
                        mServer.sendToAll(input);
                        break;
                    case COLUMN_ADD:
                        Type columnTypeAdd = new TypeToken<Action<Column>>() {
                        }.getType();
                        Action<Column> addAction = mGson.fromJson(input, columnTypeAdd);
                        Column columnAdd = addAction.getData();

                        App.getHistory().addColumnsByType(columnAdd.getType());

                        sendSyncAction(new ActionSync(App.getHistory()));
                        break;
                    case COLUMN_REMOVE:
                        Type columnTypeRemove = new TypeToken<Action<Column>>() {
                        }.getType();
                        Action<Column> removeAction = mGson.fromJson(input, columnTypeRemove);
                        Column columnRemove = removeAction.getData();

                        if (columnRemove.getType().equals(ColumnType.AAA)) {
                            App.getHistory().removeColumnAAA();
                        } else App.getHistory().removeColumnBBB();

                        sendSyncAction(new ActionSync(App.getHistory()));
                        break;
                    case CLEAR_ALL:
                        App.getHistory().clearAll();
                        sendSyncAction(new ActionSync(App.getHistory()));
                        break;
                    case HISTORY:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public Communication() {
        this.mGson = new Gson();
    }

    public void connect(String ipAddress) {
        mClient = new Client();
        mClient.addSocketListener(clientListener);
        mClient.connect(ipAddress);
    }

    public void disconnect() throws IOException {
        if (mClient != null)
            mClient.disconnect();
    }

    public void addServer(ServerService server) {
        this.mServer = server;
        this.mServer.addSocketListener(serverListener);
    }

    public void startServer() {
        if (mServer != null) {
            mServer.start();
        }
    }

    public void stopServer() throws IOException {
        if (mServer != null) {
            mServer.stop();
        }
    }

    public void sendColumnAddAction(ColumnType type) throws IOException {
        Action action = new Action(ActionType.COLUMN_ADD, new Column(type));
        send(action);
    }

    public void sendColumnRemoveAction(ColumnType type) throws IOException {
        Action action = new Action(ActionType.COLUMN_REMOVE, new Column(type));
        send(action);
    }

    public void sendAllClearAction() throws IOException {
        Action action = new Action(ActionType.CLEAR_ALL, null);
        send(action);
    }

    public void sendSyncAction(ActionSync actionSync) throws IOException {
        Action action = new Action(ActionType.SYNC, actionSync);
        send(action);
    }

    private void send(Action action) throws IOException {
        if (mClient == null) {
            return;
        }
        String json = mGson.toJson(action);
        mClient.emit(json);
    }

    public void addOnConnectionEventListener(ConnectionEventListener listener) {
        mConnectionEventListeners.add(listener);
    }

    public void addOnSyncEventListener(SyncEventListener listener) {
        mSyncEventListeners.add(listener);
    }

    private void fireOnDisconnectedAction() {
        for (int i = 0; i < mConnectionEventListeners.size(); i++) {
            mConnectionEventListeners.get(i).onDisconnected();
        }
    }

    private void fireOnConnectedAction() {
        for (int i = 0; i < mConnectionEventListeners.size(); i++) {
            mConnectionEventListeners.get(i).onConnected();
        }
    }

    private void fireOnBusyAction() {
        for (int i = 0; i < mConnectionEventListeners.size(); i++) {
            mConnectionEventListeners.get(i).onServerBusy();
        }
    }

    public void fireOnSyncAction(ActionSync action) {
        for (int i = 0; i < mSyncEventListeners.size(); i++) {
            mSyncEventListeners.get(i).onDataSync(action);
        }
    }

    private void fireOnSetHost() {
        for (int i = 0; i < mConnectionEventListeners.size(); i++) {
            mConnectionEventListeners.get(i).setHost();
        }
    }
}
