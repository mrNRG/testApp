package com.ivoronyuk.test.socketapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ivoronyuk.test.socketapp.communication.ActionSync;
import com.ivoronyuk.test.socketapp.communication.client.Communication;
import com.ivoronyuk.test.socketapp.communication.client.ConnectionEventListener;
import com.ivoronyuk.test.socketapp.communication.SyncEventListener;
import com.ivoronyuk.test.socketapp.communication.server.ServerService;
import com.ivoronyuk.test.socketapp.controller.LeftFragmentController;
import com.ivoronyuk.test.socketapp.controller.RightFragmentController;
import com.ivoronyuk.test.socketapp.model.Column;
import com.ivoronyuk.test.socketapp.model.ColumnType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ServerService mServerService;
    private boolean serviceBound;

    private String connectedIP;
    private String deviceIP;

    private Communication communication;

    private LeftFragmentController leftFragmentController;
    private RightFragmentController rightFragmentController;

    private boolean isHost;
    /**
     * Callback for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service bound");
            }
            ServerService.RunServiceBinder binder = (ServerService.RunServiceBinder) service;
            mServerService = binder.getService();
            serviceBound = true;
            // Ensure the service is not in the foreground when bound
            mServerService.background();

            if (serviceBound && !mServerService.isAwake()) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Starting server");
                }
                communication.addServer(mServerService);
                communication.startServer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        initHistory();
        initControllers();
        initCommunication();
    }

    private void initControllers() {
        initLeftFragmentController();
        initRightFragmentController();
    }

    private void initLeftFragmentController() {
        leftFragmentController = new LeftFragmentController(MainActivity.this, App.getHistory().getMap());
        leftFragmentController.setOnEventListener(new LeftFragmentController.EventListener() {
            @Override
            public void onColumnRemove(ColumnType type) {
                try {
                    communication.sendColumnRemoveAction(type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClearAll() {
                try {
                    communication.sendAllClearAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initRightFragmentController() {
        rightFragmentController = new RightFragmentController(MainActivity.this);
        rightFragmentController.setOnEventListener(new RightFragmentController.EventListener() {
            @Override
            public void addColumn(ColumnType type) {
                try {
                    communication.sendColumnAddAction(type);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connect(String ipAddress) {
                connectedIP = ipAddress;
                connectToServer(ipAddress);
            }
        });
    }

    private void initCommunication() {
        communication = new Communication();
        communication.addOnConnectionEventListener(new ConnectionEventListener() {
            @Override
            public void onConnected() {
                if (!isHost) {
                    rightFragmentController.onConnected(connectedIP);
                    try {
                        communication.stopServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDisconnected() {
                if (!isHost) {
                    connectedIP = "";
                    rightFragmentController.onDisconnected();
                    communication.startServer();
                }
            }

            @Override
            public void onServerBusy() {
                rightFragmentController.onServerBusy();
            }

            @Override
            public void setHost() {
                rightFragmentController.setHost();
                connectToServer(deviceIP);
            }
        });

        communication.addOnSyncEventListener(new SyncEventListener() {
            @Override
            public void onDataSync(ActionSync actionSync) {
                App.setHistory(actionSync.getHistory());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leftFragmentController.setAAAColumnCount(App.getHistory().getColumnsByType(ColumnType.AAA).size());
                        leftFragmentController.setBBBColumnCount(App.getHistory().getColumnsByType(ColumnType.BBB).size());
                    }
                });
            }
        });
    }

    private void connectToServer(String ipAddress) {
        if (!isHost) {
            if (ipAddress.equals(deviceIP)) {
                isHost = true;
            }
            communication.connect(ipAddress);
        }
    }

    private void initHistory() {
        HashMap<ColumnType, List<Column>> map = new HashMap<>();
        map.put(ColumnType.BBB, new ArrayList<Column>());
        map.put(ColumnType.AAA, new ArrayList<Column>());

        App.getHistory().setMap(map);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service");
        }
        Intent i = new Intent(MainActivity.this, ServerService.class);
        this.startService(i);
        this.bindService(i, mConnection, 0);
    }

    protected void onStop() {
        super.onStop();

        if (serviceBound) {
            // If a server is active, foreground the service, otherwise kill the service
            if (mServerService.isAwake()) {
                mServerService.foreground();
            } else {
                stopService(new Intent(this, ServerService.class));
            }
            // Unbind the service
            unbindService(mConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceIP = NetworkUtil.getDeviceIPWiFiData(MainActivity.this);

        if (rightFragmentController != null) {
            rightFragmentController.checkWiFiConnection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServerService != null) {
            mServerService.background();
            stopService(new Intent(this, ServerService.class));
            if (serviceBound) {
                // Unbind the service
                unbindService(mConnection);
                serviceBound = false;
            }
        }
        try {
            if (communication != null) {
                communication.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
