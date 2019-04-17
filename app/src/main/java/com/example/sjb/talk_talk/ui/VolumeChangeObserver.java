package com.example.sjb.talk_talk.ui;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

public class VolumeChangeObserver extends ContentObserver {
    int previousVolume = 0;

    int mStreamType = AudioManager.STREAM_MUSIC;
    Context mContext = null;
    private VolumeChangeListener mVolumeChangeListener = null;

    public int getStreamType() {
        return mStreamType;
    }

    public void setStreamType(int streamType) {
        mStreamType = streamType;
    }

    public void setVolumeChangeListener(VolumeChangeListener volumeChangeListener) {
        mVolumeChangeListener = volumeChangeListener;
    }

    public void clearVolumeChangeListener() {
        mVolumeChangeListener = null;
    }

    public interface VolumeChangeListener {
        void onVolumeChanged(int previousLevel, int currentLevel);
    }

    public VolumeChangeObserver(Context c, Handler handler) {
        super(handler);
        mContext = c;

        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = normalizedLevel(audio, audio.getStreamVolume(mStreamType), mStreamType);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = normalizedLevel(audio, audio.getStreamVolume(mStreamType), mStreamType);

        int delta=previousVolume-currentVolume;
        if (delta != 0) {
            previousVolume = currentVolume;
            if (mVolumeChangeListener != null) {
                mVolumeChangeListener.onVolumeChanged(previousVolume, currentVolume);
            }
        }

        previousVolume=currentVolume;
    }

    private int normalizedLevel(AudioManager audioManager, int level, int streamType) {
        float maxLevel = (float)audioManager.getStreamMaxVolume(streamType);
        if (maxLevel != 0f)
            return Math.round(((float)audioManager.getStreamVolume(streamType) / maxLevel) * 100f);

        return level;
    }

}