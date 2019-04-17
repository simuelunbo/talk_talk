package com.example.sjb.talk_talk.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.wowza.gocoder.sdk.api.logging.WOWZLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerView extends AppCompatTextView {

    final public static long DEFAULT_REFRESH_INTERVAL = 1000L;

    private long mTimerStart = 0L;
    private long mTimerDuration = -1L;

    private ScheduledExecutorService mTimerThread = null;

    public interface TimerProvider {
        long getTimecode();
        long getDuration();
    }

    public void setTimerProvider(TimerProvider timerProvider) {
        mTimerProvider = timerProvider;
    }

    private TimerProvider mDefaultTimerProvider;
    private TimerProvider mTimerProvider;

    public TimerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mTimerProvider = null;
        mDefaultTimerProvider = new TimerProvider() {
            @Override
            public long getTimecode() {
                return System.currentTimeMillis() - mTimerStart;
            }
            @Override
            public long getDuration() {
                return -1L;
            }
        };
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public long getTimerDuration() {
        return mTimerDuration;
    }

    public void setTimerDuration(long timerDuration) {
        mTimerDuration = timerDuration;
    }

    public void startTimer() {
        startTimer(DEFAULT_REFRESH_INTERVAL);
    }

    public synchronized void startTimer(long refreshInterval) {
        if (mTimerThread != null){  return; }
        if (mTimerProvider == null) mTimerProvider = mDefaultTimerProvider;

        setText("00:00:00");

        mTimerStart = System.currentTimeMillis();
        mTimerThread = Executors.newSingleThreadScheduledExecutor();
        mTimerThread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setText(genTimerDisplay());
                    }
                });
            }
        }, refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);

        setVisibility(VISIBLE);
    }

    private String genTimerDisplay() {
        String formatStr;

        long timecodeMs = mTimerProvider.getTimecode();
        long durationMs = mTimerProvider.getDuration();

        long timecodeTotalSeconds = timecodeMs / 1000L;
        long timecodeHours = timecodeTotalSeconds / 3600L,
                timecodeMinutes = (timecodeTotalSeconds / 60L) % 60L,
                timecodeSeconds = timecodeTotalSeconds % 60L;

        if (durationMs > 0L && durationMs >= timecodeMs) {
            long durationTotalSeconds = durationMs / 1000L;
            long durationHours = durationTotalSeconds / 3600L,
                    durationMinutes = (durationTotalSeconds / 60L) % 60L,
                    durationSeconds = durationTotalSeconds % 60L;

            formatStr = String.format("%02d:%02d:%02d / %02d:%02d:%02d", timecodeHours, timecodeMinutes, timecodeSeconds, durationHours, durationMinutes, durationSeconds);
        } else
            formatStr = String.format("%02d:%02d:%02d", timecodeHours, timecodeMinutes, timecodeSeconds);

        return formatStr;
    }

    public synchronized void stopTimer() {
        if (mTimerThread == null) return;

        WOWZLog.debug("timer stopped");
        mTimerThread.shutdown();
        mTimerThread = null;

        setVisibility(INVISIBLE);
        setText("00:00:00");
    }

    public synchronized boolean isRunning() {
        return (mTimerThread != null);
    }
}
