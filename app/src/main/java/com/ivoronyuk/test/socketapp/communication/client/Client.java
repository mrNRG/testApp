package com.ivoronyuk.test.socketapp.communication.client;

import com.google.gson.Gson;
import com.ivoronyuk.test.socketapp.communication.Action;
import com.ivoronyuk.test.socketapp.communication.ActionType;
import com.ivoronyuk.test.socketapp.communication.SocketListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class Client {
    private static final int PORT = 9001;

    private Socket mSocket;
    private boolean isConnected;

    private List<SocketListener> mSocketListeners = new ArrayList<>();
    private BlockingQueue<String> mQueue = new LinkedBlockingQueue<>();

    private BufferedReader in;
    private PrintWriter out;

    public void connect(final String ipAddress) {
        new Thread() {
            @Override
            public void run() {
                try {
                    isConnected = true;

                    mSocket = new Socket(ipAddress, PORT);

                    in = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));

                    out = new PrintWriter(mSocket.getOutputStream(), true);


                    new Thread() {
                        @Override
                        public void run() {
                            while (isConnected) {
                                try {
                                    String message = mQueue.take();
                                    out.println(message);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();

                    new Thread() {
                        @Override
                        public void run() {
                            while (isConnected) {
                                try {
                                    String line = in.readLine();
                                    if (line == null) {
                                        disconnect();
                                    }
                                    fireOnMessageReceived(line);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // FIXME: 1/12/18 need more more elegant method
    public void disconnect() throws IOException {
        isConnected = false;

        Action action = new Action<>(ActionType.DISCONNECTED, null);
        Gson gson = new Gson();

        fireOnMessageReceived(gson.toJson(action));


        if (mSocket != null) {
            mSocket.close();
        }
        mSocketListeners.clear();
    }

    public void emit(String message) throws IOException {
        mQueue.add(message);
    }

    private void fireOnMessageReceived(String inputLine) {
        for (int i = 0; i < mSocketListeners.size(); i++) {
            mSocketListeners.get(i).onMessageReceived(inputLine);
        }
    }

    public void addSocketListener(SocketListener listener) {
        mSocketListeners.add(listener);
    }
}
