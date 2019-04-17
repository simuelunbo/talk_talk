package com.example.sjb.talk_talk.Service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.Item.Item_chats;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_Chatroom;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_chats;
import com.example.sjb.talk_talk.Request.ChatRoomMsgUpdateRequest;
import com.example.sjb.talk_talk.Request.ChatsRequest;
import com.example.sjb.talk_talk.Request.MyImgAndPersonnelCheckInRoomRequest;
import com.example.sjb.talk_talk.SQL_DBdata.DBHelper;
import com.example.sjb.talk_talk.activity.Chatroom;
import com.example.sjb.talk_talk.activity.Talktalk_main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chat_service extends Service {
    Notification notification;
    NotificationManager notification_m;
    DBHelper dbHelper;
    ArrayList<Item_Chatroom> chats = new ArrayList<>();
    ArrayList<Item_chats> room = new ArrayList<>();
    Handler handler;
    SocketChannel socketChannel;
    private static final String HOST = "115.71.239.124";
    private static final int PORT = 5001;
    private final int TEST_FLAG = 111;
    String data;
    Thread thread;

    SimpleDateFormat mFormat;
    long mNow;
    Date mDate;

    int NumberOfPeople;
    String userEmail;
    String userName;
    String google;

    int j;

    String array[];

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("서비스 onCreate", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {// 백그라운드에서 실행되는 동작들이 들어가는 곳
        Log.i("서비스 시작", "Chat_service 부분 서비스 시작");
        SharedPreferences sp = getSharedPreferences("login_user", Activity.MODE_PRIVATE); // 로그인 유저 정보 저장
        userEmail = sp.getString("user_Email", "");
        userName = sp.getString("user_Name", "");
        google = sp.getString("google", "");

        dbHelper = new DBHelper(this, "Chatroom_record.db", null, 1);

        notification_m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        setmychat();
        checkchatID();

        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + " a");
                    ioe.printStackTrace();
                }
                checkUpdate.start();
            }
        }).start();

        return START_STICKY;
    }

    public void onDestroy() {
        try {
            socketChannel.close();
            Log.i(socketChannel+"", "onDestroy에서 socketchannel 연결 되었는지 확인 유무");
            Thread.interrupted();
            Log.i("서비스 종료", "서비스 종료 chat_service onDestroy 부분");
        } catch (Exception e) {
            Log.i("service 에러", "" + e);
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
                if (readByteCount == -1) {
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

    private Runnable showUpdate = new Runnable() {

        public void run() {
            String receive = "" + data;
            Log.i("receive값", "" + receive);
            checkchatID();
            array = receive.split("!@#&");

            mFormat = new SimpleDateFormat("a hh:mm");
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);

            if (array[5].equals("100") == true) {

            } else {
                for (j = 0; j < room.size(); j++) {
                    if (room.get(j).room_num.equals(array[0]) == true) { // 다른 사람이 채팅방 초대받고 할때는 추가가 안됨 로직을 잘못함 근데 시간이 없어서 우선 이렇게 구현
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            //서버로부터 여기서 데이터를 받음
                            @Override
                            public void onResponse(String response) {
                                Log.i("서버값", "" + response);
                                Log.i("chatID값", "" + array[0]);
                                Log.i("보낸 유저 email array1", "" + array[1]);
                                Log.i("보낸 유저 이름 array2", "" + array[2]);
                                Log.i("보낸 메세지", "" + array[4]);
                                Log.i("메세지 상태값", "" + array[5]);
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");

                                    //서버에서 보내준 값이 true이면?
                                    if (success) {
                                        NumberOfPeople = jsonResponse.getInt("NumberOfPeople");  // 방에 존재하는 인원수
                                        String roomname = array[0].replace(userName, "");
                                        if (array[5].equals("0") == true) {
                                            Log.i("일반 메세지 값 0", "" + array[5]);
                                            if (array[1].equals(userEmail) == false) {
                                                // 해당방 ID값이고 다른 유저가 보낸 메세지 일때
                                                chats.add(new Item_Chatroom(array[0], array[1], array[2], array[3], array[4], 1, mFormat.format(mDate), NumberOfPeople - 1, false));
                                                dbHelper.insert(array[0], array[1], array[2], array[3], array[4], 1, mFormat.format(mDate), NumberOfPeople - 1, 1); // 1일때 거짓 0일때 참


//                                                Intent intent = new Intent(Chat_service.this, Chatroom.class);
//                                                intent.putExtra("방ID", array[0]);
//                                                intent.putExtra("방이름", roomname);
//                                                PendingIntent pendingIntent = PendingIntent.getActivity(Chat_service.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//                                                notification = new Notification.Builder(getApplicationContext())
//                                                        .setContentTitle(roomname)
//                                                        .setContentText(array[4])
//                                                        .setSmallIcon(R.drawable.talktalk)
//                                                        .setContentIntent(pendingIntent)
//                                                        .build();
//
//                                                //확인하면 자동으로 알림이 제거 되도록
//                                                notification.flags = Notification.FLAG_AUTO_CANCEL;
//                                                notification_m.notify(777, notification);// 777 notification의 고유 아이디
                                            }
                                        } else if (array[5].equals("1") == true) {
                                            if (array[1].equals(userEmail) == false) {
                                                if (array[3].equals("나감") == true) { // 유저가 나갔을때 room_no 값을 변경
                                                    String changechatID = array[0].replace(" " + array[2], "");
                                                    chats.add(new Item_Chatroom(array[0], array[1], array[2], "", array[2] + "님이 나갔습니다", 2, mFormat.format(mDate), 0, false));
                                                    dbHelper.chatID_update(array[0], changechatID);
                                                    dbHelper.insert(changechatID, array[1], array[2], " ", array[2] + "님이 나갔습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장

    //                                                Intent intent = new Intent(Chat_service.this, Talktalk_main.class);
    //                                                PendingIntent pendingIntent = PendingIntent.getActivity(Chat_service.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    //
    //                                                notification = new Notification.Builder(getApplicationContext())
    //                                                        .setContentTitle(changechatID)
    //                                                        .setContentText(array[2] + "님이 나갔습니다")
    //                                                        .setSmallIcon(R.drawable.talktalk)
    //                                                        .setContentIntent(pendingIntent)
    //                                                        .build();
    //
    //                                                //확인하면 자동으로 알림이 제거 되도록
    //                                                notification.flags = Notification.FLAG_AUTO_CANCEL;
    //                                                notification_m.notify(777, notification);// 777 notification의 고유 아이디

                                                } else if (array[3].equals("초대") == true) {
                                                    dbHelper.chatID_update(array[0], array[4]);
                                                    chats.add(new Item_Chatroom(array[4], array[1], array[2], "", array[2] + "님을 초대 하였습니다", 2, mFormat.format(mDate), 0, false));
                                                    dbHelper.insert(array[4], array[1], array[2], " ", array[2] + "님을 초대 하였습니다", 2, mFormat.format(mDate), 0, 0);// db에 메세지 저장

    //                                                Intent intent = new Intent(Chat_service.this, Talktalk_main.class);
    //                                                PendingIntent pendingIntent = PendingIntent.getActivity(Chat_service.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    //
    //                                                notification = new Notification.Builder(getApplicationContext())
    //                                                        .setContentTitle(array[4])
    //                                                        .setContentText(array[2] + "님을 초대 하였습니다")
    //                                                        .setSmallIcon(R.drawable.talktalk)
    //                                                        .setContentIntent(pendingIntent)
    //                                                        .build();
    //
    //                                                //확인하면 자동으로 알림이 제거 되도록
    //                                                notification.flags = Notification.FLAG_AUTO_CANCEL;
    //                                                notification_m.notify(777, notification);// 777 notification의 고유 아이디
                                                }
                                            }
                                        } else if (array[5].equals("2") == true) {
                                            if (array[1].equals(userEmail) == false) {
                                                chats.add(new Item_Chatroom(array[0], array[1], array[2], array[3], array[4], 4, mFormat.format(mDate), NumberOfPeople - 1, false));
                                                dbHelper.insert(array[0], array[1], array[2], array[3], array[4], 4, mFormat.format(mDate), NumberOfPeople - 1, 1);

//                                                Intent intent = new Intent(Chat_service.this, Chatroom.class);
//                                                intent.putExtra("방ID", array[0]);
//                                                intent.putExtra("방이름", roomname);
//                                                PendingIntent pendingIntent = PendingIntent.getActivity(Chat_service.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//                                                notification = new Notification.Builder(getApplicationContext())
//                                                        .setContentTitle(roomname)
//                                                        .setContentText("사진을 보냈습니다")
//                                                        .setSmallIcon(R.drawable.talktalk)
//                                                        .setContentIntent(pendingIntent)
//                                                        .build();
//
//                                                //확인하면 자동으로 알림이 제거 되도록
//                                                notification.flags = Notification.FLAG_AUTO_CANCEL;
//                                                notification_m.notify(777, notification);// 777 notification의 고유 아이디
                                            }
                                        } else if (array[5].equals("3") == true) {
                                            if (array[1].equals(userEmail) == false) {
                                                for (int i = 0; i < chats.size(); i++) {
                                                    if (chats.get(i).Read > 0) {
                                                        chats.set(i, new Item_Chatroom(array[0], chats.get(i).friendEmail, chats.get(i).friend, chats.get(i).friendimg, chats.get(i).chat, chats.get(i).who, chats.get(i).time, chats.get(i).Read - 1, chats.get(i).Readcheck));
                                                        dbHelper.readupdate(array[0], chats.get(i).Read);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };//responseListener 끝
                        MyImgAndPersonnelCheckInRoomRequest session_request = new MyImgAndPersonnelCheckInRoomRequest(chats.get(j).chatID, userEmail, userName, google, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(Chat_service.this);
                        queue.add(session_request);

                    }
                }
            }

        }

    };


    public void setmychat() {//------------------------------------------------------------------------------------------------------- sqlite db에서 저장된 값들을 recyclerview에 추가
        for (int i = 0; i < dbHelper.Allresult().size(); i++) {
            Item_Chatroom item_chatroom = new Item_Chatroom("", "", "", "", "", 100, "", 0, false);
            item_chatroom.chatID = dbHelper.Allresult().get(i).chatID;
            item_chatroom.friendEmail = dbHelper.Allresult().get(i).friendEmail;
            item_chatroom.friend = dbHelper.Allresult().get(i).friend;
            item_chatroom.friendimg = dbHelper.Allresult().get(i).friendimg;
            item_chatroom.chat = dbHelper.Allresult().get(i).chat;
            item_chatroom.who = dbHelper.Allresult().get(i).who;
            item_chatroom.time = dbHelper.Allresult().get(i).time;
            item_chatroom.Read = dbHelper.Allresult().get(i).Read;
            item_chatroom.Readcheck = dbHelper.Allresult().get(i).Readcheck;
            chats.add(item_chatroom);
        }
    }

    public void checkchatID()
    {
        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                try {
                    room.clear();
                    //서버로부터 받는 데이터는 JSON타입의 객체이다.
                    JSONArray jsonResponse = new JSONArray(response);
                    for (int i = 0; i < jsonResponse.length(); i++) {
                        Item_chats item_chats = new Item_chats("", "", "", "", "", false);
                        item_chats.room_num = jsonResponse.getJSONObject(i).getString("room_no");
                        item_chats.profile_img = jsonResponse.getJSONObject(i).getString("profile_img");
                        item_chats.name = jsonResponse.getJSONObject(i).getString("friend_name");
                        item_chats.msg = jsonResponse.getJSONObject(i).getString("last_msg");
                        item_chats.time = jsonResponse.getJSONObject(i).getString("time");
                        room.add(item_chats);
                    }

                    for (int i = 0; i < room.size(); i++) {
                        for (int j = 0; j < room.size(); j++) {
                            if (i == j) {
                            } else if (room.get(j).room_num.equals(room.get(i).room_num)) {
                                room.set(i, new Item_chats(room.get(i).room_num, "root", room.get(i).name + "  " + room.get(j).name, room.get(i).msg, room.get(i).time, true));
                                //중복값 제거 오류 수정
                                room.remove(j);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.i("error", "" + e);
                }

            }
        };//responseListener 끝

        //volley 사용법
        //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
        ChatsRequest chatsRequest = new ChatsRequest(userEmail, userName, google, responseListener);
        //2. RequestQueue를 생성한다.
        RequestQueue queue = Volley.newRequestQueue(Chat_service.this);
        //3. RequestQueue에 RequestObject를 넘겨준다.
        queue.add(chatsRequest);
    }
}

