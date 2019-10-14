package com.example.ijkplayerdemo.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class KeyboardLayout extends FrameLayout {
    private KeyboardLayoutListener mKeyboardListener;
    // 输入法是否激活
    private Boolean mIsKeyboardActive = false;
    // 输入法高度
    private int mKeyboardHeight = 0;

    public KeyboardLayout(Context context) {
        this(context, null);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
    }

    public void setKeyboardListener(KeyboardLayoutListener listener) {
        mKeyboardListener = listener;
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        int mScreenHeight = 0;

        private int getScreenHeight() {
            if (mScreenHeight > 0) {
                return mScreenHeight;
            }
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
            mScreenHeight = metrics.heightPixels;
            return mScreenHeight;
        }

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            ((Activity)getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = getScreenHeight();
            int keyboardHeight = screenHeight - rect.bottom - 990;
            Log.d("738276", keyboardHeight+" "+rect.bottom);
            mIsKeyboardActive = false;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsKeyboardActive = true;
                mKeyboardHeight = keyboardHeight;
            }
            Log.d("738276",mIsKeyboardActive + "");
            if (mKeyboardListener != null) {
                mKeyboardListener.onKeyboardStateChanged(mIsKeyboardActive, keyboardHeight);
            }
        }
    }

    public interface KeyboardLayoutListener {
        void onKeyboardStateChanged(Boolean isAlive, int keyboardHeight);
    }

}
