package com.example.sjb.talk_talk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_Chat_find_friend;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.Item.Item_contacts;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_Chat_find_friend;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_contacts;
import com.example.sjb.talk_talk.Request.Chat_find_friendRequest;
import com.example.sjb.talk_talk.Request.ContactsRequest;
import com.example.sjb.talk_talk.Request.FriendsInChatroomRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Chat_find_friend extends AppCompatActivity {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    ArrayList<Item_Chat_find_friend> friends = new ArrayList<>();
    String userEmail;
    String userName;
    String google;
    String ConvertJson;
    ImageButton back;
    Button find, startchat;
    EditText friendName;
    Adapter_Chat_find_friend mAdapter;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_find);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        final StringBuffer sb = new StringBuffer();// 방 ID
        final StringBuffer roomname = new StringBuffer();// 방이름

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        sb.append(" "+userName);

        back = (ImageButton)findViewById(R.id.backchatList);
        find = (Button)findViewById(R.id.select_friend_find);
        startchat = (Button)findViewById(R.id.StartChat);
        friendName = (EditText)findViewById(R.id.ChatFindfriendName);

        recyclerView = (RecyclerView)findViewById(R.id.ChatFindFriend_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);// 가로세로 지정
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                try {
                    //서버로부터 받는 데이터는 JSON타입의 객체이다.
                    JSONArray jsonResponse = new JSONArray(response);
                    for(int i = 0; i < jsonResponse.length(); i++){
                        Item_Chat_find_friend item_chat_find_friend = new Item_Chat_find_friend("", "" , "","","");
                        item_chat_find_friend.email = jsonResponse.getJSONObject(i).getString("email");
                        item_chat_find_friend.google = jsonResponse.getJSONObject(i).getString("google");
                        item_chat_find_friend.profile_img = jsonResponse.getJSONObject(i).getString("profile");
                        item_chat_find_friend.name = jsonResponse.getJSONObject(i).getString("name");
                        item_chat_find_friend.status_msg = jsonResponse.getJSONObject(i).getString("msg");
                        friends.add(item_chat_find_friend);
                    }
                    mAdapter = new Adapter_Chat_find_friend(friends);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
//                    textView.setText(contact.size()+"명");

                } catch (JSONException e) {
                    Log.i("error", ""+e);
                }

            }
        };//responseListener 끝

        //volley 사용법
        //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
        ContactsRequest contactsRequest = new ContactsRequest(userEmail, google, responseListener);
        //2. RequestQueue를 생성한다.
        RequestQueue queue = Volley.newRequestQueue(Chat_find_friend.this);
        //3. RequestQueue에 RequestObject를 넘겨준다.
        queue.add(contactsRequest);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String f_name =  friendName.getText().toString();

                Log.i("검색이름", ""+f_name);
                friends.clear();
                mAdapter = new Adapter_Chat_find_friend(friends);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                Response.Listener<String> responseListener = new Response.Listener<String>() {

                    //서버로부터 여기서 데이터를 받음
                    @Override
                    public void onResponse(String response) {
                        try {
                            //서버로부터 받는 데이터는 JSON타입의 객체이다.
                            JSONArray jsonResponse = new JSONArray(response);
                            for(int i = 0; i < jsonResponse.length(); i++){
                                Item_Chat_find_friend item_chat_find_friend = new Item_Chat_find_friend("", "" , "","","");
                                item_chat_find_friend.email = jsonResponse.getJSONObject(i).getString("email");
                                item_chat_find_friend.google = jsonResponse.getJSONObject(i).getString("google");
                                item_chat_find_friend.profile_img = jsonResponse.getJSONObject(i).getString("profile");
                                item_chat_find_friend.name = jsonResponse.getJSONObject(i).getString("name");
                                item_chat_find_friend.status_msg = jsonResponse.getJSONObject(i).getString("msg");
                                friends.add(item_chat_find_friend);
                            }
                            mAdapter = new Adapter_Chat_find_friend(friends);
                            recyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
//                    textView.setText(contact.size()+"명");

                        } catch (JSONException e) {
                            Log.i("error", ""+e);
                        }

                    }
                };//responseListener 끝

                //volley 사용법
                //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
                Chat_find_friendRequest chat_find_friendRequest = new Chat_find_friendRequest(f_name, userEmail, google, responseListener);
                //2. RequestQueue를 생성한다.
                RequestQueue queue = Volley.newRequestQueue(Chat_find_friend.this);
                //3. RequestQueue에 RequestObject를 넘겨준다.
                queue.add(chat_find_friendRequest);
            }
        });
        startchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends = mAdapter.getmItem();
                JSONArray jsonArray= new JSONArray();
                try {
                for(int i = 0; i <friends.size(); i++) {
                    Item_Chat_find_friend item = friends.get(i);
                    if (item.isSelected() == true) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("FriendName", friends.get(i).name);
                        jsonObject.put("FriendEmail", friends.get(i).email);
                        sb.append(" "+friends.get(i).name);
                        roomname.append(friends.get(i).name+" ");
                        jsonArray.put(jsonObject);
                    }
                }
//                        Intent intent = new Intent(Chat_find_friend.this,Chatroom.class);
                        ConvertJson = jsonArray.toString();

                    Response.Listener<String> responseListener = new Response.Listener<String>() {

                        //서버로부터 여기서 데이터를 받음
                        @Override
                        public void onResponse(String response) {
                            Log.i("서버값", ""+response);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);

                                boolean success = jsonResponse.getBoolean("success");
                                if(success) {
                                    Intent intent = new Intent(Chat_find_friend.this, Chatroom.class);
                                    intent.putExtra("방이름",roomname.toString());
                                    intent.putExtra("방ID",sb.toString());
                                    intent.putExtra("방생성","room");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };//responseListener 끝
                    Log.i("친구들 이름 붙여져 있는가2222?", ""+ sb.toString());
                    Log.i("converjson", ""+ConvertJson);
                    FriendsInChatroomRequest friendsInChatroomRequest = new FriendsInChatroomRequest(userName, userEmail, ConvertJson, sb.toString(), responseListener);
                    RequestQueue queue = Volley.newRequestQueue(Chat_find_friend.this);
                    queue.add(friendsInChatroomRequest);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

    }
}
