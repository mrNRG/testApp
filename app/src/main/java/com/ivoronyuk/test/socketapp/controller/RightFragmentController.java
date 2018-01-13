package com.ivoronyuk.test.socketapp.controller;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivoronyuk.test.socketapp.MainActivity;
import com.ivoronyuk.test.socketapp.NetworkUtil;
import com.ivoronyuk.test.socketapp.R;
import com.ivoronyuk.test.socketapp.model.ColumnType;

import java.util.Locale;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class RightFragmentController {

    private final String DEFAULT_IP;
    private EventListener mListener;
    private MainActivity mActivity;
    private LinearLayout connectionContainer;
    private EditText etIpInput;
    private TextView tvConnectedTo;
    private TextView tvDeviceIP;
    private View.OnClickListener onAddColumnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_AAA:
                    if (mListener != null) {
                        mListener.addColumn(ColumnType.AAA);
                    }
                    break;
                case R.id.btn_BBB:
                    if (mListener != null) {
                        mListener.addColumn(ColumnType.BBB);
                    }
                    break;
            }
        }
    };

    public RightFragmentController(final MainActivity activity) {
        this.mActivity = activity;
        this.DEFAULT_IP = NetworkUtil.getDeviceIPWiFiData(mActivity);

        init();
    }

    public void setOnEventListener(EventListener listener) {
        this.mListener = listener;
    }

    private void init() {
        connectionContainer = mActivity.findViewById(R.id.connection_container);
        etIpInput = mActivity.findViewById(R.id.et_ip_address);
        etIpInput.setText(DEFAULT_IP);

        tvConnectedTo = mActivity.findViewById(R.id.tv_connected_to);
        tvDeviceIP = mActivity.findViewById(R.id.tv_device_ip);

        Button btnAAA = mActivity.findViewById(R.id.btn_AAA);
        Button btnBBB = mActivity.findViewById(R.id.btn_BBB);
        Button btnConnect = mActivity.findViewById(R.id.btn_connect);

        btnAAA.setOnClickListener(onAddColumnListener);
        btnBBB.setOnClickListener(onAddColumnListener);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etIpInput.getText().toString().trim();
                if (input.isEmpty()) {
                    return;
                }

                if (mListener != null) {
                    mListener.connect(input);
                }
            }
        });

        ImageView historyView = mActivity.findViewById(R.id.history_view);
        historyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, R.string.msg_feature_will_be_later, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onConnected(final String ipAddress) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String s = String.format(Locale.getDefault(), mActivity.getString(R.string.msg_connected_to), ipAddress);
                tvConnectedTo.setText(s);
                tvConnectedTo.setVisibility(View.VISIBLE);

                etIpInput.setText("");
                connectionContainer.setVisibility(View.GONE);
            }
        });
    }

    public void onDisconnected() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etIpInput.setText(DEFAULT_IP);
                connectionContainer.setVisibility(View.VISIBLE);

                tvConnectedTo.setText("");
                tvConnectedTo.setVisibility(View.GONE);
            }
        });
    }

    public void onServerBusy() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, R.string.msg_server_busy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setHost() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvConnectedTo.setText(R.string.title_host);
                tvConnectedTo.setVisibility(View.VISIBLE);

                etIpInput.setText("");
                connectionContainer.setVisibility(View.GONE);
            }
        });
    }

    private void setDeviceIp(String ipAddress) {
        tvDeviceIP.setText(ipAddress);
    }

    public void checkWiFiConnection() {
        setDeviceIp(NetworkUtil.checkWiFiConnection(mActivity));
    }

    public interface EventListener {
        void addColumn(ColumnType type);

        void connect(String ipAddress);
    }
}