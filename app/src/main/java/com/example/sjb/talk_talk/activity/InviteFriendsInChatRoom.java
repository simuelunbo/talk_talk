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
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_Chat_find_friend;
import com.example.sjb.talk_talk.Request.Chat_find_friendRequest;
import com.example.sjb.talk_talk.Request.InviteFriendContactsRequest;
import com.example.sjb.talk_talk.Request.InviteFriendsInChatRoomRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class InviteFriendsInChatRoom extends AppCompatActivity {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    ArrayList<Item_Chat_find_friend> friends = new ArrayList<>();
    String userEmail;
    String userName;
    String google;
    String ConvertJson;
    ImageButton back;
    Button find, Invite;
    EditText friendName;
    Adapter_Chat_find_friend mAdapter;
    String chatID,roomname;

    SimpleDateFormat nFormat;
    long nNow;
    Date nDate;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_friend_in_chatroom);

        Intent intent2 = new Intent(this.getIntent());
        chatID = intent2.getStringExtra("chatID"); // 채팅방에서 받아온 방 ID값
        roomname = intent2.getStringExtra("roomname"); // 채팅방 이름
        Log.i("chatID값~!!!!!!", ""+chatID);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        back = (ImageButton)findViewById(R.id.backchatroom);
        find = (Button)findViewById(R.id.select_invite_friend);
        Invite = (Button)findViewById(R.id.invite);
        friendName = (EditText)findViewById(R.id.InviteFindfriendName);

        recyclerView = (RecyclerView)findViewById(R.id.InviteFriendInChatroom_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);// 가로세로 지정
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final StringBuffer sb = new StringBuffer(); // 방 ID값 및 방 이름 덧붙일거

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

                } catch (JSONException e) {
                    Log.i("error", ""+e);
                }

            }
        };//responseListener 끝

        //volley 사용법
        //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
        InviteFriendContactsRequest contactsRequest = new InviteFriendContactsRequest(chatID, userEmail, google, responseListener);
        //2. RequestQueue를 생성한다.
        RequestQueue queue = Volley.newRequestQueue(InviteFriendsInChatRoom.this);
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
                        Log.i("친구들 값", ""+response);
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

                        } catch (JSONException e) {
                            Log.i("error", ""+e);
                        }

                    }
                };//responseListener 끝

                //volley 사용법
                //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
                Chat_find_friendRequest chat_find_friendRequest = new Chat_find_friendRequest(f_name, userEmail, google, responseListener);
                //2. RequestQueue를 생성한다.
                RequestQueue queue = Volley.newRequestQueue(InviteFriendsInChatRoom.this);
                //3. RequestQueue에 RequestObject를 넘겨준다.
                queue.add(chat_find_friendRequest);
            }
        });
        Invite.setOnClickListener(new View.OnClickListener() {
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
                            jsonArray.put(jsonObject);
                        }
                    }
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
                                    Intent intent = new Intent();
                                    intent.putExtra("초대방이름",roomname+sb.toString());
                                    intent.putExtra("초대방ID",chatID+sb.toString());
                                    intent.putExtra("초대목록",ConvertJson);
                                    setResult(RESULT_OK, intent);
                                    finish();
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
                    InviteFriendsInChatRoomRequest inviteFriendsInChatRoomRequest = new InviteFriendsInChatRoomRequest(chatID, chatID+sb.toString(), ConvertJson, nFormat.format(nDate), responseListener);
                    RequestQueue queue = Volley.newRequestQueue(InviteFriendsInChatRoom.this);
                    queue.add(inviteFriendsInChatRoomRequest);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
