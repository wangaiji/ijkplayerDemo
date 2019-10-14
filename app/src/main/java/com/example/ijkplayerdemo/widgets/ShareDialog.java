package com.example.ijkplayerdemo.widgets;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ijkplayerdemo.R;

public class ShareDialog extends DialogFragment {
    private ImageView mScreenshot;
    private Bitmap mBitmap;
    private TextView mCancelText;
    private TextView mSaveText;
    private OnDialogSaveListener mSaveListener;
    private OnDialogDismissListener mDismissListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 去掉标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        // 背景变暗
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置dialog动画
        window.setWindowAnimations(R.style.DialogAnimation);
        View view = inflater.inflate(R.layout.dialog_share, container);
        mScreenshot = view.findViewById(R.id.screenshot_image);
        ViewGroup.LayoutParams layoutParams = mScreenshot.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels * 7 / 10;
        layoutParams.height = getResources().getDisplayMetrics().heightPixels * 7 / 10;
        mScreenshot.setLayoutParams(layoutParams);
        if (mBitmap != null) {
            mScreenshot.setImageBitmap(mBitmap);
        }
        mSaveText = view.findViewById(R.id.save_tv);
        mSaveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSaveListener != null) {
                    mSaveListener.onSave(mBitmap);
                }
                dismiss();
            }
        });
        mCancelText = view.findViewById(R.id.cancel_tv);
        mCancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dismiss();
            }
        });
        return view;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    public void setScreenImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void setmSaveListener(OnDialogSaveListener onSaveListener) {
        mSaveListener = onSaveListener;
    }

    public void setmDismissListener(OnDialogDismissListener onDismissListener) {
        mDismissListener = onDismissListener;
    }

    public interface OnDialogSaveListener {
        void onSave(Bitmap bitmap);
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }
}
