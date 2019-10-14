package com.example.ijkplayerdemo.holder;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.ijkplayerdemo.R;
import com.example.ijkplayerdemo.widgets.ColorImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ColorViewHolder extends RecyclerView.ViewHolder {
    public ColorImageView colorButton;

    public ColorViewHolder(View itemView) {
        super(itemView);
        colorButton = itemView.findViewById(R.id.color_button);
    }
}
