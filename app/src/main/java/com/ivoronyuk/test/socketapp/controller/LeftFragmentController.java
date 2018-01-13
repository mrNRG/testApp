package com.ivoronyuk.test.socketapp.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivoronyuk.test.socketapp.MainActivity;
import com.ivoronyuk.test.socketapp.R;
import com.ivoronyuk.test.socketapp.model.Column;
import com.ivoronyuk.test.socketapp.model.ColumnType;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class LeftFragmentController {

    private EventListener mListener;
    private MainActivity mActivity;
    private TextView colAAA;
    private TextView colBBB;
    private HashMap<ColumnType, List<Column>> map;

    public LeftFragmentController(final MainActivity activity, HashMap<ColumnType, List<Column>> map) {
        this.mActivity = activity;

        map.size();
        init(map);
    }

    public void setOnEventListener(EventListener listener) {
        this.mListener = listener;
    }

    private void init(HashMap<ColumnType, List<Column>> map) {
        LinearLayout columnContainer = mActivity.findViewById(R.id.column_container);

        for (final Map.Entry<ColumnType, List<Column>> entry : map.entrySet()) {
            View view = LayoutInflater.from(mActivity)
                    .inflate(R.layout.view_column_item, columnContainer, false);

            final TextView text = view.findViewById(R.id.tv_column);

            switch (entry.getKey()) {
                case AAA:
                    colAAA = text;
                    break;
                case BBB:
                    colBBB = text;
                    break;
            }
            text.setText(String.format(Locale.getDefault(),
                    mActivity.getString(R.string.title_column_name),
                    entry.getValue().size(),
                    entry.getKey().equals(ColumnType.AAA) ? mActivity.getString(R.string.btn_aaa) : mActivity.getString(R.string.btn_bbb)));

            ImageButton btnRemove = view.findViewById(R.id.btn_remove);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onColumnRemove(entry.getKey());
                    }
                }
            });
            columnContainer.addView(view);
        }

        Button btnClear = mActivity.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClearAll();
                }
            }
        });
    }

    public void setAAAColumnCount(int count) {
        colAAA.setText(String.format(Locale.getDefault(),
                mActivity.getString(R.string.title_column_name),
                count,
                mActivity.getString(R.string.btn_aaa)));
    }

    public void setBBBColumnCount(int count) {
        colBBB.setText(String.format(Locale.getDefault(),
                mActivity.getString(R.string.title_column_name),
                count,
                mActivity.getString(R.string.btn_bbb)));
    }

    public interface EventListener {
        void onColumnRemove(ColumnType type);

        void onClearAll();
    }
}
