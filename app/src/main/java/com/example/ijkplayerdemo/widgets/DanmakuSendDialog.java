package com.example.ijkplayerdemo.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ijkplayerdemo.R;
import com.example.ijkplayerdemo.adapter.ColorViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DanmakuSendDialog extends Dialog implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener, View.OnClickListener {
    private InputMethodManager inputMethodManager;
    private ImageView mGraffitiIv;
    private EditText mDanmakuEdit;
    private TextView mDanmakuSend;
    private LinearLayout mPaletteLayout;
    private RecyclerView mRecyclerView;
    private ColorViewAdapter mAdapter;
    private List<String> mColorList = new ArrayList<>();
    private DialogListener mListener;

    public DanmakuSendDialog(Context context, DialogListener listener) {
        this(context, R.style.dialog_send_danmaku, R.layout.dialog_danmakuedit, Gravity.BOTTOM);
        mListener = listener;
    }

    public DanmakuSendDialog(Context context, int theme, int resLayout, int gravity) {
        super(context, theme);
        Window window = getWindow();
        if (window != null) {
            setContentView(resLayout);
            mGraffitiIv = findViewById(R.id.graffiti_image);
            mDanmakuEdit = findViewById(R.id.danmaku_editText);
            mDanmakuSend = findViewById(R.id.danmaku_send);
            mPaletteLayout = findViewById(R.id.palette_layout);
            mRecyclerView = findViewById(R.id.color_recyclerView);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.getDecorView().setPadding(0, 0, 0, 0);
            layoutParams.gravity = gravity;
            window.setAttributes(layoutParams);
        }
        // 初始化软键盘
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mColorList = Arrays.asList("#FF5252", "#448AFF", "#0DBE94", "#EEFF41", "#ffffff", "#FFAB40", "#FF4081");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ColorViewAdapter(mColorList);
        mRecyclerView.setAdapter(mAdapter);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnShowListener(this);
        setOnCancelListener(this);
        setOnDismissListener(this);
        mDanmakuEdit.setOnClickListener(this);
        mDanmakuSend.setOnClickListener(this);
        mGraffitiIv.setOnClickListener(this);
    }

    @Override
    public void onCancel(final DialogInterface dialog) {
        if (mDanmakuEdit != null) {
            if (inputMethodManager != null) {
               mDanmakuEdit.post(new Runnable() {
                   @Override
                   public void run() {
                       inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                       try {
                           Thread.sleep(60);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               });
            }
            dialog.dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (mDanmakuEdit != null) {
            if (inputMethodManager != null) {
                mDanmakuEdit.post(new Runnable() {
                    @Override
                    public void run() {
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void setShowInputState() {
        mGraffitiIv.setSelected(false);
    }

    public void showPaletteLayout(Boolean isShow) {
        if (isShow) {
            mPaletteLayout.setVisibility(View.VISIBLE);
        } else {
            mPaletteLayout.setVisibility(View.GONE);
        }
    }

    public void setPaletteLayoutHeight(int keyboardHeight) {
        ViewGroup.LayoutParams layoutParams = mPaletteLayout.getLayoutParams();
        layoutParams.height = keyboardHeight;
        mPaletteLayout.setLayoutParams(layoutParams);
    }

    public void setText(String text) {
        mDanmakuEdit.setText(text);
    }
    public void cancel() {
        onCancel(this);
        mDanmakuEdit.setText("");
    }

    public String getText() {
        return mDanmakuEdit.getText().toString();
    }

    public String getColor() {
        return mAdapter.getColor();
    }

    @Override
    public void onClick(View v) {
        mListener.onClick(v);
    }

    public interface DialogListener {
        void onClick(View view);
    }
}
