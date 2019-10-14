package com.example.ijkplayerdemo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ijkplayerdemo.media.IjkPlayerView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity  {
    private IjkPlayerView ijkPlayerView;
    private InputStream stream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ijkPlayerView = findViewById(R.id.ijkplayer);
        try {
            stream = getAssets().open("custom.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ijkPlayerView.init()
                .createParser(stream)
                .setVideoPath("http://flv2.bn.netease.com/videolib3/1611/28/GbgsL3639/SD/movie_index.m3u8");

    }

    @Override
    public void onBackPressed() {
        if (ijkPlayerView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkPlayerView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ijkPlayerView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkPlayerView.onDestroy();
    }

}
