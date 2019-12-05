package com.example.sjb.talk_talk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.Item.Item_ChattingProfile;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_Chatroom;
import com.example.sjb.talk_talk.Request.ChatImageUploadRequest;
import com.example.sjb.talk_talk.Request.ChatRoomMsgUpdateRequest;
import com.example.sjb.talk_talk.Request.LeaveTheRoomRequst;
import com.example.sjb.talk_talk.Request.MyImgAndPersonnelCheckInRoomRequest;
import com.example.sjb.talk_talk.Request.Session_Request;
import com.example.sjb.talk_talk.SQL_DBdata.DBHelper;
import com.example.sjb.talk_talk.Service.Chat_service;
import com.example.sjb.talk_talk.fragment.chats;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chatroom extends AppCompatActivity {

    private  static final int MY_PERMISSION_CAMERA = 1111;
    private  static final int REQUEST_TAKE_PHOTO = 2222;
    private  static  final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private static final String UPPER_ALPHA_DIGITS = "ACEFGHJKLMNPQRUVWXY123456789";

    Handler handler;
    String data;
    public static SocketChannel socketChannel;
    private static final String HOST = "115.71.239.124";
    private static final int PORT = 5001;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    String msg;
    String userEmail;
    String userName;
    String userGoogle;
    String  mCurrentPhotoPath, send_img_url;
    public static String chatID, RoomName;
    String img_url; // 자기 프로필 사진 위치
    String create_room;
    Button sendMsgBtn;
    ImageView back,select,AddImg;
    EditText sendMsgEditText;
    TextView roomname;
    Adapter_Chatroom mAdapter;
    int NumberOfPeople;
    SimpleDateFormat mFormat;
    long mNow;
    Date mDate;
    String[] array;

    SimpleDateFormat nFormat;
    long nNow;
    Date nDate;
    ArrayList<Item_Chatroom> chat = new ArrayList<>();

    Uri imageUri;
    Uri photoURI, albumURI;
    Bitmap bitmap;
    DBHelper dbHelper;

    //채팅방
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        sendMsgBtn= (Button) findViewById(R.id.sendMsgBtn);
        sendMsgEditText = (EditText) findViewById(R.id.msgEditText);
        roomname = (TextView) findViewById(R.id.chatRoomFriendNameTxtView);
        back = (ImageView) findViewById(R.id.backChatRoomListImgView);
        select = (ImageView) findViewById(R.id.menuChatRoomImgView);
        AddImg = (ImageView) findViewById(R.id.addPhotoImgView);
        recyclerView = (RecyclerView) findViewById(R.id.chatRoomListView);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        dbHelper = new DBHelper(getApplicationContext(),"Chatroom_record.db", null,1);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        userGoogle = sp.getString("google","");

        final Intent intent = new Intent(this.getIntent());
        RoomName = intent.getStringExtra("방이름");
        chatID = intent.getStringExtra("방ID");
//        create_room = intent.getStringExtra("방생성");

        roomname.setText(RoomName);

        Intent intent1 = new Intent(Chatroom.this,Chat_service.class);
        stopService(intent1);
        Log.i("서비스 종료", "서비스 종료 Chatroom 부분");

        //------------------------------------------------------------------------------------------------------------------------------------------------------------- 나의 이미지값 가져오기

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
        RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
        queue.add(session_request);
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

        setmychat();// 소켓연결후 setmychat() 메소드 실행(채팅방 채팅 내용들 불러오기)
        recyclerView.scrollToPosition(chat.size()-1); // 어레이 리스트의 크기의 -1 위치로 포지셔닝을 하는 것 밑으로 갱신

        sendMsgBtn.setOnClickListener(new View.OnClickListener() { // 메세지 보내기
            @Override
            public void onClick(View v) {
                try {

                    final String return_msg = sendMsgEditText.getText().toString();
                    Log.i("보내는값이 보내지는가", ""+return_msg);

                    mFormat = new SimpleDateFormat("a hh:mm");

                    mNow = System.currentTimeMillis();
                    mDate = new Date(mNow);

                    nFormat = new SimpleDateFormat("MM/dd a hh:mm");

                    nNow = System.currentTimeMillis();
                    nDate = new Date(nNow);

                    dbHelper.insert(chatID, userEmail, userName, img_url, return_msg, 0, mFormat.format(mDate),NumberOfPeople-1 ,0); // readcheck 0일때 참 1 일때는 거짓

                    chat.add(new Item_Chatroom(chatID, userEmail,userName,img_url,return_msg,0, mFormat.format(mDate),NumberOfPeople-1 ,true));
                    mAdapter = new Adapter_Chatroom(chat);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(chat.size()-1);

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
                                        new SendmsgTask1().execute(sendok);
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
                        RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
                        queue.add(chatRoomMsgUpdateRequest);
                    }

                } catch (Exception e) {
                    Log.i("button 쪽 에러", ""+e);
                    e.printStackTrace();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() { // 뒤로가기
            @Override
            public void onClick(View v) {
                try {
                    socketChannel.close();
                    Intent intent1 = new Intent(Chatroom.this,Talktalk_main.class);
                    intent1.putExtra("chats","chats");
                    startActivity(intent1);
                    finish();
                }
                catch (Exception e){
                    Log.i("소켓채널 닫기 error", ""+e);
                }
            }
        });
        select.setOnClickListener(new View.OnClickListener() { // 방나가기, 초대하기 선택
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(Chatroom.this,v);
                p.inflate(R.menu.select_room);
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.RoomOut:
                                AlertDialog.Builder builder = new AlertDialog.Builder(Chatroom.this);

                                builder.setTitle("채팅방 나가기")
                                        .setMessage("채팅방을 나가면 대화 내용 및 채팅 목록에서 모두 삭제됩니다 채팅방에서 나가시겠습니까?")
                                        .setCancelable(false)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
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
                                                                final String changechatID = chatID.replace(" "+userName,"");
                                                                String leave = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+"나감"+"!@#&"+changechatID+"!@#&"+"1"+"!@#&";
                                                                new SendmsgTask1().execute(leave);
                                                                dbHelper.delete(chatID); // db에 채팅아이디 값들을 삭제
                                                                socketChannel.close();
                                                                Intent intent1 = new Intent(Chatroom.this,Talktalk_main.class);
                                                                intent1.putExtra("chats","chats");
                                                                startActivity(intent1);
                                                                finish();
                                                            }
                                                        }
                                                        catch (Exception e)
                                                        {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                };//responseListener 끝
                                                String changeroomID = chatID.replace(" "+userName,"");// 유저 방ID에서 삭제
                                                LeaveTheRoomRequst leaveTheRoomRequst = new LeaveTheRoomRequst(chatID, changeroomID, userEmail, responseListener);
                                                RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
                                                queue.add(leaveTheRoomRequst);
                                            }
                                        })
                                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog dialog =builder.create();
                                dialog.show();
                                break;
                            case R.id.InviteFriend:
                                try {
                                    Intent intent1 = new Intent(Chatroom.this, InviteFriendsInChatRoom.class);
                                    intent1.putExtra("chatID", chatID);
                                    intent1.putExtra("roomname", roomname.getText().toString());//방제 텍스트값을 가져와서 인텐트로 보낸다
//                                    socketChannel.close();
                                    startActivityForResult(intent1, 1);
                                }
                                catch (Exception e){

                                }
                            default:
                                break;
                        }
                        return false;
                    }
                });
                p.show();
            }
        });
        AddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();

                PopupMenu p = new PopupMenu(Chatroom.this,v);
                p.inflate(R.menu.chatroom_etc);
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.m1: //카메라
                                captureCamera();
                                break;
                            case R.id.m2: // 앨범 불러오기
                                getAlbum();
                                break;
                            case R.id.m3: // 영상통화
                                break;
                            case R.id.m4: // 방송하기
                                try {
//                                    String RandomRoomID = randomString(7, UPPER_ALPHA_DIGITS);
//                                    String streaming = chatID + "!@#&" + userEmail + "!@#&" + userName + "!@#&" + img_url + "!@#&" + RandomRoomID + "!@#&" + "4" + "!@#&";
//                                    // '0번째' 방ID '첫번째' 유저 email '두번째' 유저 이름 '3번째'보내는 유저 프로필 사진 '4번째' 스트리밍 방 ID값 '5번째' 4 방송 시작
//                                    chat.add(new Item_Chatroom(chatID, userEmail, userName, img_url, userName + "님이 방송 시작하였습니다", 0, mFormat.format(mDate), NumberOfPeople - 1, true));
//                                    // 자기 자신이 방송하는걸 챗창에 올리는거라서 who 값을 0으로 주었다
//                                    dbHelper.insert(chatID, userEmail, userName, img_url, userName + "님이 방송 시작하였습니다", 0, mFormat.format(mDate), NumberOfPeople - 1, 0); // 0은 true 값 1은 false값
//                                    new SendmsgTask1().execute(streaming);
                                    socketChannel.close();
                                    Intent intent3 = new Intent(Chatroom.this, CameraActivity.class);
                                    intent3.putExtra("방ID", chatID);
                                    intent3.putExtra("방이름", roomname.getText().toString());//방제 텍스트값을 가져와서 인텐트로 보낸다
                                    startActivity(intent3);
                                    finish();
                                }
                                catch (Exception e){
                                    Log.i("방송하기 부분 소켓 close error", ""+e);
                                }
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                p.show();
            }
        });
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
                    sendMsgEditText.setText("");
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

     private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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

                        mAdapter = new Adapter_Chatroom(chat);
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
                            final String changeroomname = roomname.getText().toString().replace(array[2],"");//나간유저 방이름에서 삭제
                            dbHelper.chatID_update(chatID,changechatID); // sqlite DB에 변경된 chatID값을 변경함

                            chatID = changechatID;// 변경된 방ID값

                            dbHelper.insert(chatID, array[1], array[2], " ", array[2]+"님이 나갔습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장
                            Log.i("insert값2222", "2222");
                            mAdapter = new Adapter_Chatroom(chat);
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
                                            roomname.setText(changeroomname); // 변경된 방이름
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
                            RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
                            queue.add(chatRoomMsgUpdateRequest);
                        }
                        else if (array[3].equals("초대")==true){
                            Log.i("array[3]값이 '초대'인가???", ""+array[3]);

                            dbHelper.chatID_update(chatID,array[4]); //array[4]에 변경된 chatID값을 넣음 그후 변경된 chatID값을 db에도 chatID값 변경
                            chatID = array[4];

                            chat.add(new Item_Chatroom(chatID,array[1],array[2],"",array[2]+"님을 초대 하였습니다",2,mFormat.format(mDate),0,true));

                            dbHelper.insert(chatID,array[1],array[2]," ",array[2]+"님을 초대 하였습니다",2,mFormat.format(mDate),0,0);// db에 메세지 저장
                            Log.i("insert값3333", "3333");
                            String changeroomname = roomname.getText().toString()+" "+array[2];
                            roomname.setText(changeroomname); // 방이름 변경
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

                            mAdapter = new Adapter_Chatroom(chat);
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                            ChatRoomMsgUpdateRequest chatRoomMsgUpdateRequest = new ChatRoomMsgUpdateRequest(chatID, array[2]+"님을 초대 하였습니다", nFormat.format(nDate), responseListener);
                            RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
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

                        mAdapter = new Adapter_Chatroom(chat);
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
                        chat.add(new Item_Chatroom(chatID,array[4],array[2],array[3],array[2]+"님이 방송을 시작하였습니다",5,mFormat.format(mDate),NumberOfPeople-2,true));
                        // 이메일값에 streamID값을 넣어줬음
                        dbHelper.insert(chatID, array[4],array[2],array[3],array[2]+"님이 방송을 시작하였습니다 ",5,mFormat.format(mDate),NumberOfPeople-2,0);// 1일때 거짓 0일때 참
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[4]+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);
                    }
                }
                else if(array[5].equals("5")==true){ //방송 종료
                    if(array[1].equals(userEmail)==false){
                        // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                        chat.add(new Item_Chatroom(chatID,array[1],array[2],array[3],array[2]+"님이 방송을 종료하였습니다",6,mFormat.format(mDate),NumberOfPeople-2,true));// 이메일값에 streamID값을 넣어줬음
                        dbHelper.insert(chatID, array[1],array[2],array[3],array[2]+"님이 방송을 종료하였습니다",6,mFormat.format(mDate),NumberOfPeople-2,0);// 1일때 거짓 0일때 참
                        // NumberOfPeople-2값으로 준 이유는 해당 채팅방에 속한 메세지를 받는 유저가 채팅방에 들어 와있다고 판단하고 -1 더 차감 하고 readcheck 값을 참으로 설정
                        String returnmsg = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+array[2]+"님이 방송을 종료하였습니다"+"!@#&"+"3"+"!@#&"; // 해당 메세지를 읽었다고 확인 메세지를 보내기위한 string 값 그래서 array[5]에 3(읽음확인용)으로 주었다
                        delaymessage(returnmsg);
                    }
                }
            }
            mAdapter = new Adapter_Chatroom(chat);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chat.size()-1); // 리사이클러뷰 포지셔닝 밑으로
        }
    };

    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함

                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File createImageFile() throws IOException {//저장경로 파일
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "sjb");

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }
    private void getAlbum(){
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
    public void cropImage(){
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                String ChangedRoomName = data.getStringExtra("초대방이름");
                String ChangedRoomID = data.getStringExtra("초대방ID");
                String friendlist = data.getStringExtra("초대목록");

                try{
                    JSONArray jsonArray = new JSONArray(friendlist);
                    NumberOfPeople = NumberOfPeople + jsonArray.length();
                    for(int i=0;i<jsonArray.length();i++){
                        String invite = chatID+"!@#&"+jsonArray.getJSONObject(i).getString("FriendEmail")+"!@#&"+jsonArray.getJSONObject(i).getString("FriendName")+"!@#&"+"초대"+"!@#&"+ChangedRoomID+"!@#&"+"1"+"!@#&";// array[4]를 임시적으로 방ID값을 넣음
                        new SendmsgTask1().execute(invite);

                        dbHelper.chatID_update(chatID,ChangedRoomID);// DB에 있는 채팅방ID값 변경시키기
                        chatID = ChangedRoomID;
                        roomname.setText(ChangedRoomName);
                        chat.add(new Item_Chatroom(chatID, jsonArray.getJSONObject(i).getString("FriendEmail"),jsonArray.getJSONObject(i).getString("FriendName"),"",jsonArray.getJSONObject(i).getString("FriendName")+"님을 초대 하였습니다",2,"",0,false));
                        dbHelper.insert(chatID, jsonArray.getJSONObject(i).getString("FriendEmail"),jsonArray.getJSONObject(i).getString("FriendName"),"",jsonArray.getJSONObject(i).getString("FriendName")+"님을 초대 하였습니다",2,"",0,0);
                        // db에 나간 유저 메세지 집어 넣기
                    }
                    mAdapter = new Adapter_Chatroom(chat);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
                catch (JSONException e){
                    Log.e("JSON error", ""+e);
                }
            }
        }
        else if(requestCode==REQUEST_TAKE_PHOTO){
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Log.i("REQUEST_TAKE_PHOTO", "OK");

                    galleryAddPic();
                    File albumFile = null;
                    albumFile = createImageFile();
                    photoURI = imageUri;
                    albumURI = Uri.fromFile(albumFile);
                    cropImage();
                } catch (Exception e) {
                    Log.e("REQUEST_TAKE_PHOTO", e.toString());
                }
            } else {
                Toast.makeText(Chatroom.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode==REQUEST_TAKE_ALBUM){
            if (resultCode == Activity.RESULT_OK) {

                if(data.getData() != null){
                    try {
                        File albumFile = null;
                        albumFile = createImageFile();
                        photoURI = data.getData();
                        albumURI = Uri.fromFile(albumFile);
                        cropImage();
                    }catch (Exception e){
                        Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                    }
                }
            }
        }
        else if (requestCode==REQUEST_IMAGE_CROP){
            if (resultCode == Activity.RESULT_OK) {
                galleryAddPic();
                Log.i("album", albumURI+"<<<<<<<<<<<<<<<albumURI");
                try {
                    InputStream inputStream = getContentResolver().openInputStream(albumURI);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    String imageData = imageToString(bitmap);


                    //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("서버 응답값", ""+response);
                            try {
                                mFormat = new SimpleDateFormat("a hh:mm");
                                mNow = System.currentTimeMillis();
                                mDate = new Date(mNow);

                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                send_img_url = jsonResponse.getString("send_img");
                                //서버에서 보내준 값이 true이면?
                                if (success) {
                                    chat.add(new Item_Chatroom(chatID,userEmail,userName,img_url,send_img_url,3,mFormat.format(mDate),NumberOfPeople-1,true));
                                    String img = chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+send_img_url+"!@#&"+"2"+"!@#&";// '0번째' 방ID '첫번째' 유저 email '두번째' 유저 이름 '3번째'보내는 유저 프로필 사진 '4번째' 보내는 메세지 '5번째' 상태 알림창 0번일때 메세지 1번일땐 들어오고 나가고 2번일땐 사진 이미지
                                    dbHelper.insert(chatID,userEmail,userName,img_url,send_img_url,3,mFormat.format(mDate),0,0);
                                    new SendmsgTask1().execute(img);
                                }
                                mAdapter = new Adapter_Chatroom(chat);
                                recyclerView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                Log.i("error", ""+e);
                            }
                        }
                    };
                    nFormat = new SimpleDateFormat("MM/dd a hh:mm");
                    nNow = System.currentTimeMillis();
                    nDate = new Date(nNow);
                    ChatImageUploadRequest image_uploadRequest = new ChatImageUploadRequest(chatID, imageData, nFormat.format(nDate),responseListener);
                    RequestQueue queue = Volley.newRequestQueue(Chatroom.this);
                    queue.add(image_uploadRequest);
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }

            }
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(Chatroom.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서..
                break;
        }
    }

    private String imageToString(Bitmap bitmap) {//비트맵 이미지를 Base64 string값으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void setmychat(){//------------------------------------------------------------------------------------------------------- sqlite db에서 저장된 값들을 recyclerview에 추가
        Log.i("dbHelper의 크기", ""+dbHelper.getResult(chatID).size());
        for(int i =0; i< dbHelper.getResult(chatID).size(); i++) {
            Item_Chatroom item_chatroom = new Item_Chatroom("","","","","",100,"",100,false);
            item_chatroom.chatID = dbHelper.getResult(chatID).get(i).chatID;
            item_chatroom.friendEmail = dbHelper.getResult(chatID).get(i).friendEmail;
            item_chatroom.friend = dbHelper.getResult(chatID).get(i).friend;
            item_chatroom.friendimg = dbHelper.getResult(chatID).get(i).friendimg;
            item_chatroom.chat = dbHelper.getResult(chatID).get(i).chat;
            item_chatroom.who = dbHelper.getResult(chatID).get(i).who;
            item_chatroom.time = dbHelper.getResult(chatID).get(i).time;
            Log.i("read들 개수값", ""+dbHelper.getResult(chatID).get(i).Read);
            Log.i("readcheck들의 값 구하기", ""+dbHelper.getResult(chatID).get(i).Readcheck);
            if(dbHelper.getResult(chatID).get(i).Readcheck==false){
                dbHelper.readupdate2(chatID, dbHelper.getResult(chatID).get(i).Read-1, 0,1); //해당 채팅방이고 메세지 readcheck값이 1일때 read값을 -1 감소시키고 readcheck값을 0으로 바꿈
                item_chatroom.Read = dbHelper.getResult(chatID).get(i).Read;
                item_chatroom.Readcheck = true;
                String  SendReadMark= chatID+"!@#&"+userEmail+"!@#&"+userName+"!@#&"+img_url+"!@#&"+dbHelper.getResult(chatID).get(i).chat+"!@#&"+"3"+"!@#&"; // 해당유저가 읽었다고 표시하기 위해 보내는 메세지
                new SendmsgTask2().execute(SendReadMark); // 메세지 보내기
            }
            else {
                item_chatroom.Read = dbHelper.getResult(chatID).get(i).Read;
                item_chatroom.Readcheck = true;
            }
            chat.add(item_chatroom);
        }
        mAdapter = new Adapter_Chatroom(chat);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();// arraylist에 추가 어뎁터 동기화
    }

    public void delaymessage(final String a){ // 메세지 받고 1초뒤에 읽었다고 확인 메세지 보내기
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    new SendmsgTask2().execute(a);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }
     @Override
     public void onBackPressed(){
        super.onBackPressed();
        try {
            socketChannel.close();
            Intent intent1 = new Intent(Chatroom.this, Talktalk_main.class);
            intent1.putExtra("chats", "chats");
            startActivity(intent1);
            finish();
        }
        catch (Exception e){
            e.printStackTrace();
        }
     }


}
