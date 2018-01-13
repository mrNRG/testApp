package com.ivoronyuk.test.socketapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class NetworkUtil {

    // TODO: 1/12/18 need to add  CONNECTIVITY_CHANGE listener
    public static String checkWiFiConnection(Context context) {

        boolean WIFI = false;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                    WIFI = true;
                }
            }
        }

        if (WIFI) {
            return String.format(Locale.getDefault(), context.getString(R.string.msg_device_ip), getDeviceIPWiFiData(context));
        } else {
            return context.getString(R.string.msg_check_wifi);
        }
    }

    @SuppressWarnings("deprecation")
    public static String getDeviceIPWiFiData(Context context) {

        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        String ip = null;
        if (wm != null) {
            ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }

        return ip;
    }
}
