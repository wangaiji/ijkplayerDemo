package com.example.ijkplayerdemo.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ijkplayerdemo.R;
import com.example.ijkplayerdemo.danmaku.AcFunDanmakuParser;
import com.example.ijkplayerdemo.danmaku.BiliDanmakuParser;
import com.example.ijkplayerdemo.widgets.DanmakuSendDialog;
import com.example.ijkplayerdemo.widgets.KeyboardLayout;
import com.example.ijkplayerdemo.widgets.MarqueeTextView;
import com.example.ijkplayerdemo.widgets.ShareDialog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.example.ijkplayerdemo.utils.StringUtils.generateTime;
import static com.example.ijkplayerdemo.utils.StringUtils.getCurrentTime;

public class IjkPlayerView extends FrameLayout implements View.OnClickListener {
    private static final int MAX_VIDEO_SEEK = 1000;
    private static final int MSG_UPDATE_SEEK = 100000;
    private static final int DEFAULT_HIDE_TIME = 10000;
    private static final int PLAY_STATUS = 888;
    private static final int PAUSE_STATUS = 889;
    // 加载状态
    private boolean mIsReady = false;
    // 是否是第一次播放状态
    private boolean mIsNeverPlay = true;
    // 缓冲状态
    private boolean mIsBufferingStart = false;
    // 硬件加速
    private boolean mIsRenderingStart = false;
    // 表示是否拖拽进度条
    private boolean mIsSeeking = false;
    // 表示是否显示控制栏
    private boolean mIsShowBar = true;
    // 表示是否全屏
    private boolean mIsFullScreen = false;
    // 表示是否手机设置屏幕锁屏
    private boolean mIsScreenLocked = false;
    private boolean mIsResume = false;
    // 表示锁屏按钮
    private boolean mIsForbidTouch = false;

    // 表示当前视频播放位置
    private int mCurPosition = -1;
    private int mDanmakuPosition = -1;
    // 表示要跳转到的位置
    private int mTargetPosition = -1;
    // 当前亮度
    private float mCurBrightness = -1;
    // 当前音量
    private int mCurVolume = -1;
    // 表示视频初始高度
    private int mInitHeight;
    private int mWidthPixels;

    private int mKeyboardHeight = 400;
    // 视频状态
    private int mPlayStatus;

    private IDanmakuView mDanmakuView;
    private DanmakuContext mContext;
    private BaseCacheStuffer.Proxy mCacheStufferAdapter;

    private DanmakuSendDialog mSendDialog;
    // 解析器对象
    private BaseDanmakuParser mParser;
    private AppCompatActivity mAttachActivity;
    private FrameLayout mVideoLayout;
    private IjkVideoView mVideoView;
    private ImageView mPlayStart;
    private ProgressBar mLoadPb;
    private TextView mReloadText;
    private ConstraintLayout mTopLayout;
    private ImageView mPlayerBack;
    private MarqueeTextView mPlayerTitle;
    private ProgressBar mBatteryPb;
    private TextView mSystemTime;
    private ImageView mScreenshot;
    private FrameLayout mWindowLayout;
    private ImageView mWindowBack;
    private SeekBar mFullSeek;
    private ConstraintLayout mBottomLayout;
    private ImageView mPlayButton;
    private TextView mStartTime;
    private TextView mEndTime;
    private SeekBar mPlaySeek;
    private ImageView mDanmakuEdit;
    private ImageView mDanmakuControl;
    private TextView mMediaQuality;
    private ImageView mFullScreen;
    private ImageView mPlayBackground;
    private FrameLayout mTouchLayout;
    private TextView mVolumeGesture;
    private TextView mBrightnessGesture;
    private TextView mProgressSlide;
    private ImageView mPlayerLock;
    // 监听布局
    private KeyboardLayout mKeyboardLayout;

    // 最大音量
    private int mMaxVolume;
    // 系统的音量管理者
    private AudioManager mAudioManager;
    // 屏幕旋转监听
    private OrientationEventListener mOrientationListener;
    // 手势控制
    private GestureDetector mGestureDetector;

    private DanmakuSendDialog.DialogListener mListener = new DanmakuSendDialog.DialogListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.danmaku_send) {
                addDanmaku(false, mSendDialog.getText());
                mSendDialog.cancel();
            }

            if (view.getId() == R.id.danmaku_editText) {
                mSendDialog.setShowInputState();
                mSendDialog.showPaletteLayout(false);
            }

            if (view.getId() == R.id.graffiti_image) {
                view.setSelected(!view.isSelected());
                if (view.isSelected()) {
                    Log.d("738276", "1");
                    // 不改变布局
                    mAttachActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    InputMethodManager inputMethodManager = (InputMethodManager) mAttachActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSendDialog.showPaletteLayout(true);
                } else {
                    Log.d("738276", "2");
                    mAttachActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    mSendDialog.showPaletteLayout(false);
                    InputMethodManager inputMethodManager = (InputMethodManager) mAttachActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_SEEK) {
                setProgress();
                if (!mIsSeeking) {
                    msg = obtainMessage(MSG_UPDATE_SEEK);
                    sendMessageDelayed(msg, 200);
                }
            }
        }
    };

    private Runnable mHideBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideAllView(false);
        }
    };

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mIsScreenLocked = true;
            }
        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            long duration = mVideoView.getDuration();
            mTargetPosition = (int) (duration * progress) / MAX_VIDEO_SEEK;
            // 更新时间
            mStartTime.setText(generateTime(mTargetPosition));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mIsSeeking = true;
            mHandler.removeMessages(MSG_UPDATE_SEEK);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mIsSeeking = false;
           // seekTo(mTargetPosition);
            Log.d("4747474", mVideoView.isPlaying() + " resume" + mTargetPosition + "");
            seekTo(mTargetPosition);
            mTargetPosition = -1;
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK,1000);
        }
    };

    // 截图对话框
    private ShareDialog mShareDialog;
    private ShareDialog.OnDialogDismissListener mDismissListener = new ShareDialog.OnDialogDismissListener() {
        @Override
        public void onDismiss() {
            recoverEditVideo();
        }
    };
    private ShareDialog.OnDialogSaveListener mSaveListener = new ShareDialog.OnDialogSaveListener() {
        @Override
        public void onSave(Bitmap bitmap) {
            MediaStore.Images.Media.insertImage(mAttachActivity.getContentResolver(),
                    bitmap, System.currentTimeMillis() + ".jpg", null);
            Toast.makeText(mAttachActivity, "保存成功", Toast.LENGTH_SHORT).show();

        }
    };
    // 手势监听
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        // 是否是按下的标识
        private boolean isDownTouch;
        // 是否为亮度控制
        private boolean isBrightness;
        // 是否为横向滑动
        private boolean isLandscape;


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mIsForbidTouch) {
                float mOldX = e1.getX();
                float mOldY = e1.getY();
                float deltaX = mOldX - e2.getX();
                float deltaY = mOldY - e2.getY();
                if (isDownTouch) {
                    isLandscape = Math.abs(deltaX) >= Math.abs(deltaY);
                    isBrightness = mOldX < getResources().getDisplayMetrics().widthPixels * 0.5f;
                    isDownTouch = false;
                }

                if (isLandscape) {
                    setProgressSlide(-deltaX / mVideoView.getWidth());
                } else {
                    float percent = deltaY / mVideoView.getHeight();
                    if (isBrightness) {
                        setBrightnessSlide(percent);
                    } else {
                        setVolumeSlide(percent);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            isDownTouch = true;
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            toggleControlBar();
            return true;
        }
    };

    // 触摸监听
    private OnTouchListener mTouchListener = new OnTouchListener() {
        private static final int NORMAL = 1;
        // 触摸模式
        private int mode;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mode = NORMAL;
                    mHandler.removeCallbacks(mHideBarRunnable);
                    break;
            }
            if (mode == NORMAL) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endGesture();
                }
            }
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int status, int extra) {
            switchStatus(status);
            return true;
        }
    };

    public IjkPlayerView(Context context) {
        this(context, null);
    }

    public IjkPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        if (context instanceof AppCompatActivity) {
            mAttachActivity = (AppCompatActivity) context;
        } else {
            throw new IllegalArgumentException("Context must be AppCompatActivity");
        }
        View.inflate(context, R.layout.layout_player_view, this);
        mVideoLayout = findViewById(R.id.video_layout);
        mVideoView = findViewById(R.id.ijkvideo);
        mDanmakuView = findViewById(R.id.video_danmaku);
        mPlayStart = findViewById(R.id.play_start);
        mLoadPb = findViewById(R.id.load_pb);
        mReloadText = findViewById(R.id.reload_tv);
        mTopLayout = findViewById(R.id.player_top_bar);
        mPlayerBack = findViewById(R.id.player_back_button);
        mPlayerTitle = findViewById(R.id.player_title);
        mBatteryPb = findViewById(R.id.battery_pb);
        mSystemTime = findViewById(R.id.system_time);
        mScreenshot = findViewById(R.id.screenshot_button);
        mWindowLayout = findViewById(R.id.window_top_bar);
        mWindowBack = findViewById(R.id.window_back);
        mFullSeek = findViewById(R.id.full_player_seek);
        mBottomLayout = findViewById(R.id.player_bottom_bar);
        mPlayButton = findViewById(R.id.play_button);
        mStartTime = findViewById(R.id.start_time);
        mEndTime = findViewById(R.id.end_time);
        mPlaySeek = findViewById(R.id.player_seek);
        mDanmakuEdit = findViewById(R.id.danmaku_edit);
        mDanmakuControl = findViewById(R.id.danmaku_control_btn);
        mMediaQuality = findViewById(R.id.text_media_quality);
        mFullScreen = findViewById(R.id.full_screen_button);
        mPlayBackground = findViewById(R.id.player_background);
        mTouchLayout = findViewById(R.id.touch_gesture_layout);
        mVolumeGesture = findViewById(R.id.gesture_volume);
        mBrightnessGesture = findViewById(R.id.gesture_brightness);
        mProgressSlide = findViewById(R.id.progress_slide);
        mPlayerLock = findViewById(R.id.player_lock);
        mKeyboardLayout = findViewById(R.id.keyboard_layout);


        mPlaySeek.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mFullSeek.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPlayStart.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mPlayerLock.setOnClickListener(this);
        mScreenshot.setOnClickListener(this);
        mPlayerBack.setOnClickListener(this);
        mDanmakuControl.setOnClickListener(this);
        mDanmakuEdit.setOnClickListener(this);
        mOrientationListener = new OrientationEventListener(mAttachActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                handleOrientation(orientation);
            }
        };

        // 注册接受广播
        mAttachActivity.registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    public IjkPlayerView init() {
        initPlayer();
        return this;
    }

    private void initPlayer() {
        // 加载so文件
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        // 设置音量
        mAudioManager = (AudioManager) mAttachActivity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 进度
        mPlaySeek.setMax(MAX_VIDEO_SEEK);
        mFullSeek.setMax(MAX_VIDEO_SEEK);
        mVideoLayout.setOnTouchListener(mTouchListener);
        mGestureDetector = new GestureDetector(mAttachActivity, mGestureListener);
        mVideoLayout.setClickable(true);
        mVideoView.setOnInfoListener(mInfoListener);
        mOrientationListener.enable();
    }

    // 初始化弹幕
    private void initDanmakuView() {
        // 设置滚动弹幕最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 4);
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
        mContext = DanmakuContext.create();
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                //    .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);

        if (mDanmakuView != null) {
            if (mParser == null) {
                mParser = new BaseDanmakuParser() {
                    @Override
                    protected IDanmakus parse() {
                        return new Danmakus();
                    }
                };
            }
            mDanmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    if (mVideoView.isPlaying()) {
                        Log.d("5444454","1");
                        mDanmakuView.start();
                    }
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {

                }

                @Override
                public void drawingFinished() {

                }
            });
            mDanmakuView.prepare(mParser, mContext);
            mDanmakuView.showFPS(false);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
    }

    // 创建解析器对象，解析输入流
    public IjkPlayerView createParser(InputStream stream) {
        if (stream == null) {
            return this;
        }
        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_ACFUN);
        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new AcFunDanmakuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        if (mDanmakuView != null) {
            mParser = parser;
        }
        return this;
    }

    private void switchStatus(int status) {
        Log.d("4444", status + "");
        switch (status) {
            // 缓冲开始
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d("3333", "2");
                mIsBufferingStart = true;
                if (!mIsNeverPlay) {
                    mLoadPb.setVisibility(View.VISIBLE);
                }
                break;
            // 缓冲结束
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d("3333", "4");
                mIsBufferingStart = false;
                mLoadPb.setVisibility(View.GONE);
                break;
            // 加载完成
            case MediaPlayerParams.STATE_PREPARED:
                mIsReady = true;
                break;
            // 开始视频渲染
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mIsRenderingStart = true;
                break;
            // 播放中
            case MediaPlayerParams.STATE_PLAYING:
                mLoadPb.setVisibility(View.GONE);
                if (mIsRenderingStart) {
                    resumeDanmaku();
                }
                break;
            // 出错
            case MediaPlayerParams.STATE_ERROR:
                pause();
                break;
            // 结束
            case MediaPlayerParams.STATE_COMPLETED:
                pause();
                break;
        }
    }

    public IjkPlayerView setTitle(String title) {
        mPlayerTitle.setText(title);
        return this;
    }

    // 播放
    public void start() {
        Log.d("3333", "2");
        mPlayStart.setVisibility(View.GONE);
        mPlayBackground.setVisibility(View.GONE);
        mLoadPb.setVisibility(View.VISIBLE);
        mWindowLayout.setVisibility(View.VISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIME);

        if (!mVideoView.isPlaying()) {
            mPlayButton.setSelected(false);
            Log.d("4747474", "2  "+mVideoView.getCurrentPosition()+" " );
            mVideoView.start();
            Log.d("4747474", "1  "+mVideoView.getCurrentPosition()+" " );
            mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
            mIsResume = false;
            if (!mIsNeverPlay) {
                mDanmakuView.show();
            }
        }

        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            initDanmakuView();
            mDanmakuView.hide();
        }
//        // 开启屏幕常亮
//        mAttachActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // 暂停
    public void pause() {
        mPlayButton.setSelected(true);
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        mDanmakuView.pause();
        // 关闭屏幕常亮
        // mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public IjkPlayerView setVideoPath(String uri) {
        mVideoView.setVideoPath(uri);
        return this;
    }

    private void setProgress() {
        if (mVideoView == null || mIsSeeking) {
            return;
        }
        // 视频的当前进度
        long position = mVideoView.getCurrentPosition();
        Log.d("565544",position+"");
        // 视频的总时长
        long duration = mVideoView.getDuration();
        if (duration > 0) {
            long pos = MAX_VIDEO_SEEK * position / duration;
            mPlaySeek.setProgress((int) pos);
            if (mIsFullScreen) {
                mFullSeek.setProgress((int) pos);
            }
        }
        // 获得缓冲的百分比
        int percent = mVideoView.getBufferPercentage();
        mPlaySeek.setSecondaryProgress(percent * 10);
        if (mIsFullScreen) {
            mFullSeek.setSecondaryProgress(percent * 10);
        }

        // 更新时间
        mStartTime.setText(generateTime(position));
        mEndTime.setText(generateTime(duration));
    }

    private void seekTo(int position) {
//        if (!mVideoView.isPlaying() && mIsResume) {
//            Log.d("83276","1");
//            mVideoView.resume();
//            mVideoView.seekTo(position);
//            return;
//        }
        mVideoView.seekTo(position);
        mDanmakuPosition = position;
        Log.d("4747474","  "+position+"");
        if (position == 0) {
            if (mVideoView.isPlaying()) {
                mVideoView.resume();
                mVideoView.setRender(IjkVideoView.RENDER_TEXTURE_VIEW);
                mDanmakuView.seekTo((long) 0);
            }
        }

        if (mVideoView.isPlaying()) {
            Log.d("76765434","1");
            mDanmakuView.seekTo((long) position);
        }
    }

    private void changeScreen() {
        Log.d("6666", "2");
        if (mAttachActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mFullScreen.setSelected(false);
            mIsFullScreen = false;
            mDanmakuView.hide();
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setScreenBar();
        } else {
            mFullScreen.setSelected(true);
            mIsFullScreen = true;
            if (!mDanmakuControl.isSelected()) {
                mDanmakuView.show();
            }
            Log.d("333333", "4");
            //设置全屏
            mAttachActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setScreenBar();
            mBottomLayout.setVisibility(View.VISIBLE);
        }
        changeHeight();
    }

    private void handleOrientation(int orientation) {
        if (mIsFullScreen) {
            if (orientation > 80 && orientation < 100) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            } else if (orientation > 260 && orientation < 280) {
                mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    private void changeHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (mIsFullScreen) {
            // 高度扩展为横向全屏
            layoutParams.height = mWidthPixels;
        } else {
            layoutParams.height = mInitHeight;
        }
        setLayoutParams(layoutParams);
    }

    // 设置控制栏
    private void setScreenBar() {
        if (mIsForbidTouch) {
            mPlayerLock.setVisibility(mIsShowBar ? View.VISIBLE : View.GONE);
        } else {
            mSystemTime.setText(getCurrentTime());
            mWindowLayout.setVisibility(mIsFullScreen ? View.GONE : View.VISIBLE);
            mBottomLayout.setVisibility(mIsShowBar ? View.VISIBLE : View.GONE);
            mTopLayout.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
            mFullSeek.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
            mPlaySeek.setVisibility(mIsFullScreen ? View.GONE : View.VISIBLE);
            mDanmakuEdit.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
            mDanmakuControl.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
            mMediaQuality.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
            mPlayerLock.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
        }
    }

    // 按返回键
    public boolean onBackPressed() {
        if (mIsFullScreen) {
            mIsFullScreen = false;
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mFullScreen.setSelected(false);
            mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setScreenBar();
            changeHeight();
            return true;
        }
        return false;
    }

    // 开关控制栏
    private void toggleControlBar() {
        mIsShowBar = !mIsShowBar;
        if (mIsShowBar) {
            setScreenBar();
            mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIME);
        } else {
            hideAllView(false);
        }
    }

    // 隐藏除视频外的所有视图
    private void hideAllView(boolean IsTouchLock) {
        mIsShowBar = false;
        mTouchLayout.setVisibility(View.GONE);
        mWindowLayout.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mFullSeek.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        if (!IsTouchLock) {
            mPlayerLock.setVisibility(View.GONE);
        }
    }

    // 滑动改变进度条
    private void setProgressSlide(float percent) {
        int position = mVideoView.getCurrentPosition();
        long duration = mVideoView.getDuration();
        // 单次拖拽的最大时间差
        long deltaMax = Math.min(100 * 1000, duration / 2);
        // 滑动时间
        long delta = (long) (percent * deltaMax);
        mTargetPosition = (int) (position + delta);
        if (mTargetPosition > duration) {
            mTargetPosition = (int) duration;
        } else if (mTargetPosition <= 0) {
            mTargetPosition = 0;
        }
        mProgressSlide.setVisibility(View.VISIBLE);
        mProgressSlide.setText(generateTime(mTargetPosition));
    }

    // 滑动改变亮度
    private void setBrightnessSlide(float percent) {
        if (mCurBrightness == -1) {
            mCurBrightness = mAttachActivity.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness < 0.01f) {
                mCurBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams attributes = mAttachActivity.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness > 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness < 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        mTouchLayout.setVisibility(View.VISIBLE);
        mBrightnessGesture.setVisibility(View.VISIBLE);
        mBrightnessGesture.setText((int) Math.ceil(attributes.screenBrightness * 100) + "%");
        mAttachActivity.getWindow().setAttributes(attributes);

    }

    // 滑动改变音量
    private void setVolumeSlide(float percent) {
        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }
        int volume = (int) (percent * mMaxVolume) + mCurVolume;
        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        } else if (volume < 0) {
            volume = 0;
        }
        mTouchLayout.setVisibility(View.VISIBLE);
        mVolumeGesture.setVisibility(View.VISIBLE);
        mVolumeGesture.setText(volume * 100 / mMaxVolume + "%");
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void endGesture() {
        refreshHideRunnable();
        if (mTargetPosition >= 0 && mTargetPosition != mVideoView.getCurrentPosition()) {
            seekTo(mTargetPosition);
            mPlaySeek.setProgress(mTargetPosition * MAX_VIDEO_SEEK / mVideoView.getDuration());
            if (mIsFullScreen) {
                mFullSeek.setProgress(mTargetPosition * MAX_VIDEO_SEEK / mVideoView.getDuration());
            }
            mTargetPosition = -1;
        }
        mTouchLayout.setVisibility(View.GONE);
        mBrightnessGesture.setVisibility(View.GONE);
        mVolumeGesture.setVisibility(View.GONE);
        mProgressSlide.setVisibility(View.GONE);
        mCurBrightness = -1;
        mCurVolume = -1;
    }

    // 切换控制锁
    private void togglePlayerLock() {
        mIsForbidTouch = !mIsForbidTouch;
        mPlayerLock.setSelected(mIsForbidTouch);
        if (mIsForbidTouch) {
            mOrientationListener.disable();
            hideAllView(true);
        } else {
            mOrientationListener.enable();
            mTopLayout.setVisibility(View.VISIBLE);
            mFullSeek.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onResume() {
        Log.d("7366453","1");
        mIsResume = true;
        mVideoView.resume();
        if (mCurPosition != -1) {
            Log.d("4747474", "onResume: 1");
            seekTo(mCurPosition);
            mCurPosition = -1;
        }
        mVideoView.start();
        mPlayButton.setSelected(false);
    }

    public void onPause() {
        Log.d("7366453","2");
        mCurPosition = mVideoView.getCurrentPosition();
        mVideoView.pause();
        mPlayButton.setSelected(true);
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            Log.d("7366453","8");
            mDanmakuView.pause();
        }
    }

    public void onStop() {
        Log.d("7366453","3");
        onPause();
        mVideoView.stopPlayback();
    }

    public int onDestroy() {
        Log.d("7366453","4");
        // 记录播放进度
        int curPosition = mVideoView.getCurrentPosition();
        mVideoView.destroy();
        IjkMediaPlayer.native_profileEnd();
        // 注销广播
        mAttachActivity.unregisterReceiver(mScreenReceiver);
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
        return curPosition;
    }

    @Override
    public void onClick(View v) {
        Log.d("6666", "3");
        refreshHideRunnable();
        int id = v.getId();
        if (id == R.id.play_start || id == R.id.play_button) {
            if (mVideoView.isPlaying()) {
                pause();
            } else {
                start();
            }
        }
        if (id == R.id.full_screen_button) {
            Log.d("6666", "1");
            changeScreen();
        }
        if (id == R.id.player_lock) {
            togglePlayerLock();
        }
        if (id == R.id.player_back_button) {
            mIsFullScreen = false;
            mFullScreen.setSelected(false);
            mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setScreenBar();
            changeHeight();
        }
        if (id == R.id.screenshot_button) {
            doScreenShot();
        }
        if (id == R.id.danmaku_control_btn) {
            if (mDanmakuControl.isSelected()) {
                mDanmakuControl.setSelected(false);
                mDanmakuView.show();
            } else {
                mDanmakuControl.setSelected(true);
                mDanmakuView.hide();
            }
        }
        if (id == R.id.danmaku_edit) {
            if (mSendDialog == null) {
                mSendDialog = new DanmakuSendDialog(mAttachActivity, mListener);
            }
            mKeyboardLayout.setKeyboardListener(new KeyboardLayout.KeyboardLayoutListener() {
                @Override
                public void onKeyboardStateChanged(Boolean isAlive, int keyboardHeight) {
                    if (isAlive) {
                        if (mKeyboardHeight != keyboardHeight) {
                            mSendDialog.setPaletteLayoutHeight(keyboardHeight);
                            mKeyboardHeight = keyboardHeight;
                        }
                    }
                }
            });
            mSendDialog.show();
        }
    }

    // 编辑屏幕显示状态
    private void editVideo() {
        if (mVideoView.isPlaying()) {
            pause();
            mPlayStatus = PLAY_STATUS;
        } else {
            mPlayStatus = PAUSE_STATUS;
        }
        hideAllView(false);
    }

    // 展示截屏对话框
    private void showScreenDialog(Bitmap bitmap) {
        if (mShareDialog == null) {
            mShareDialog = new ShareDialog();
            mShareDialog.setmSaveListener(mSaveListener);
            mShareDialog.setmDismissListener(mDismissListener);
        }
        mShareDialog.setScreenImage(bitmap);
        mShareDialog.show(mAttachActivity.getSupportFragmentManager(), "");
    }

    // 截屏
    private void doScreenShot() {
        editVideo();
        showScreenDialog(mVideoView.getScreenshot());
    }

    // 恢复视频状态
    private void recoverEditVideo() {
        if (mPlayStatus == PLAY_STATUS) {
            start();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mInitHeight == 0) {
            mInitHeight = getHeight();
            mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        }
    }

    private void refreshHideRunnable() {
        mHandler.removeCallbacks(mHideBarRunnable);
        mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIME);
    }

    /**
     * 弹幕相关操作
     */
    private void addDanmaku(boolean islive, String text) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = text;
        danmaku.priority = 0;
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.parseColor(mSendDialog.getColor());
        mDanmakuView.addDanmaku(danmaku);

    }

    private void resumeDanmaku() {
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            if (mDanmakuPosition != -1) {
                mDanmakuView.seekTo((long) mDanmakuPosition);
                mDanmakuPosition = -1;
            } else {
                mDanmakuView.resume();
            }
        }
    }
}
