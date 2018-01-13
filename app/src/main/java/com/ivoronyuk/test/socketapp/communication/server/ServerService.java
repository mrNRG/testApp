package com.ivoronyuk.test.socketapp.communication.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.ivoronyuk.test.socketapp.MainActivity;
import com.ivoronyuk.test.socketapp.R;
import com.ivoronyuk.test.socketapp.communication.Action;
import com.ivoronyuk.test.socketapp.communication.ActionType;
import com.ivoronyuk.test.socketapp.communication.SocketListener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class ServerService extends Service {

    private static final String TAG = ServerService.class.getSimpleName();

    private static final int PORT = 9001;
    private static final int MAX_CONNECTIONS = 10;
    // Foreground notification id
    private static final int NOTIFICATION_ID = 1;
    private static HashSet<String> ipAddresses = new HashSet<>();
    private static HashSet<PrintWriter> writers = new HashSet<>();
    private final IBinder serviceBinder = new RunServiceBinder();
    private ServerSocket serverSocket;
    private Gson mGson;
    private List<SocketListener> mSocketListeners = new ArrayList<>();
    private boolean isAwake;

    public boolean isAwake() {
        return isAwake;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Creating server service");
                try {
                    serverSocket = new ServerSocket(PORT);
                    mGson = new Gson();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting server service");
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Binding server service");
        return serviceBinder;
    }

    /**
     * Place the service into the foreground
     */
    public void foreground() {
        startForeground(NOTIFICATION_ID, createNotification());
    }

    /**
     * Return the service to the background
     */
    public void background() {
        stopForeground(true);
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */

    @SuppressWarnings("deprecation")
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.msg_background_run))
                .setContentText(getString(R.string.msg_tap_to_return))
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

    public void start() {
        if (isAwake) {
            Log.e(TAG, "Server is already running.");
            return;
        }
        isAwake = true;
        new SocketServerThread().start();
    }

    public void stop() throws IOException {
        isAwake = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        String s = "Server shutdown.";
        sendToLog(s);

//        ipAddresses.clear();
//        writers.clear();
//        mSocketListeners.clear();
    }

    private void filterMessages(String input) {
        for (int i = 0; i < mSocketListeners.size(); i++) {
            mSocketListeners.get(i).onMessageReceived(input);
        }
    }

    public void sendToAll(String msg) {
        for (PrintWriter writer : writers) {
            send(writer, msg);
        }
    }

    private void send(PrintWriter writer, String msg) {
        writer.println(msg);
    }

    private void sendToLog(String input) {
        Log.d(TAG, input);
    }

    public void addSocketListener(SocketListener listener) {
        mSocketListeners.add(listener);
    }

    public class RunServiceBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }

    private class SocketServerThread extends Thread {
        @Override
        public void run() {
            String s = "Server is running.";
            sendToLog(s);
            try {
                while (!isInterrupted()) {
                    if (serverSocket != null) {
                        new Handler(serverSocket.accept()).start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ServerService.this.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private class Handler extends Thread {
        private String ipAddress;
        private java.net.Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(java.net.Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()), true);

                while (socket.isConnected()) {
                    ipAddress = socket.getRemoteSocketAddress().toString();
                    synchronized (ipAddresses) {
                        if (!ipAddresses.contains(ipAddress)) {
                            ipAddresses.add(ipAddress);
                            break;
                        }
                    }
                }

                String s = String.format(Locale.getDefault(), "New connection IP: %s. \nTotal count: %d.", ipAddress, ipAddresses.size());

                sendToLog(s);

                writers.add(out);

                if (ipAddresses.size() > MAX_CONNECTIONS) {
                    String s1 = "ServerService is busy! Max connection count is: " + MAX_CONNECTIONS;
                    Log.d(TAG, s1);
                    System.out.println(s1);
                    sendToLog(s1);

                    Action busy = new Action(ActionType.BUSY, null);
                    String json = mGson.toJson(busy);
                    send(out, json);
                    filterMessages(json);
                    return;
                } else {
                    Action connected = new Action(ActionType.CONNECTED, null);
                    String json = mGson.toJson(connected);
                    send(out, json);
                    filterMessages(json);
                }

                while (isAwake) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    String msg = String.format(Locale.getDefault(), "MESSAGE %s: %s", ipAddress, input);
                    sendToLog(msg);

                    filterMessages(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // This client is going down!  Remove its ipAddress and its print
                // writer from the sets, and close its socket.
                String s = String.format(Locale.getDefault(), "The client %s disconnected.", ipAddress);
                sendToLog(s);

                if (ipAddress != null) {
                    ipAddresses.remove(ipAddress);
                }
                if (out != null) {
                    writers.remove(out);
                    out.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
