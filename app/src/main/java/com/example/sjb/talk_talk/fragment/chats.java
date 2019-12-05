package com.example.sjb.talk_talk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.Item.Item_chats;
import com.example.sjb.talk_talk.Item.Item_contacts;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_Chatroom;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_chats;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_contacts;
import com.example.sjb.talk_talk.Request.ChatRoomMsgUpdateRequest;
import com.example.sjb.talk_talk.Request.ChatsRequest;
import com.example.sjb.talk_talk.Request.ContactsRequest;
import com.example.sjb.talk_talk.Request.MyImgAndPersonnelCheckInRoomRequest;
import com.example.sjb.talk_talk.SQL_DBdata.DBHelper;
import com.example.sjb.talk_talk.activity.Chat_find_friend;
import com.example.sjb.talk_talk.activity.Chatroom;
import com.example.sjb.talk_talk.activity.My_info;
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
//채팅 목록
public class chats extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    Adapter_chats mAdapter;
    ArrayList<Item_chats> chat = new ArrayList<>();
    ArrayList<Item_Chatroom> ic = new ArrayList<>();
    String data;

    SimpleDateFormat mFormat;
    long mNow;
    Date mDate;

    Handler handler;
    SocketChannel socketChannel;
    private static final String HOST = "115.71.239.124";
    private static final int PORT = 5001;

    String userEmail;
    String userName;
    String google;
    ImageButton button;

    SimpleDateFormat nFormat;
    long nNow;
    Date nDate;

    int NumberOfPeople;

    private Context mcontext;

    DBHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // chats프레그먼트를 쓰기위해서 mcontext에 chats값을 넣음 이유) -> 메인 쓰레드가 아닌 보조 쓰레드상에서 getactivity()값을 못쓰기 때문에
        super.onCreate(savedInstanceState);
        mcontext = chats.this.getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.chats, container, false);
        Log.i("getActivity값", ""+chats.this.getActivity());
        SharedPreferences sp = getActivity().getSharedPreferences("login_user", Activity.MODE_PRIVATE); // 로그인 유저 정보 저장
        userEmail = sp.getString("user_Email","");
        userName = sp.getString("user_Name","");
        google = sp.getString("google","");
        dbHelper = new DBHelper(getActivity(),"Chatroom_record.db", null,1);

        recyclerView = (RecyclerView) layout.findViewById(R.id.chats_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        button = (ImageButton) layout.findViewById(R.id.add_chatroom);

        setmychat();

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chats.this.getActivity(), Chat_find_friend.class);
                startActivity(intent);
            }
        });

        return layout;
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
//                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = new Runnable() { // 서버에서 메세지를 받는 메소드

        public void run() {
            Response.Listener<String> responseListener = new Response.Listener<String>() {

                //서버로부터 여기서 데이터를 받음
                @Override
                public void onResponse(String response) {
                    try {
                        chat.clear();
                        //서버로부터 받는 데이터는 JSON타입의 객체이다.
                        JSONArray jsonResponse = new JSONArray(response);
                        for(int i = 0; i < jsonResponse.length(); i++){
                            Item_chats item_chats = new Item_chats("","","","","",false);
                            item_chats.room_num = jsonResponse.getJSONObject(i).getString("room_no");
                            item_chats.profile_img = jsonResponse.getJSONObject(i).getString("profile_img");
                            item_chats.name = jsonResponse.getJSONObject(i).getString("friend_name");
                            item_chats.msg =  jsonResponse.getJSONObject(i).getString("last_msg");
                            item_chats.time = jsonResponse.getJSONObject(i).getString("time");
                            chat.add(item_chats);
                        }

                        for (int i = 0; i < chat.size(); i++) {
                            for (int j = 0; j < chat.size(); j++) {
                                if (i == j) {
                                } else if (chat.get(j).room_num.equals(chat.get(i).room_num)) {
                                    chat.set(i, new Item_chats(chat.get(i).room_num, "root", chat.get(i).name + "  " + chat.get(j).name, chat.get(i).msg, chat.get(i).time, true));
                                    //중복값 제거 오류 수정
                                    Log.i("방번호~~~", "" + chat.get(i).room_num);
                                    Log.i("이름~~~!!!!!!!!", "" + chat.get(i).name);
                                    Log.i("stopnamechange값 변화했는가", "" + chat.get(i).stopnamechange);
                                    chat.remove(j);
                                }
                            }
                        }
                        mAdapter = new Adapter_chats(chat);
                        recyclerView.setAdapter(null);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.i("error", ""+e);
                    }

                }
            };//responseListener 끝

            //volley 사용법
            //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
            ChatsRequest chatsRequest = new ChatsRequest(userEmail, userName, google, responseListener);
            //2. RequestQueue를 생성한다.
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            //3. RequestQueue에 RequestObject를 넘겨준다.
            queue.add(chatsRequest);

        }

    };

    @Override
    public void onStop() {
        super.onStop();
        try{
            socketChannel.close();
        }
        catch (Exception e){
            Log.i("소켓채널 끊기 에러", ""+e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                try {
                    chat.clear();
                    //서버로부터 받는 데이터는 JSON타입의 객체이다 .
                    JSONArray jsonResponse = new JSONArray(response);
                    for(int i = 0; i < jsonResponse.length(); i++){
                        Item_chats item_chats = new Item_chats("","","","","",false);
                        item_chats.room_num = jsonResponse.getJSONObject(i).getString("room_no");
                        item_chats.profile_img = jsonResponse.getJSONObject(i).getString("profile_img");
                        item_chats.name = jsonResponse.getJSONObject(i).getString("friend_name");
                        item_chats.msg =  jsonResponse.getJSONObject(i).getString("last_msg");
                        item_chats.time = jsonResponse.getJSONObject(i).getString("time");
                        chat.add(item_chats);
                    }

                    for (int i = 0; i < chat.size(); i++) {
                        for (int j = 0; j < chat.size(); j++) {
                            if (i == j) {
                            } else if (chat.get(j).room_num.equals(chat.get(i).room_num)) {
                                chat.set(i, new Item_chats(chat.get(i).room_num, "root", chat.get(i).name + "  " + chat.get(j).name, chat.get(i).msg, chat.get(i).time, true));
                                //중복값 제거 오류 수정
                                Log.i("방번호~~~", "" + chat.get(i).room_num);
                                Log.i("이름~~~!!!!!!!!", "" + chat.get(i).name);
                                Log.i("stopnamechange값 변화했는가", "" + chat.get(i).stopnamechange);
                                chat.remove(j);
                            }
                        }
                    }
                    mAdapter = new Adapter_chats(chat);
                    recyclerView.setAdapter(null);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.i("error", ""+e);
                }

            }
        };//responseListener 끝

        //volley 사용법
        //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
        ChatsRequest chatsRequest = new ChatsRequest(userEmail, userName, google, responseListener);
        //2. RequestQueue를 생성한다.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        //3. RequestQueue에 RequestObject를 넘겨준다.
        queue.add(chatsRequest);

        ((Talktalk_main)getActivity()).refresh();
    }

    public void setmychat(){//------------------------------------------------------------------------------------------------------- sqlite db에서 저장된 값들을 recyclerview에 추가

        for(int i =0; i< dbHelper.Allresult().size(); i++) {
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
            Log.i("read들 개수값", "" + dbHelper.Allresult().get(i).Read);
            Log.i("readcheck들의 값 구하기", "" + dbHelper.Allresult().get(i).Readcheck);
            ic.add(item_chatroom);
        }
    }
}
