package com.example.sjb.talk_talk.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_CameraActivity;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_PlayerActivity;
import com.example.sjb.talk_talk.Request.ChatRoomMsgUpdateRequest;
import com.example.sjb.talk_talk.Request.MyImgAndPersonnelCheckInRoomRequest;
import com.example.sjb.talk_talk.SQL_DBdata.DBHelper;
import com.example.sjb.talk_talk.config.GoCoderSDKPrefs;
import com.example.sjb.talk_talk.ui.DataTableFragment;
import com.example.sjb.talk_talk.ui.MultiStateButton;
import com.example.sjb.talk_talk.ui.StatusView;
import com.example.sjb.talk_talk.ui.TimerView;
import com.example.sjb.talk_talk.ui.VolumeChangeObserver;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataEvent;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayerActivity extends GoCoderSDKActivityBase {
    final private static String TAG = PlayerActivity.class.getSimpleName();

    // Stream player view
    private WOWZPlayerView mStreamPlayerView = null;
    private WOWZPlayerConfig mStreamPlayerConfig = null;

    // UI controls
    private MultiStateButton mBtnPlayStream   = null;
    private MultiStateButton    mBtnSettings     = null;
    private MultiStateButton    mBtnMic          = null;
    private MultiStateButton    mBtnScale        = null;
    private SeekBar mSeekVolume      = null;
    private ProgressDialog mBufferingDialog = null;

    private StatusView mStatusView       = null;
    private TextView mHelp             = null;
    private TimerView mTimerView        = null;
    private ImageButton mStreamMetadata   = null;
    private boolean           mUseHLSPlayback   = false;
    private WOWZPlayerView.PacketThresholdChangeListener packetChangeListener = null;
    private VolumeChangeObserver mVolumeSettingChangeObserver = null;

    Button Msgbtn; // 채팅 입력 버튼
    EditText Msgedt; // 채팅 에디트 텍스트
    TextView Viewers; // 인원수 나타내는 텍스트

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    String userEmail, roomname;
    String userName;
    String userGoogle;
    DBHelper dbHelper;

    Handler handler;
    String data;

    int NumberOfPeople;
    SimpleDateFormat mFormat;
    long mNow;
    Date mDate;
    String[] array;

    String img_url; // 자기 프로필 사진 위치

    String chatID;


    private static final String HOST = "115.71.239.124";
    private static final int PORT = 5001;

    String StreamCloudServer = "976abd.entrypoint.cloud.wowza.com";
    String ApplicationName = "app-6464";
    String SourceUsername = "client38577";
    String SourcePassword = "00874cae";

    SocketChannel socketChannel;

    String RandomRoomID;


    SimpleDateFormat nFormat;
    long nNow;
    Date nDate;

    Adapter_PlayerActivity mAdapter;
    ArrayList<Item_Chatroom> chat = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_player);


        mRequiredPermissions = new String[]{};

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);

        mBtnPlayStream = (MultiStateButton) findViewById(R.id.ic_play_stream);
        mBtnSettings = (MultiStateButton) findViewById(R.id.ic_settings);
        mBtnMic = (MultiStateButton) findViewById(R.id.ic_mic);
        mBtnScale = (MultiStateButton) findViewById(R.id.ic_scale);

        mTimerView = (TimerView) findViewById(R.id.txtTimer);
        mStatusView = (StatusView) findViewById(R.id.statusView);
        mStreamMetadata = (ImageButton) findViewById(R.id.imgBtnStreamInfo);
        mHelp = (TextView) findViewById(R.id.streamPlayerHelp);

        mSeekVolume = (SeekBar) findViewById(R.id.sb_volume);

        mTimerView.setVisibility(View.GONE);

        Msgbtn = (Button) findViewById(R.id.StreamSendMsgBtn);
        Msgedt = (EditText) findViewById(R.id.StreamMsgEditText);
        Viewers = (TextView) findViewById(R.id.viewers);

        //--------------------------------- 리사이클러뷰 ------------------------------------------
        recyclerView = (RecyclerView) findViewById(R.id.ViewerChat);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // recyclerview
        dbHelper = new DBHelper(getApplicationContext(),"Chatroom_record.db", null,1);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);
        final SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        userGoogle = sp.getString("google","");

        Intent intent = new Intent(this.getIntent());
        roomname = intent.getStringExtra("방이름");
        chatID = intent.getStringExtra("방ID");
        RandomRoomID = intent.getStringExtra("streamID");

        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        Log.i(TAG+"방이름", ""+roomname);
//        Log.i(TAG+"방ID", ""+chatID);
//        Log.i("스트리밍방ID", ""+RandomRoomID);
//        editor.putString("wz_live_port_number", String.valueOf(hostConfig.getPortNumber()));
        editor.putString("wz_live_host_address",StreamCloudServer);
        editor.putString("wz_live_app_name", ApplicationName);
        editor.putString("wz_live_stream_name", RandomRoomID);
        editor.putString("wz_live_username", SourceUsername);
        editor.putString("wz_live_password", SourcePassword);
        editor.apply();
        // 스트리밍 방ID와 서버, 앱, 유저이름. 비밀번호 설정 저장

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                Log.i("서버값", ""+response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    //서버에서 보내준 값이 true이면?
                    if (success) {
                        img_url = jsonResponse.getString("profile"); // 프로필 사진 이미지 주소값
                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople");  // 방에 존재하는 인원수
                        Log.i("인원수 체크~~!!!", ""+NumberOfPeople);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };//responseListener 끝
        MyImgAndPersonnelCheckInRoomRequest session_request = new MyImgAndPersonnelCheckInRoomRequest(chatID,userEmail,userName,userGoogle, responseListener);
        RequestQueue queue = Volley.newRequestQueue(PlayerActivity.this);
        queue.add(session_request);

        //------------------------------------------------------------------------------------------------------------------------

        Msgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String return_msg = Msgedt.getText().toString();
                    Log.i("보내는값이 보내지는가", ""+return_msg);

                    mFormat = new SimpleDateFormat("a hh:mm");

                    mNow = System.currentTimeMillis();
                    mDate = new Date(mNow);

                    nFormat = new SimpleDateFormat("MM/dd a hh:mm");

                    nNow = System.currentTimeMillis();
                    nDate = new Date(nNow);

                    dbHelper.insert(chatID, userEmail, userName, img_url, return_msg, 0, mFormat.format(mDate),NumberOfPeople-1 ,0); // readcheck 0일때 참 1 일때는 거짓

                    chat.add(new Item_Chatroom(chatID, userEmail,userName,img_url,return_msg,0, mFormat.format(mDate),NumberOfPeople-1 ,true));
                    mAdapter = new Adapter_PlayerActivity(chat);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chat.size()-1); // 리사이클러뷰 포지셔닝 밑으로


                    final String sendok = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+return_msg+"!@#&"+"0"+"!@#&";// '0번째' 방ID '첫번째' 유저 email '두번째' 유저 이름 '3번째'보내는 유저 프로필 사진 '4번째' 보내는 메세지 '5번째' 상태 알림창 0번일때 메세지 1번일땐 들어오고 나가고
                    if (!TextUtils.isEmpty(return_msg)) {

                        Response.Listener<String> responseListener = new Response.Listener<String>() {

                            //서버로부터 여기서 데이터를 받음
                            @Override
                            public void onResponse(String response) {
                                Log.i("서버값", ""+response);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");

                                    //서버에서 보내준 값이 true이면?
                                    if (success) {
                                        new PlayerActivity.SendmsgTask1().execute(sendok);

                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };//responseListener 끝
                        ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, return_msg, nFormat.format(nDate), responseListener);
                        RequestQueue queue = Volley.newRequestQueue(PlayerActivity.this);
                        queue.add(chatRoomMsgUpdateRequest);
                    }

                } catch (Exception e) {
                    Log.i("button 쪽 에러", ""+e);
                    e.printStackTrace();
                }
            }
        });


        //////------------------------------------------------------------------------------------------------------- 채팅 소켓 연결

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    Log.i("socketchannel 연결된값", ""+socketChannel);
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + " a");
                    ioe.printStackTrace();
                }
                checkUpdate.start();
            }
        }).start();

        String view = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+"방송시청"+"!@#&"+"6"+"!@#&";
        delaymessage(view); // 방송 시청

        //------------------------------------------------------------------------------------------------------------------------------

        if (sGoCoderSDK != null) {
            /*
            Packet change listener setup
             */
            final PlayerActivity activity = this;
            packetChangeListener = new WOWZPlayerView.PacketThresholdChangeListener() {
                @Override
                public void packetsBelowMinimumThreshold(int packetCount) {
                    WOWZLog.debug("Packets have fallen below threshold "+packetCount+"... ");

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(activity, "Packets have fallen below threshold ... ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void packetsAboveMinimumThreshold(int packetCount) {
                    WOWZLog.debug("Packets have risen above threshold "+packetCount+" ... ");

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(activity, "Packets have risen above threshold ... ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            mStreamPlayerView.setShowAllNotificationsWhenBelowThreshold(false);
            mStreamPlayerView.setMinimumPacketThreshold(20);
            mStreamPlayerView.registerPacketThresholdListener(packetChangeListener);
            ///// End packet change notification listener

            mTimerView.setTimerProvider(new TimerView.TimerProvider() {
                @Override
                public long getTimecode() {
                    return mStreamPlayerView.getCurrentTime();
                }

                @Override
                public long getDuration() {
                    return mStreamPlayerView.getDuration();
                }
            });

            mSeekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mStreamPlayerView != null && mStreamPlayerView.isPlaying()) {
                        mStreamPlayerView.setVolume(progress);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            // listen for volume changes from device buttons, etc. 볼륨 조절
            mVolumeSettingChangeObserver = new VolumeChangeObserver(this, new Handler());
            getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeSettingChangeObserver);
            mVolumeSettingChangeObserver.setVolumeChangeListener(new VolumeChangeObserver.VolumeChangeListener() {
                @Override
                public void onVolumeChanged(int previousLevel, int currentLevel) {
                    if (mSeekVolume != null)
                        mSeekVolume.setProgress(currentLevel);

                    if (mStreamPlayerView != null && mStreamPlayerView.isPlaying()) {
                        mStreamPlayerView.setVolume(currentLevel);
                    }
                }
            });

            mBtnScale.setState(mStreamPlayerView.getScaleMode() == WOWZMediaConfig.FILL_VIEW);

            // The streaming player configuration properties
            mStreamPlayerConfig = new WOWZPlayerConfig();

            mBufferingDialog = new ProgressDialog(this);
            mBufferingDialog.setTitle(R.string.status_buffering);
            mBufferingDialog.setMessage(getResources().getString(R.string.msg_please_wait));
            mBufferingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    cancelBuffering(dialogInterface);
                }
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onMetaData", new WOWZDataEvent.EventListener(){
                @Override
                public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                    String meta = "";
                    if(eventParams!=null)
                        meta = eventParams.toString();


                    WOWZLog.debug("onWZDataEvent -> eventName "+eventName+" = "+meta);

                    return null;
                }
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onStatus", new WOWZDataEvent.EventListener(){
                @Override
                public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                    if(eventParams!=null)
                        WOWZLog.debug("onWZDataEvent -> eventName "+eventName+" = "+eventParams.toString());

                    return null;
                }
            });

            // testing player data event handler.
            mStreamPlayerView.registerDataEventListener("onTextData", new WOWZDataEvent.EventListener(){
                @Override
                public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                    if(eventParams!=null)
                        WOWZLog.debug("onWZDataEvent -> "+eventName+" = "+eventParams.get("text"));

                    return null;
                }
            });
        } else {
            mHelp.setVisibility(View.GONE);
            mStatusView.setErrorMessage(WowzaGoCoder.getLastError().getErrorDescription());
        }

//-------------------------------------------------------------------------

        Msgedt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



    //-------------------------------------------------------tcp IP 스레드 ------------------------------------------------
    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) { //연결이 안되어 있을때 socketchannel.read(buf)값은 0이다 -1은 데이터가 끝어진경우 0 초과는 리시브된 데이터가 있는 경우
                    throw new IOException();
                }
                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();
                Log.d("receive", "msg :" + data);
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Runnable showUpdate = new Runnable() { // 서버에서 메세지를 받는 메소드
        public void run() {
            String receive = "" + data;
            Log.i("receive값", ""+receive);
            array = receive.split("!@#&");

            mFormat = new SimpleDateFormat("a hh:mm");
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);

            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);
            if(array[0].equals(chatID)==true){
                Log.i("chatID값", ""+array[0]);
                Log.i("보낸 유저 email array1", ""+array[1]);
                Log.i("보낸 유저 이름 array2", ""+array[2]);
                Log.i("보낸 메세지", ""+array[4]);
                Log.i("메세지 상태값", ""+array[5]);
                if(array[5].equals("0")==true){
                    Log.i("일반 메세지 값 0", ""+array[5]);
                    if(array[1].equals(userEmail)==false){
                        Log.i("보낸유저 email값 11", ""+array[1]);
                        Log.i("받는 유저 email값 11", ""+userEmail);
                        // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                        chat.add(new Item_Chatroom(chatID,array[1],array[2],array[3],array[4],1,mFormat.format(mDate),NumberOfPeople-2,true));
                        Log.i("메세지 받았을때 Numberofpeople", ""+NumberOfPeople);
                        dbHelper.insert(chatID,array[1],array[2],array[3],array[4],1,mFormat.format(mDate),NumberOfPeople-2,0); // 1일때 거짓 0일때 참
                        Log.i("insert값1111", "1111");
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[4]+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);//

                        mAdapter = new Adapter_PlayerActivity(chat);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(array[5].equals("1")==true){
                    Log.i("상태 메세지값 1", ""+array[5]);
                    if(array[1].equals(userEmail)==false) { //해당방 ID값이고 다른 유저가 방을 나가거나 초대 받았을때
                        Log.i("보낸유저 email값 22", ""+array[1]);
                        Log.i("받는 유저 email값 22", ""+userEmail);
                        if (array[3].equals("나감") == true) { // 유저가 나갔을때 room_no 값을 변경
                            Log.i("array[3]값이 '나감'인가???", ""+array[3]);
                            final String changechatID = chatID.replace(" "+array[2],"");
                            chat.add(new Item_Chatroom(chatID, array[1], array[2], "",array[2]+"님이 나갔습니다", 2, mFormat.format(mDate), 0, true));
                            final String changeroomname = roomname.replace(array[2],"");//나간유저 방이름에서 삭제
                            dbHelper.chatID_update(chatID,changechatID); // sqlite DB에 변경된 chatID값을 변경함

                            chatID = changechatID;// 변경된 방ID값

                            dbHelper.insert(chatID, array[1], array[2], " ", array[2]+"님이 나갔습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장
                            Log.i("insert값2222", "2222");
                            mAdapter = new Adapter_PlayerActivity(chat);
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean success = jsonResponse.getBoolean("success");
                                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople"); //채팅방 인원수 서버에서 가져와 동기화

                                        if (success) {
                                            roomname = changeroomname; // 방이름 변경
                                            NumberOfPeople = NumberOfPeople - 1;
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            };//responseListener 끝
                            nFormat = new SimpleDateFormat("MM/dd a hh:mm");
                            nNow = System.currentTimeMillis();
                            nDate = new Date(nNow);

                            ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, array[4], nFormat.format(nDate), responseListener);
                            RequestQueue queue = Volley.newRequestQueue(PlayerActivity.this);
                            queue.add(chatRoomMsgUpdateRequest);
                        }
                        else if (array[3].equals("초대")==true){
                            Log.i("array[3]값이 '초대'인가???", ""+array[3]);

                            dbHelper.chatID_update(chatID,array[4]); //array[4]에 변경된 chatID값을 넣음 그후 변경된 chatID값을 db에도 chatID값 변경
                            chatID = array[4];

                            chat.add(new Item_Chatroom(chatID,array[1],array[2],"",array[2]+"님을 초대 하였습니다",2,mFormat.format(mDate),0,true));

                            dbHelper.insert(chatID,array[1],array[2]," ",array[2]+"님을 초대 하였습니다",2,mFormat.format(mDate),0,0);// db에 메세지 저장
                            Log.i("insert값3333", "3333");
                            roomname = roomname +" "+array[2];// 방이름 변경
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.i("서버값", ""+response);
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean success = jsonResponse.getBoolean("success");
                                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople");//채팅방 인원수 서버에서 가져와 동기화
                                        if (success) {

                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            };//responseListener 끝
                            nFormat = new SimpleDateFormat("MM/dd a hh:mm");
                            nNow = System.currentTimeMillis();
                            nDate = new Date(nNow);

                            mAdapter = new Adapter_PlayerActivity(chat);
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                            ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, array[2]+"님을 초대 하였습니다", nFormat.format(nDate), responseListener);
                            RequestQueue queue = Volley.newRequestQueue(PlayerActivity.this);
                            queue.add(chatRoomMsgUpdateRequest);
                        }
                    }
                }
                else if(array[5].equals("2")==true){// 해당방 ID값이고 다른 유저가 보낸 이미지 일때
                    Log.i("이미지 메세지 값 2", ""+array[5]);
                    if(array[1].equals(userEmail)==false){
                        Log.i("보낸유저 email값 33", ""+array[1]);
                        Log.i("받는 유저 email값 33", ""+userEmail);
                        chat.add(new Item_Chatroom(chatID, array[1],array[2],array[3],array[4],4,mFormat.format(mDate),NumberOfPeople-2 ,true));
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        dbHelper.insert(chatID, array[1],array[2],array[3],array[4],4,mFormat.format(mDate),NumberOfPeople-2,0);// 1일때 거짓 0일때 참
                        Log.i("insert값4444", "4444");
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[4]+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다

                        delaymessage(returnmsg);

                        mAdapter = new Adapter_PlayerActivity(chat);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if(array[5].equals("3")==true){ // 읽음 처리
                    if(array[1].equals(userEmail)==false){
                        Log.i("누가 읽었는지 확인해주기", ""+array[1]);
                        for(int i = 0; i<chat.size(); i++){
                            if(chat.get(i).Read > 0) { // 읽음 표시 카운터 수가 0이상일 경우
                                Log.i("해당 메세지 read값", ""+chat.get(i).Read);
                                chat.set(i, new Item_Chatroom(chatID, chat.get(i).friendEmail, chat.get(i).friend, chat.get(i).friendimg, chat.get(i).chat, chat.get(i).who, chat.get(i).time, chat.get(i).Read - 1, chat.get(i).Readcheck));
                                Log.i("해당 메세지 변화한 read값", ""+chat.get(i).Read);
                                dbHelper.readupdate(chatID, chat.get(i).Read); // 해당 메세지를 읽었다고 보낸 메세지를 받아서 읽음 표시 카운터를 -1한뒤 표시함
                            }
                        }
                    }
                }
                else if(array[5].equals("4")==true){ // 방송 시작
                    if(array[1].equals(userEmail)==false){
                        // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                        chat.add(new Item_Chatroom(chatID,array[1],array[2],array[3],array[2]+"님이 방송을 시작하였습니다",5,mFormat.format(mDate),NumberOfPeople-2,true));
                        dbHelper.insert(chatID, array[1],array[2],array[3],array[2]+"님이 방송을 시작하였습니다 ",5,mFormat.format(mDate),NumberOfPeople-2,0);// 1일때 거짓 0일때 참
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[4]+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);
                    }
                }
                else if(array[5].equals("5")==true){ //방송 종료
                    if(array[1].equals(userEmail)==false){
                        // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                        chat.add(new Item_Chatroom(chatID,array[1],array[2],array[3],array[2]+"님이 방송을 종료하였습니다",6,mFormat.format(mDate),NumberOfPeople-2,true));
                        dbHelper.insert(chatID, array[1],array[2],array[3],array[2]+"님이 방송을 종료하였습니다",6,mFormat.format(mDate),NumberOfPeople-2,0);// 1일때 거짓 0일때 참
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[2]+"님이 방송을 종료하였습니다"+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);
                    }
                }
                else if(array[5].equals("7")==true){// CameraActivity에서 viewer수를 보낸것을 받아서 시청자수 표시
                    Viewers.setText(array[4]);
                }
            }
            mAdapter = new Adapter_PlayerActivity(chat);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chat.size()-1); // 리사이클러뷰 포지셔닝 밑으로
        }
    };

    //---------------------------------------------------------------------------------------------------------- 채팅 소켓연결 -------------------------------------------------

    @Override
    protected void onDestroy() {
        if (mVolumeSettingChangeObserver != null)
            getApplicationContext().getContentResolver().unregisterContentObserver(mVolumeSettingChangeObserver);

        super.onDestroy();
    }

    /**
     * Android Activity class methods
     */

    @Override
    protected void onResume() {
        super.onResume();

        syncUIControlState();
        startLoading();
    }

    @Override
    protected void onPause() {
        if (mStreamPlayerView != null && mStreamPlayerView.isPlaying()) {
            mStreamPlayerView.stop();

            // Wait for the streaming player to disconnect and shutdown...
            mStreamPlayerView.getCurrentStatus().waitForState(WOWZState.IDLE);
        }

        super.onPause();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean isPlayerConfigReady()
    {

        return false;
    }

    /*
    Click handler for network pausing
     */
    public void onPauseNetwork(View v)
    {
        Button btn = (Button)findViewById(R.id.pause_network);
        if(btn.getText().toString().trim().equalsIgnoreCase("pause network")) {
            WOWZLog.info("Pausing network...");
            btn.setText("Unpause Network");
            mStreamPlayerView.pauseNetworkStack();
        }
        else{
            WOWZLog.info("Unpausing network... btn.getText(): "+btn.getText());
            btn.setText("Pause Network");
            mStreamPlayerView.unpauseNetworkStack();
        }
    }

    /**
     * Click handler for the playback button
     */
    // 방송 재생 버튼 눌렀을때
    public void onTogglePlayStream(View v) {

        if (mStreamPlayerView.isPlaying()) {
            mStreamPlayerView.stop();
        } else if (mStreamPlayerView.isReadyToPlay()) {
            if(!this.isNetworkAvailable()){
                displayErrorDialog("No internet connection, please try again later.");
                return;
            }

            mHelp.setVisibility(View.GONE);
            WOWZStreamingError configValidationError = mStreamPlayerConfig.validateForPlayback();
            if (configValidationError != null) {
                mStatusView.setErrorMessage(configValidationError.getErrorDescription());
            } else {
                // Set the detail level for network logging output
                mStreamPlayerView.setLogLevel(mWZNetworkLogLevel);

                // Set the player's pre-buffer duration as stored in the app prefs
                float preBufferDuration = GoCoderSDKPrefs.getPreBufferDuration(PreferenceManager.getDefaultSharedPreferences(this));

                mStreamPlayerConfig.setPreRollBufferDuration(preBufferDuration);

                // Start playback of the live stream
                mStreamPlayerView.play(mStreamPlayerConfig, this);
            }
        }
    }

    /**
     * WOWZStatusCallback interface methods
     */
    @Override
    public synchronized void onWZStatus(WOWZStatus status) {
        final WOWZStatus playerStatus = new WOWZStatus(status);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                WOWZStatus status = new WOWZStatus(playerStatus.getState());
                switch(playerStatus.getState()) {

                    case WOWZPlayerView.STATE_PLAYING:
                        // Keep the screen on while we are playing back the stream
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (mStreamPlayerConfig.getPreRollBufferDuration() == 0f) {
                            mTimerView.startTimer();
                        }

                        // Since we have successfully opened up the server connection, store the connection info for auto complete

                        GoCoderSDKPrefs.storeHostConfig(PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this), mStreamPlayerConfig);

                        // Log the stream metadata
                        WOWZLog.debug(TAG, "Stream metadata:\n" + mStreamPlayerView.getMetadata());
                        break;

                    case WOWZPlayerView.STATE_READY_TO_PLAY:
                        // Clear the "keep screen on" flag
                        WOWZLog.debug(TAG, "STATE_READY_TO_PLAY player activity status!");
                        if(playerStatus.getLastError()!=null)
                            displayErrorDialog(playerStatus.getLastError());

                        playerStatus.clearLastError();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        mTimerView.stopTimer(); // 타이머 멈출때 방송도 종료
                        Viewers.setText("0");
                        Toast.makeText(PlayerActivity.this,"방송 종료하였습니다",Toast.LENGTH_SHORT).show();
                        String a = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+"방송종료"+"!@#&"+"8"+"!@#&";
                        new PlayerActivity.SendmsgTask2().execute(a);
                        endstreaming();
                        break;

                    case WOWZPlayerView.STATE_PREBUFFERING_STARTED:
                        WOWZLog.debug(TAG, "Dialog for buffering should show...");
                        showBuffering();
                        break;

                    case WOWZPlayerView.STATE_PREBUFFERING_ENDED:
                        WOWZLog.debug(TAG, "Dialog for buffering should stop...");
                        hideBuffering();
                        // Make sure player wasn't signaled to shutdown
                        if (mStreamPlayerView.isPlaying()) {
                            mTimerView.startTimer();
                        }
                        break;

                    default:
                        break;
                }
                syncUIControlState();
            }
        });
    }

    @Override
    public synchronized void onWZError(final WOWZStatus playerStatus) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                displayErrorDialog(playerStatus.getLastError());
                syncUIControlState();
            }
        });
    }

    /**
     * Click handler for the mic/mute button
     */
    public void onToggleMute(View v) {
        mBtnMic.toggleState();

        if (mStreamPlayerView != null)
            mStreamPlayerView.mute(!mBtnMic.isOn());

        mSeekVolume.setEnabled(mBtnMic.isOn());
    }

    public void onToggleScaleMode(View v) { // 화면 크기 조절
        int newScaleMode = mStreamPlayerView.getScaleMode() ==
                WOWZMediaConfig.RESIZE_TO_ASPECT ? WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mBtnScale.setState(newScaleMode == WOWZMediaConfig.FILL_VIEW);
        mStreamPlayerView.setScaleMode(newScaleMode);
    }

    /**
     * Click handler for the metadata button
     */
    public void onStreamMetadata(View v) {
        WOWZDataMap streamMetadata = mStreamPlayerView.getMetadata();
        WOWZDataMap streamStats = mStreamPlayerView.getStreamStats();
//        WOWZDataMap streamConfig = mStreamPlayerView.getStreamConfig().toDataMap();
        WOWZDataMap streamConfig = new WOWZDataMap();
        WOWZDataMap streamInfo = new WOWZDataMap();

        streamInfo.put("- Stream Statistics -", streamStats);
        streamInfo.put("- Stream Metadata -", streamMetadata);
        //streamInfo.put("- Stream Configuration -", streamConfig);

        DataTableFragment dataTableFragment = DataTableFragment.newInstance("Stream Information", streamInfo, false, false);

        // Display/hide the data table fragment
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, dataTableFragment)
                .addToBackStack("metadata_fragment")
                .commit();
    }

    /**
     * Click handler for the settings button
     */
    public void onSettings(View v) {
        // Display the prefs fragment
        GoCoderSDKPrefs.PrefsFragment prefsFragment = new GoCoderSDKPrefs.PrefsFragment();
        prefsFragment.setFixedSource(true);
        prefsFragment.setForPlayback(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Update the state of the UI controls
     */
    private void syncUIControlState() {
        boolean disableControls = (!(mStreamPlayerView.isReadyToPlay() || mStreamPlayerView.isPlaying()) || sGoCoderSDK == null);
        if (disableControls) {
            mBtnPlayStream.setEnabled(false);
            mBtnSettings.setEnabled(false);
            mSeekVolume.setEnabled(false);
            mBtnScale.setEnabled(false);
            mBtnMic.setEnabled(false);
            mStreamMetadata.setEnabled(false);
        } else {
            mBtnPlayStream.setState(mStreamPlayerView.isPlaying());
            mBtnPlayStream.setEnabled(true);

            if (mStreamPlayerConfig.isAudioEnabled()) {
                mBtnMic.setVisibility(View.VISIBLE);
                mBtnMic.setEnabled(true);

                mSeekVolume.setVisibility(View.VISIBLE);
                mSeekVolume.setEnabled(mBtnMic.isOn());
                mSeekVolume.setProgress(mStreamPlayerView.getVolume());
            } else {
                mSeekVolume.setVisibility(View.GONE);
                mBtnMic.setVisibility(View.GONE);
            }

            mBtnScale.setVisibility(View.VISIBLE);
            mBtnScale.setVisibility(mStreamPlayerView.isPlaying() && mStreamPlayerConfig.isVideoEnabled() ? View.VISIBLE : View.GONE);
            mBtnScale.setEnabled(mStreamPlayerView.isPlaying() && mStreamPlayerConfig.isVideoEnabled());

            mBtnSettings.setEnabled(!mStreamPlayerView.isPlaying());
            mBtnSettings.setVisibility(mStreamPlayerView.isPlaying() ? View.GONE : View.VISIBLE);

            mStreamMetadata.setEnabled(mStreamPlayerView.isPlaying());
            mStreamMetadata.setVisibility(mStreamPlayerView.isPlaying() ? View.VISIBLE : View.GONE);
        }
    }

    private void showBuffering() {
        try {
            if (mBufferingDialog == null) return;
            mBufferingDialog.show();
        }
        catch(Exception ex){}
    }

    private void cancelBuffering(DialogInterface dialogInterface) {
        if(mStreamPlayerConfig.getHLSBackupURL()!=null || mStreamPlayerConfig.isHLSEnabled()){
            mStreamPlayerView.stop(true);
        }
        else if (mStreamPlayerView != null && mStreamPlayerView.isPlaying()) {
            mStreamPlayerView.stop(true);
        }
    }

    private void hideBuffering() {
        if (mBufferingDialog.isShowing())
            mBufferingDialog.dismiss();
    }

    @Override
    public void syncPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mWZNetworkLogLevel = Integer.valueOf(prefs.getString("wz_debug_net_log_level", String.valueOf(WOWZLog.LOG_LEVEL_DEBUG)));

        mStreamPlayerConfig.setIsPlayback(true);
        if (mStreamPlayerConfig != null)
            GoCoderSDKPrefs.updateConfigFromPrefsForPlayer(prefs, mStreamPlayerConfig);
    }

    // 시작할때 자동으로 스트리밍을 재생
    public void startLoading(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBtnPlayStream.performClick();
            }
        },1000);
    }

    public void delaymessage(final String a){ // 메세지 받고 1초뒤에 읽었다고 확인 메세지 보내기
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    new PlayerActivity.SendmsgTask2().execute(a);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }

    public void endstreaming(){ // 스트리밍 종료 후 PlayerActivity 나가기
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    socketChannel.close();
                    Intent intent = new Intent(PlayerActivity.this,Chatroom.class);
                    intent.putExtra("방ID",chatID);
                    intent.putExtra("방이름",roomname);
                    startActivity(intent);
                    finish();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    private class SendmsgTask1 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() { // 보내기전 텍스트 메세지 비우기
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Msgedt.setText("");
                }
            });
        }
    }

    private class SendmsgTask2 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    public void onBackPressed() { // 뒤로가기 눌렷을때
        super.onBackPressed();
        try {
            String a = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+"방송종료"+"!@#&"+"8"+"!@#&";
            new PlayerActivity.SendmsgTask2().execute(a);
            endstreaming(); // 1초뒤 방송 종료
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
