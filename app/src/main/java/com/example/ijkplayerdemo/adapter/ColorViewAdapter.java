package com.example.ijkplayerdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ijkplayerdemo.R;
import com.example.ijkplayerdemo.holder.ColorViewHolder;
import com.example.ijkplayerdemo.widgets.ColorImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mColorList;
    private Context mContext;
    private Map<ColorImageView, String> mColorImageViewMap;
    private String mColor = "#ffffff";


    public ColorViewAdapter(List<String> colorList) {
        mColorList = colorList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_color_item, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final ColorViewHolder holder = (ColorViewHolder) viewHolder;
        // 资源文件不是透明的地方都会改变
        holder.colorButton.setColorFilter(Color.parseColor(mColorList.get(i)));
        if (mColorImageViewMap == null) {
            mColorImageViewMap = new HashMap<>();
        }
        mColorImageViewMap.put(holder.colorButton, mColorList.get(i));
        for (Map.Entry<ColorImageView, String> map : mColorImageViewMap.entrySet()) {
            Log.d("987656","8");
            if (map.getValue().equals("#ffffff")) {
                Log.d("987656","9");
                map.getKey().setFlag(true);
                map.getKey().invalidate();
            }
        }
        holder.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ColorImageView imageView: mColorImageViewMap.keySet()) {
                    imageView.setFlag(false);
                    imageView.invalidate();
                }
                holder.colorButton.setFlag(true);
                holder.colorButton.invalidate();
                for (Map.Entry<ColorImageView, String> map : mColorImageViewMap.entrySet()) {
                    if (map.getKey() == holder.colorButton) {
                        mColor = map.getValue();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColorList.size();
    }

    public String getColor() {
        if (mColor != null) {
            return mColor;
        }
        Log.d("987656", mColor+"");
        return null;
    }

}
