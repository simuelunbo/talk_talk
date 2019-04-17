package com.example.sjb.talk_talk.config;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.activity.GoCoderSDKActivityBase;
import com.wowza.gocoder.sdk.api.codec.WOWZCodecUtils;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZStreamConfig;
import com.wowza.gocoder.sdk.api.configuration.WowzaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.h264.WOWZProfileLevel;

import java.util.Arrays;
import java.util.HashMap;

public class GoCoderSDKPrefs {
    private final static String TAG = GoCoderSDKPrefs.class.getSimpleName();

    public static void updateConfigFromPrefs(SharedPreferences sharedPrefs, WOWZMediaConfig mediaConfig) {
        // video settings
        mediaConfig.setVideoEnabled(sharedPrefs.getBoolean("wz_video_enabled", true));

        mediaConfig.setVideoFrameWidth(sharedPrefs.getInt("wz_video_frame_width", WOWZMediaConfig.DEFAULT_VIDEO_FRAME_WIDTH));
        mediaConfig.setVideoFrameHeight(sharedPrefs.getInt("wz_video_frame_height", WOWZMediaConfig.DEFAULT_VIDEO_FRAME_HEIGHT));
        mediaConfig.setVideoFramerate(Integer.parseInt(sharedPrefs.getString("wz_video_frame_rate", String.valueOf(WOWZMediaConfig.DEFAULT_VIDEO_FRAME_RATE))));
        mediaConfig.setVideoKeyFrameInterval(Integer.parseInt(sharedPrefs.getString("wz_video_keyframe_interval", String.valueOf(WOWZMediaConfig.DEFAULT_VIDEO_KEYFRAME_INTERVAL))));
        mediaConfig.setVideoBitRate(Integer.parseInt(sharedPrefs.getString("wz_video_bitrate", String.valueOf(WOWZMediaConfig.DEFAULT_VIDEO_BITRATE))));
        mediaConfig.setABREnabled(sharedPrefs.getBoolean("wz_video_use_abr", true));
        mediaConfig.setHLSEnabled(sharedPrefs.getBoolean("wz_use_hls", false));
        mediaConfig.setHLSBackupURL(sharedPrefs.getString("wz_hls_failover", null));
//        mediaConfig.setPlayerExampleAutoconfig(sharedPrefs.getString("wz_player_example_config","None"));

        int profile = sharedPrefs.getInt("wz_video_profile_level_profile", -1);
        int level = sharedPrefs.getInt("wz_video_profile_level_level", -1);
        if (profile != -1 && level != -1) {
            WOWZProfileLevel profileLevel = new WOWZProfileLevel(profile, level);
            if (profileLevel.validate()) {
                mediaConfig.setVideoProfileLevel(profileLevel);
            }
        } else {
            mediaConfig.setVideoProfileLevel(null);
        }

        // audio settings
        mediaConfig.setAudioEnabled(sharedPrefs.getBoolean("wz_audio_enabled", true));

        mediaConfig.setAudioSampleRate(Integer.parseInt(sharedPrefs.getString("wz_audio_samplerate", String.valueOf(WOWZMediaConfig.DEFAULT_AUDIO_SAMPLE_RATE))));
        mediaConfig.setAudioChannels(sharedPrefs.getBoolean("wz_audio_stereo", true) ? WOWZMediaConfig.AUDIO_CHANNELS_STEREO : WOWZMediaConfig.AUDIO_CHANNELS_MONO);
        mediaConfig.setAudioBitRate(Integer.parseInt(sharedPrefs.getString("wz_audio_bitrate", String.valueOf(WOWZMediaConfig.DEFAULT_AUDIO_BITRATE))));
    }

    public static void updateConfigFromPrefsForPlayer(SharedPreferences sharedPrefs, WOWZStreamConfig streamConfig) {
        // connection settings
        streamConfig.setHostAddress(sharedPrefs.getString("wz_live_host_address", null));
        String portNumber = sharedPrefs.getString("wz_live_port_number", String.valueOf(WowzaConfig.DEFAULT_PORT));
        if(portNumber!="") {
            streamConfig.setPortNumber(Integer.parseInt(portNumber));
        }
        //streamConfig.setUseSSL(sharedPrefs.getBoolean("wz_live_use_ssl", false));
        streamConfig.setApplicationName(sharedPrefs.getString("wz_live_app_name", WowzaConfig.DEFAULT_APP));
        streamConfig.setStreamName(sharedPrefs.getString("wz_live_stream_name", WowzaConfig.DEFAULT_STREAM));
        streamConfig.setUsername(sharedPrefs.getString("wz_live_username", null));
        streamConfig.setPassword(sharedPrefs.getString("wz_live_password", null));
        streamConfig.setIsPlayback(true);

        updateConfigFromPrefs(sharedPrefs, (WOWZMediaConfig) streamConfig);
    }

    public static void updateConfigFromPrefs(SharedPreferences sharedPrefs, WOWZStreamConfig streamConfig) {
        // connection settings
        streamConfig.setHostAddress(sharedPrefs.getString("wz_live_host_address", null));
        streamConfig.setPortNumber(Integer.parseInt(sharedPrefs.getString("wz_live_port_number", String.valueOf(WowzaConfig.DEFAULT_PORT))));
        //streamConfig.setUseSSL(sharedPrefs.getBoolean("wz_live_use_ssl", false));
        streamConfig.setApplicationName(sharedPrefs.getString("wz_live_app_name", WowzaConfig.DEFAULT_APP));
        streamConfig.setStreamName(sharedPrefs.getString("wz_live_stream_name", WowzaConfig.DEFAULT_STREAM));
        streamConfig.setUsername(sharedPrefs.getString("wz_live_username", null));
        streamConfig.setPassword(sharedPrefs.getString("wz_live_password", null));

        streamConfig.setIsPlayback(false);
        updateConfigFromPrefs(sharedPrefs, (WOWZMediaConfig) streamConfig);
    }

    public static void updateConfigFromPrefs(SharedPreferences sharedPrefs, WowzaConfig wowzaConfig) {
        // WowzaConfig-specific properties
        wowzaConfig.setCapturedVideoRotates(sharedPrefs.getBoolean("wz_captured_video_rotates", true));

        updateConfigFromPrefs(sharedPrefs, (WOWZStreamConfig) wowzaConfig);
    }

    public static int getScaleMode(SharedPreferences sharedPrefs) {
        return sharedPrefs.getBoolean("wz_video_resize_to_aspect", false) ? WOWZMediaConfig.RESIZE_TO_ASPECT : WOWZMediaConfig.FILL_VIEW;
    }

    public static String getBackupHLSURL(SharedPreferences sharedPrefs) {
        try {
            return sharedPrefs.getString("wz_hls_failover", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getPlayerExampleConfig(SharedPreferences sharedPrefs) {
        try {
            return sharedPrefs.getString("wz_player_example_config", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean getUseHLS(SharedPreferences sharedPrefs) {
        try {
            return sharedPrefs.getBoolean("wz_use_hls", false);
        } catch (Exception e) {
            return false;
        }
    }

    public static float getPreBufferDuration(SharedPreferences sharedPrefs) {
        try {
            return Float.parseFloat(sharedPrefs.getString("wz_video_player_prebuffer_duration", "0"));
        } catch (Exception e) {
            return 0f;
        }
    }

/*
    public static int getScaleAndCropMode(SharedPreferences sharedPrefs) {
        return sharedPrefs.getBoolean("wz_video_scale_and_crop", false) ? WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
    }
*/

    public static void storeHostConfig(SharedPreferences sharedPrefs, WOWZStreamConfig streamConfig) {
        String hostAddress = streamConfig.getHostAddress();
        if (hostAddress == null || hostAddress.trim().length() == 0) return;
//        AutoCompletePreference.storeAutoCompleteHostConfig(sharedPrefs, streamConfig);
    }

    public static class PrefsFragment extends PreferenceFragment {

        private boolean  mShowConnectionPrefs    = true;
        private boolean  mShowAudioPrefs         = true;
        private boolean  mShowVideoPrefs         = true;

        private boolean  mFixedVideoSource      = false;
        private boolean  mForPlayback           = false;
        private boolean  mFixedAudioSource      = false;

        private WOWZCamera mActiveCamera           = null;

        private int mPriorUIIVisibilityFlags    = -1;

        public void setShowConnectionPrefs(boolean showConnectionPrefs) {
            mShowConnectionPrefs = showConnectionPrefs;
        }

        public void setShowVideoPrefs(boolean showVideoPrefs) {
            mShowVideoPrefs = showVideoPrefs;
        }

        public void setFixedSource(boolean fixedSource) {
            setFixedVideoSource(fixedSource);
            setFixedAudioSource(fixedSource);
        }

        public void setFixedVideoSource(boolean fixedVideoSource) {
            mFixedVideoSource = fixedVideoSource;
        }

        public void setForPlayback(boolean forPlayback) {
            mForPlayback = forPlayback;
        }

        public void setFixedAudioSource(boolean fixedAudioSource) {
            mFixedAudioSource = fixedAudioSource;
        }

        public void setShowAudioPrefs(boolean showAudioPrefs) {
            mShowAudioPrefs = showAudioPrefs;
        }

        public void setActiveCamera(WOWZCamera camera) {
            mActiveCamera = camera;
        }

        private HashMap<String, String> mSummaryTexts = new HashMap<String, String>();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null)
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.background_dark));

            View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootView != null) {
                mPriorUIIVisibilityFlags = rootView.getSystemUiVisibility();
                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }

            return view;
        }

        @Override
        public void onDestroyView() {
            View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootView != null && mPriorUIIVisibilityFlags != -1) {
                rootView.setSystemUiVisibility(mPriorUIIVisibilityFlags);
            }

            super.onDestroyView();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.gocoder_sdk_pref);
            // 옵션 정보
            mSummaryTexts.clear();

            PreferenceScreen prefsScreen = (PreferenceScreen) findPreference("prefs_screen_gocoder_sdk");
            final SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            /**** Connection preferences ****/

            PreferenceCategory prefsCategory = (PreferenceCategory)findPreference("prefs_category_connection");
            prefsScreen.removePreference(prefsCategory);
//            if (!mShowConnectionPrefs) {
//                prefsScreen.removePreference(prefsCategory);
//            } else { // port값 앱 이름 스트리밍 이름 유저이름 패스워드 수정하는 코드
//                storeSummaryTexts("prefs_category_connection", mSummaryTexts);
//
//                String[] prefIds = {
//                        "wz_live_port_number",
//                        "wz_live_app_name",
//                        "wz_live_stream_name",
//                        "wz_live_username"
//                };
//                configurePrefSummaries(this, mSharedPreferences, prefsCategory, prefIds);
//
//                String[] pwIds = {
//                        "wz_live_password"
//                };
//                configurePrefSummaries(this, mSharedPreferences, prefsCategory, pwIds, true);
//
//                Preference hostAddressPref = prefsCategory.findPreference("wz_live_host_address");
//                setSummaryText(mSharedPreferences, hostAddressPref);
//
//                //
//                // Host address preference
//                //
//                hostAddressPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                    @Override
//                    public boolean onPreferenceChange(Preference preference, Object hostAddress) {
//                        if (hostAddress instanceof String) {
//
//                            //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                            WOWZStreamConfig hostConfig = AutoCompletePreference.loadAutoCompleteHostConfig(mSharedPreferences, (String)hostAddress);
//
//                            if (hostConfig != null) {
//                                WOWZLog.debug("SDKPREFS: RUNNING: "+hostConfig.getHLSBackupURL());
//                                SharedPreferences.Editor editor = mSharedPreferences.edit();
//                                editor.putString("wz_live_port_number", String.valueOf(hostConfig.getPortNumber()));
//                                editor.putString("wz_live_app_name", hostConfig.getApplicationName());
//                                editor.putString("wz_live_stream_name", hostConfig.getStreamName());
//                                editor.putString("wz_live_username", hostConfig.getUsername());
//                                editor.apply();
//
//                                AutoCompletePreference pref = (AutoCompletePreference)findPreference("wz_live_host_address");
//                                setSummaryText(pref, (String)hostAddress);
//
//                                pref = (AutoCompletePreference)findPreference("wz_live_port_number");
//                                setSummaryText(mSharedPreferences, pref);
//
//                                pref = (AutoCompletePreference)findPreference("wz_live_app_name");
//                                setSummaryText(mSharedPreferences, pref);
//
//                                pref = (AutoCompletePreference)findPreference("wz_live_stream_name");
//                                setSummaryText(mSharedPreferences, pref);
//
//                                pref = (AutoCompletePreference)findPreference("wz_live_username");
//                                setSummaryText(mSharedPreferences, pref);
//                            }
//                        }
//                        return true;
//                    }
//                });
//
//            }

            /**** Video preferences ****/

            prefsCategory = (PreferenceCategory)findPreference("prefs_category_video");

            if (!mShowVideoPrefs) {
                prefsScreen.removePreference(prefsCategory);
            } else {
                storeSummaryTexts("prefs_category_video", mSummaryTexts);

                if (mFixedVideoSource || mForPlayback) {
                    //
                    // Remove preferences for fixed video broadcast sources (e.g. MP4 file)
                    //
                    prefsCategory.removePreference(findPreference("wz_video_preset"));
                    prefsCategory.removePreference(findPreference("wz_video_frame_size"));
                    prefsCategory.removePreference(findPreference("wz_video_bitrate"));
                    prefsCategory.removePreference(findPreference("wz_video_framerate"));
                    prefsCategory.removePreference(findPreference("wz_video_keyframe_interval"));
                    prefsCategory.removePreference(findPreference("wz_video_profile_level"));
                    prefsCategory.removePreference(findPreference("wz_video_resize_to_aspect"));
                    prefsCategory.removePreference(findPreference("wz_video_use_abr"));

                    if (mForPlayback) {
                        String[] prefIds = {
                                "wz_video_player_prebuffer_duration"
                        };
                        configurePrefSummaries(this, mSharedPreferences, prefsCategory, prefIds);
                    }

                } else {
                    prefsCategory.removePreference(findPreference("wz_video_player_prebuffer_duration"));
                    prefsCategory.removePreference(findPreference("wz_use_hls"));
                    prefsCategory.removePreference(findPreference("wz_hls_failover"));
//                    prefsCategory.removePreference(findPreference("wz_player_example_config"));

                    //prefsCategory.removePreference(findPreference("wz_video_scale_and_crop"));

                    String[] prefIds = {
                            "wz_video_bitrate",
                            "wz_video_framerate",
                            "wz_video_keyframe_interval"
                    };
                    configurePrefSummaries(this, mSharedPreferences, prefsCategory, prefIds);

                    final ListPreference videoPresetPref = (ListPreference) findPreference("wz_video_preset");
                    final ListPreference videoFrameSizePref = (ListPreference) findPreference("wz_video_frame_size");

                    //
                    // Video frame size and bitrate preset preference
                    //
                    if (mActiveCamera == null) {
                        prefsCategory.removePreference(videoFrameSizePref);
                        prefsCategory.removePreference(videoPresetPref);
                    } else {
                        final WOWZMediaConfig[] presetConfigs = mActiveCamera.getSupportedConfigs();

                        String[] presetLabels = new String[presetConfigs.length];
                        String[] presetValues = new String[presetConfigs.length];
                        for(int i=0; i < presetConfigs.length; i++) {
                            presetLabels[i] = presetConfigs[i].getLabel(true, true, true, true);
                            presetValues[i] = String.valueOf(i);
                        }

                        videoPresetPref.setEntries(presetLabels);
                        videoPresetPref.setEntryValues(presetValues);

                        final EditTextPreference bitRatePref = (EditTextPreference)findPreference("wz_video_bitrate");
                        videoPresetPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object o) {
                                if (o instanceof String) {
                                    try {
                                        int selectedIndex = Integer.parseInt((String) o);

                                        if (selectedIndex >= 0 && selectedIndex < presetConfigs.length) {
                                            WOWZMediaConfig selectedConfig = presetConfigs[selectedIndex];

                                            //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                            SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
                                            prefsEditor.putInt("wz_video_frame_width", selectedConfig.getVideoFrameWidth());
                                            prefsEditor.putInt("wz_video_frame_height", selectedConfig.getVideoFrameHeight());
                                            prefsEditor.putString("wz_video_bitrate", String.valueOf(selectedConfig.getVideoBitRate()));
                                            prefsEditor.apply();

                                            WOWZSize prefSize = new WOWZSize(selectedConfig.getVideoFrameWidth(), selectedConfig.getVideoFrameHeight());
                                            setSummaryText(videoFrameSizePref, prefSize.toString());

                                            bitRatePref.setText(String.valueOf(selectedConfig.getVideoBitRate()));
                                            setSummaryText(bitRatePref, String.valueOf(selectedConfig.getVideoBitRate()));
                                        }
                                    } catch (NumberFormatException e) {
                                        // bad no. returned
                                    }
                                }
                                return true;
                            }
                        });

                        //
                        // Video frame size preference
                        //
                        final WOWZSize[] frameSizes = mActiveCamera.getSupportedFrameSizes();

                        int currentFrameWidth = mSharedPreferences.getInt("wz_video_frame_width", WOWZMediaConfig.DEFAULT_VIDEO_FRAME_WIDTH);
                        int currentFrameHeight = mSharedPreferences.getInt("wz_video_frame_height", WOWZMediaConfig.DEFAULT_VIDEO_FRAME_HEIGHT);
                        WOWZSize currentFrameSize = new WOWZSize(currentFrameWidth, currentFrameHeight);
                        setSummaryText(videoFrameSizePref, currentFrameSize.toString());

                        int curFrameSizeIdx = frameSizes.length - 1;

                        String[] frameSizeLabels = new String[frameSizes.length];
                        String[] frameSizeValues = new String[frameSizes.length];
                        for(int i=0; i < frameSizes.length; i++) {
                            frameSizeLabels[i] = frameSizes[i].toString();
                            frameSizeValues[i] = String.valueOf(i);
                            if (frameSizes[i].equals(currentFrameSize))
                                curFrameSizeIdx = i;
                        }

                        videoFrameSizePref.setEntries(frameSizeLabels);
                        videoFrameSizePref.setEntryValues(frameSizeValues);
                        videoFrameSizePref.setValueIndex(curFrameSizeIdx);

                        videoFrameSizePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object o) {
                                if (o instanceof String) {
                                    try {
                                        int selectedIndex = Integer.parseInt((String) o);

                                        if (selectedIndex >= 0 && selectedIndex < frameSizes.length) {
                                            WOWZSize selectedFrameSize = frameSizes[selectedIndex];

                                            SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
                                            prefsEditor.putInt("wz_video_frame_width", selectedFrameSize.getWidth());
                                            prefsEditor.putInt("wz_video_frame_height", selectedFrameSize.getHeight());
                                            prefsEditor.apply();

                                            setSummaryText(videoFrameSizePref, selectedFrameSize.toString());
                                        }
                                    } catch (NumberFormatException e) {
                                        // bad no. returned
                                    }
                                }
                                return true;
                            }
                        });
                    }

                    //
                    // H.264 profile level preference
                    //
                    final WOWZProfileLevel avcProfileLevels[] = WOWZCodecUtils.getProfileLevels();

                    final ListPreference profileLevelsPref = (ListPreference) findPreference("wz_video_profile_level");
                    if (avcProfileLevels.length == 0) {
                        prefsCategory.removePreference(profileLevelsPref);

                        SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();
                        prefsEditor.putInt("wz_video_profile_level_profile", -1);
                        prefsEditor.putInt("wz_video_profile_level_level", -1);
                        prefsEditor.apply();
                    }
                    else {
                        int profile = mSharedPreferences.getInt("wz_video_profile_level_profile", -1);
                        int level = mSharedPreferences.getInt("wz_video_profile_level_level", -1);

                        WOWZProfileLevel profileLevel = new WOWZProfileLevel(profile, level);
                        int prefIndex = avcProfileLevels.length;

                        String[] entries = new String[avcProfileLevels.length + 1];
                        String[] entryValues = new String[avcProfileLevels.length + 1];
                        for (int i = 0; i < avcProfileLevels.length; i++) {
                            entries[i] = avcProfileLevels[i].toString();
                            entryValues[i] = String.valueOf(i);
                            if (avcProfileLevels[i].equals(profileLevel))
                                prefIndex = i;
                        }
                        entries[avcProfileLevels.length] = "(none)";
                        entryValues[avcProfileLevels.length] = "-1";

                        profileLevelsPref.setEntries(entries);
                        profileLevelsPref.setEntryValues(entryValues);
                        profileLevelsPref.setValueIndex(prefIndex);

                        setSummaryText(profileLevelsPref, (prefIndex != avcProfileLevels.length ? profileLevel.toString() : null));

                        profileLevelsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object o) {
                                if (o instanceof String) {
                                    try {
                                        int selectedIndex = Integer.parseInt((String) o);

                                        SharedPreferences.Editor prefsEditor = mSharedPreferences.edit();

                                        if (selectedIndex >= 0 && selectedIndex < avcProfileLevels.length) {
                                            WOWZProfileLevel selectedProfileLevel = avcProfileLevels[selectedIndex];
                                            prefsEditor.putInt("wz_video_profile_level_profile", selectedProfileLevel.getProfile());
                                            prefsEditor.putInt("wz_video_profile_level_level", selectedProfileLevel.getLevel());
                                            setSummaryText(profileLevelsPref, selectedProfileLevel.toString());
                                        } else {
                                            prefsEditor.putInt("wz_video_profile_level_profile", -1);
                                            prefsEditor.putInt("wz_video_profile_level_level", -1);
                                            setSummaryText(profileLevelsPref, null);
                                        }
                                        prefsEditor.apply();

                                    } catch (NumberFormatException e) {
                                        // bad no. returned
                                    }
                                }
                                return true;
                            }
                        });
                    }
                }
            }

            /**** Audio preferences ****/

            prefsCategory = (PreferenceCategory) findPreference("prefs_category_audio");
            if (!mShowAudioPrefs) {
                CheckBoxPreference prefsEnabled = (CheckBoxPreference) findPreference("wz_audio_enabled");
                prefsScreen.removePreference(prefsEnabled);
                prefsScreen.removePreference(prefsCategory);
            } else if (mFixedAudioSource) {
                //
                // Remove preferences for fixed video broadcast sources (e.g. Mp4 file)
                //
                prefsCategory.removePreference(findPreference("wz_audio_bitrate"));
                prefsCategory.removePreference(findPreference("wz_audio_samplerate"));
                prefsCategory.removePreference(findPreference("wz_audio_stereo"));
            } else {
                String[] prefIds = {
                        "wz_audio_samplerate",
                        "wz_audio_bitrate"
                };

                storeSummaryTexts("prefs_category_audio", mSummaryTexts);
                configurePrefSummaries(this, mSharedPreferences, prefsCategory, prefIds);
            }

        }

        private void configurePrefSummaries(final PreferenceFragment prefFragment,
                                            final SharedPreferences sharedPreferences,
                                            PreferenceCategory prefCategory,
                                            String[] prefKeys,
                                            boolean isPassword) {

            for(String prefKey : prefKeys) {
                Preference pref = prefCategory.findPreference(prefKey);
                pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o instanceof String) {
                            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(prefFragment.getActivity());
                            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                            prefsEditor.putString(preference.getKey(), (String)o);
                            prefsEditor.apply();

                            boolean isPasswordField = ((preference instanceof EditTextPreference) &&
                                    ((EditTextPreference)preference).getEditText().getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));

                            setSummaryText(preference, (String)o, isPasswordField);
                        }
                        return true;
                    }
                });

                setSummaryText(sharedPreferences, pref, isPassword);
            }
        }

        @Override
        public void onPause() {
            Activity parentActivity = getActivity();
            if (parentActivity != null && parentActivity instanceof GoCoderSDKActivityBase)
                ((GoCoderSDKActivityBase)parentActivity).syncPreferences();

            super.onPause();
        }

        private void configurePrefSummaries(final PreferenceFragment prefFragment,
                                            SharedPreferences sharedPreferences,
                                            PreferenceCategory prefCategory,
                                            String[] prefKeys) {

            configurePrefSummaries(prefFragment, sharedPreferences, prefCategory, prefKeys, false);
        }

        private void storeSummaryTexts(String categoryKey, HashMap<String, String> summaryTexts) {
            PreferenceCategory prefsCategory = (PreferenceCategory) findPreference(categoryKey);

            int nPrefs = prefsCategory.getPreferenceCount();
            for(int i=0;i<nPrefs;i++) {
                Preference pref = prefsCategory.getPreference(i);
                summaryTexts.put(pref.getKey(), pref.getSummary().toString());
            }
        }

        private String getStoredSummaryText(String prefKey, HashMap<String, String> summaryTexts) {
            return (summaryTexts.containsKey(prefKey) ? summaryTexts.get(prefKey) : null);
        }

        private void setSummaryText(Preference pref, String prefValueText, boolean isPasswordPref) {
            String prefKey = pref.getKey();
            if (prefValueText == null || prefValueText.trim().length() == 0) {
                pref.setSummary(getStoredSummaryText(prefKey, mSummaryTexts));
            } else {
                Spannable prefSummary ;
                int color = ContextCompat.getColor(getActivity(), android.R.color.holo_blue_light);

                if (isPasswordPref) {
                    char[] masked = new char[prefValueText.length()];
                    Arrays.fill(masked, '*');
                    prefSummary = new SpannableString(String.valueOf(masked));
                } else {
                    prefSummary = new SpannableString(prefValueText);
                }

                prefSummary.setSpan(new ForegroundColorSpan(color), 0, prefSummary.length(), 0);
                pref.setSummary(prefSummary);
            }
        }

        private void setSummaryText(Preference pref, String prefValueText) {
            setSummaryText(pref, prefValueText, false);
        }

        private void setSummaryText(EditTextPreference pref, String prefValueText) {
            pref.setText(prefValueText);
        }

        private void setSummaryText(SharedPreferences sharedPreferences, Preference pref, boolean isPasswordPref) {
            String prefKey = pref.getKey();
            String prefValue = sharedPreferences.getString(prefKey, null);
            setSummaryText(pref, prefValue, isPasswordPref);
        }

        private void setSummaryText(SharedPreferences sharedPreferences, Preference pref) {
            setSummaryText(sharedPreferences, pref, false);
        }

    }

}