package com.example.sjb.talk_talk.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import com.example.sjb.talk_talk.ui.AutoFocusListener;
import com.example.sjb.talk_talk.ui.MultiStateButton;
import com.example.sjb.talk_talk.ui.TimerView;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraActivity extends CameraActivityBase {
    private final static String TAG = CameraActivity.class.getSimpleName();

    // UI controls
    protected MultiStateButton mBtnSwitchCamera = null;
    protected MultiStateButton mBtnTorch = null;
    protected TimerView mTimerView = null;

    // Gestures are used to toggle the focus modes
    protected GestureDetectorCompat mAutoFocusDetector = null;


    String UPPER_ALPHA_DIGITS = "ACEFGHJKLMNPQRUVWXY123456789";

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    String userEmail, RoomName;
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

    String RandomRoomID; // 스트리밍 방 ID값
    SocketChannel socketChannel;

    SimpleDateFormat nFormat;
    long nNow;
    Date nDate;

    ArrayList<Item_Chatroom> chat = new ArrayList<>();
    Adapter_CameraActivity mAdapter;

    Button Msgbtn; // 채팅 입력 버튼
    EditText Msgedt; // 채팅 에디트 텍스트
    TextView viewers; // 인원수 나타내는 텍스트

    int viewer; // 시청자 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mRequiredPermissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        // Initialize the UI controls
        mBtnTorch = (MultiStateButton) findViewById(R.id.ic_torch);
        mBtnSwitchCamera = (MultiStateButton) findViewById(R.id.ic_switch_camera);
        mTimerView = (TimerView) findViewById(R.id.txtTimer);

        Msgbtn = (Button) findViewById(R.id.StreamSendMsgBtn2);
        Msgedt = (EditText) findViewById(R.id.StreamMsgEditText2);
        viewers = (TextView) findViewById(R.id.StreamerViewers);

        //--------------------------------- 리사이클러뷰 ------------------------------------------
        recyclerView = (RecyclerView) findViewById(R.id.StreamerChat);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // recyclerview
        dbHelper = new DBHelper(getApplicationContext(), "Chatroom_record.db", null, 1);

        SharedPreferences sp = getSharedPreferences("login_user", MODE_PRIVATE);
        final SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        userName = sp.getString("user_Name", "");
        userEmail = sp.getString("user_Email", "");
        userGoogle = sp.getString("google", "");

        Intent intent = new Intent(this.getIntent());
        RoomName = intent.getStringExtra("방이름");
        chatID = intent.getStringExtra("방ID");
        RandomRoomID = randomString(7, UPPER_ALPHA_DIGITS);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putString("wz_live_port_number", String.valueOf(hostConfig.getPortNumber()));
        editor.putString("wz_live_host_address",StreamCloudServer);
        editor.putString("wz_live_app_name", ApplicationName);
        editor.putString("wz_live_stream_name", RandomRoomID);
        editor.putString("wz_live_username", SourceUsername);
        editor.putString("wz_live_password", SourcePassword);
        editor.apply();
        // 스트리밍 방ID와 서버, 앱, 유저이름. 비밀번호 설정 저장

        viewer = 1; // 시청자수 1에서 시작 자신 포함해서 1인것

        String sviewer = String.valueOf(viewer);

        viewers.setText(sviewer);

        viewer = Integer.parseInt(viewers.getText().toString()); // 인원수

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                Log.i("서버값", "" + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    //서버에서 보내준 값이 true이면?
                    if (success) {
                        img_url = jsonResponse.getString("profile"); // 프로필 사진 이미지 주소값
                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople");  // 방에 존재하는 인원수
                        Log.i("인원수 체크~~!!!", "" + NumberOfPeople);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };//responseListener 끝
        MyImgAndPersonnelCheckInRoomRequest session_request = new MyImgAndPersonnelCheckInRoomRequest(chatID, userEmail, userName, userGoogle, responseListener);
        RequestQueue queue = Volley.newRequestQueue(CameraActivity.this);
        queue.add(session_request);

        //------------------------------------------------------------------------------------------------------------------------

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));

                    Log.i("socketchannel 연결된값", "" + socketChannel);
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + " a");
                    ioe.printStackTrace();
                }
                checkUpdate.start();
            }
        }).start();

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
                    mAdapter = new Adapter_CameraActivity(chat);
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
                                        new CameraActivity.SendmsgTask1().execute(sendok);
                                        Log.i("일반 메세지 보내는 값", ""+sendok);
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };//responseListener 끝
                        ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, return_msg, nFormat.format(nDate), responseListener);
                        RequestQueue queue = Volley.newRequestQueue(CameraActivity.this);
                        queue.add(chatRoomMsgUpdateRequest);
                    }

                } catch (Exception e) {
                    Log.i("button 쪽 에러", ""+e);
                    e.printStackTrace();
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------------------------

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
            Log.i("receive값", "" + receive);
            array = receive.split("!@#&");

            mFormat = new SimpleDateFormat("a hh:mm");
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);
            if (array[0].equals(chatID) == true) {
                Log.i("chatID값", "" + array[0]);
                Log.i("보낸 유저 email array1", "" + array[1]);
                Log.i("보낸 유저 이름 array2", "" + array[2]);
                Log.i("보낸 메세지", "" + array[4]);
                Log.i("메세지 상태값", "" + array[5]);
                if (array[5].equals("0") == true) {
                    Log.i("일반 메세지 값 0", "" + array[5]);
                    if (array[1].equals(userEmail) == false) {
                        Log.i("보낸유저 email값 11", "" + array[1]);
                        Log.i("받는 유저 email값 11", "" + userEmail);
                        // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                        chat.add(new Item_Chatroom(chatID, array[1], array[2], array[3], array[4], 1, mFormat.format(mDate), NumberOfPeople - 2, true));
                        Log.i("메세지 받았을때 Numberofpeople", "" + NumberOfPeople);
                        dbHelper.insert(chatID, array[1], array[2], array[3], array[4], 1, mFormat.format(mDate), NumberOfPeople - 2, 0); // 1일때 거짓 0일때 참
                        Log.i("insert값1111", "1111");
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID + "!@#&" + userEmail + "!@#&" + userName + "!@#&" + img_url + "!@#&" + array[4] + "!@#&" + "3" + "!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);//

                        mAdapter = new Adapter_CameraActivity(chat);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                } else if (array[5].equals("1") == true) {
                    Log.i("상태 메세지값 1", "" + array[5]);
                    if (array[1].equals(userEmail) == false) { //해당방 ID값이고 다른 유저가 방을 나가거나 초대 받았을때
                        Log.i("보낸유저 email값 22", "" + array[1]);
                        Log.i("받는 유저 email값 22", "" + userEmail);
                        if (array[3].equals("나감") == true) { // 유저가 나갔을때 room_no 값을 변경
                            Log.i("array[3]값이 '나감'인가???", "" + array[3]);
                            final String changechatID = chatID.replace(" " + array[2], "");
                            chat.add(new Item_Chatroom(chatID, array[1], array[2], "", array[2] + "님이 나갔습니다", 2, mFormat.format(mDate), 0, true));
                            final String changeroomname = RoomName.replace(array[2], "");//나간유저 방이름에서 삭제
                            dbHelper.chatID_update(chatID, changechatID); // sqlite DB에 변경된 chatID값을 변경함

                            chatID = changechatID;// 변경된 방ID값

                            dbHelper.insert(chatID, array[1], array[2], " ", array[2] + "님이 나갔습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장
                            Log.i("insert값2222", "2222");
                            mAdapter = new Adapter_CameraActivity(chat);
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
                                            RoomName = changeroomname; // 방이름 변경
                                            NumberOfPeople = NumberOfPeople - 1;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };//responseListener 끝
                            nFormat = new SimpleDateFormat("MM/dd a hh:mm");
                            nNow = System.currentTimeMillis();
                            nDate = new Date(nNow);

                            ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, array[4], nFormat.format(nDate), responseListener);
                            RequestQueue queue = Volley.newRequestQueue(CameraActivity.this);
                            queue.add(chatRoomMsgUpdateRequest);
                        } else if (array[3].equals("초대") == true) {
                            Log.i("array[3]값이 '초대'인가???", "" + array[3]);

                            dbHelper.chatID_update(chatID, array[4]); //array[4]에 변경된 chatID값을 넣음 그후 변경된 chatID값을 db에도 chatID값 변경
                            chatID = array[4];

                            chat.add(new Item_Chatroom(chatID, array[1], array[2], "", array[2] + "님을 초대 하였습니다", 2, mFormat.format(mDate), 0, true));

                            dbHelper.insert(chatID, array[1], array[2], " ", array[2] + "님을 초대 하였습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장
                            Log.i("insert값3333", "3333");
                            RoomName = RoomName + " " + array[2];// 방이름 변경
                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.i("서버값", "" + response);
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        boolean success = jsonResponse.getBoolean("success");
                                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople");//채팅방 인원수 서버에서 가져와 동기화
                                        if (success) {

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };//responseListener 끝
                            nFormat = new SimpleDateFormat("MM/dd a hh:mm");
                            nNow = System.currentTimeMillis();
                            nDate = new Date(nNow);

                            mAdapter = new Adapter_CameraActivity(chat);
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                            ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, array[2] + "님을 초대 하였습니다", nFormat.format(nDate), responseListener);
                            RequestQueue queue = Volley.newRequestQueue(CameraActivity.this);
                            queue.add(chatRoomMsgUpdateRequest);
                        }
                    }
                } else if (array[5].equals("2") == true) {// 해당방 ID값이고 다른 유저가 보낸 이미지 일때
                    Log.i("이미지 메세지 값 2", "" + array[5]);
                    if (array[1].equals(userEmail) == false) {
                        Log.i("보낸유저 email값 33", "" + array[1]);
                        Log.i("받는 유저 email값 33", "" + userEmail);
                        chat.add(new Item_Chatroom(chatID, array[1], array[2], array[3], array[4], 4, mFormat.format(mDate), NumberOfPeople - 2, true));
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        dbHelper.insert(chatID, array[1], array[2], array[3], array[4], 4, mFormat.format(mDate), NumberOfPeople - 2, 0);// 1일때 거짓 0일때 참
                        Log.i("insert값4444", "4444");
                        String returnmsg = chatID + "!@#&" + userEmail + "!@#&" + userName + "!@#&" + img_url + "!@#&" + array[4] + "!@#&" + "3" + "!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다

                        delaymessage(returnmsg);

                        mAdapter = new Adapter_CameraActivity(chat);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else if (array[5].equals("3") == true) { // 읽음 처리
                    if (array[1].equals(userEmail) == false) {
                        Log.i("누가 읽었는지 확인해주기", "" + array[1]);
                        for (int i = 0; i < chat.size(); i++) {
                            if (chat.get(i).Read > 0) { // 읽음 표시 카운터 수가 0이상일 경우
                                Log.i("해당 메세지 read값", "" + chat.get(i).Read);
                                chat.set(i, new Item_Chatroom(chatID, chat.get(i).friendEmail, chat.get(i).friend, chat.get(i).friendimg, chat.get(i).chat, chat.get(i).who, chat.get(i).time, chat.get(i).Read - 1, chat.get(i).Readcheck));
                                Log.i("해당 메세지 변화한 read값", "" + chat.get(i).Read);
                                dbHelper.readupdate(chatID, chat.get(i).Read); // 해당 메세지를 읽었다고 보낸 메세지를 받아서 읽음 표시 카운터를 -1한뒤 표시함
                            }
                        }
                    }
                }
                else if(array[5].equals("6")== true){ // 시청자수 +1 증가
                    Log.i(TAG+"시청자수에 들어왓는가", "들어왔는가?");
                    viewer = viewer+1;
                    Log.i(TAG+"viewer수", ""+viewer);
                    String sviewer = String.valueOf(viewer);
                    viewers.setText(sviewer);
                    String view = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+sviewer+"!@#&"+"7"+"!@#&";// viewer은 시청자수
                    new CameraActivity.SendmsgTask2().execute(view);
                }
                else if(array[5].equals("8")== true){ // 시청자수 -1 감소
                    viewer = viewer-1;
                    Log.i(TAG+"viewer수", ""+viewer);
                    String sviewer = String.valueOf(viewer);
                    viewers.setText(sviewer);
                    String view = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+sviewer+"!@#&"+"7"+"!@#&";// viewer은 시청자수
                    new CameraActivity.SendmsgTask2().execute(view);
                }

            }
            mAdapter = new Adapter_CameraActivity(chat);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chat.size()-1); // 리사이클러뷰 포지셔닝 밑으로

        }
    };

    //---------------------------------------------------------------------------------------------------------- 채팅 소켓연결 -------------------------------------------------

    /**
     * Android Activity lifecycle methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasDevicePermissionToAccess() && sGoCoderSDK != null && mWZCameraView != null) {
            if (mAutoFocusDetector == null)
                mAutoFocusDetector = new GestureDetectorCompat(this, new AutoFocusListener(this, mWZCameraView));

            WOWZCamera activeCamera = mWZCameraView.getCamera();
            if (activeCamera != null && activeCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
                activeCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Click handler for the switch camera button
     */
    public void onSwitchCamera(View v) {
        if (mWZCameraView == null) return;

        mBtnTorch.setState(false);
        mBtnTorch.setEnabled(false);

        // Set the new surface extension prior to camera switch such that
        // setting will take place with the new one.  So if it is currently the front
        // camera, then switch to default setting (not mirrored).  Otherwise show mirrored.

        WOWZCamera newCamera = mWZCameraView.switchCamera();
        if (newCamera != null) {
            if (newCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
                newCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);

            boolean hasTorch = newCamera.hasCapability(WOWZCamera.TORCH);
            if (hasTorch) {
                mBtnTorch.setState(newCamera.isTorchOn());
                mBtnTorch.setEnabled(true);
            }
        }
    }


    /**
     * Click handler for the torch/flashlight button
     */
    public void onToggleTorch(View v) {
        if (mWZCameraView == null) return;

        WOWZCamera activeCamera = mWZCameraView.getCamera();
        activeCamera.setTorchOn(mBtnTorch.toggleState());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAutoFocusDetector != null)
            mAutoFocusDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    /**
     * Update the state of the UI controls
     */
    @Override
    protected boolean syncUIControlState() {
        mFormat = new SimpleDateFormat("a hh:mm");
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        boolean disableControls = super.syncUIControlState();

        if (disableControls) {
            mBtnSwitchCamera.setEnabled(false);
            mBtnTorch.setEnabled(false);
        } else {
            boolean isDisplayingVideo = (this.hasDevicePermissionToAccess(Manifest.permission.CAMERA) && getBroadcastConfig().isVideoEnabled() && mWZCameraView.getCameras().length > 0);
            boolean isStreaming = getBroadcast().getStatus().isRunning();

            if (isDisplayingVideo) {
                WOWZCamera activeCamera = mWZCameraView.getCamera();

                boolean hasTorch = (activeCamera != null && activeCamera.hasCapability(WOWZCamera.TORCH));
                mBtnTorch.setEnabled(hasTorch);
                if (hasTorch) {
                    mBtnTorch.setState(activeCamera.isTorchOn());
                }

                mBtnSwitchCamera.setEnabled(mWZCameraView.getCameras().length > 0);
            } else {
                mBtnSwitchCamera.setEnabled(false);
                mBtnTorch.setEnabled(false);
            }

            if (isStreaming && !mTimerView.isRunning()) { // 스트리밍 시작
                mTimerView.startTimer();
                dbHelper.insert(chatID,userEmail,userName,img_url,userName+"님이 방송을 시작하였습니다",0,mFormat.format(mDate),NumberOfPeople-1,0);
                String streaming = chatID + "!@#&" + userEmail + "!@#&" + userName + "!@#&" + img_url + "!@#&" + RandomRoomID + "!@#&" + "4" + "!@#&";
                new SendmsgTask1().execute(streaming); // 스트리밍 시작 메세지 보내기
            } else if (getBroadcast().getStatus().isIdle() && mTimerView.isRunning()) {
                mTimerView.stopTimer();
                dbHelper.insert(chatID,userEmail,userName,img_url,userName+"님이 방송을 종료하였습니다",0,mFormat.format(mDate),NumberOfPeople-1,0);
                String streaming = chatID + "!@#&" + userEmail + "!@#&" + userName + "!@#&" + img_url + "!@#&" + RandomRoomID + "!@#&" + "5" + "!@#&";
                new SendmsgTask1().execute(streaming); // 스트리밍 끝 메세지 보내기
                viewers.setText("1");
            } else if (!isStreaming) { // 스트리밍 끝
                mTimerView.setVisibility(View.GONE);
            }
        }
        return disableControls;
    }

    public void delaymessage(final String a) { // 메세지 받고 1초뒤에 읽었다고 확인 메세지 보내기
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    new CameraActivity.SendmsgTask2().execute(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
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
    private String randomString(int length, String characterSet) { // 스트리밍 방 ID값으로 이용
        StringBuilder sb = new StringBuilder(); //consider using StringBuffer if needed
        for (int i = 0; i < length; i++) {
            int randomInt = new SecureRandom().nextInt(characterSet.length());
            sb.append(characterSet.substring(randomInt, randomInt + 1));
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            socketChannel.close();
            Intent intent1 = new Intent(CameraActivity.this, Chatroom.class);
            intent1.putExtra("방ID",chatID);
            intent1.putExtra("방이름",RoomName);
            startActivity(intent1);
            finish();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
