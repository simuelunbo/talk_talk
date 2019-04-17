package com.example.sjb.talk_talk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_block_friend;
import com.example.sjb.talk_talk.Item.Item_contacts;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_block_friend;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_contacts;
import com.example.sjb.talk_talk.Request.Block_friend_listRequest;
import com.example.sjb.talk_talk.Request.Msg_changeRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Block_friend extends AppCompatActivity {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    String userName, userEmail, google;
    ArrayList<Item_block_friend> block = new ArrayList<>();
    Adapter_block_friend mAdapter;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_friend);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        recyclerView = (RecyclerView)findViewById(R.id.block_friend_recyclerview2);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);// 가로세로 지정
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            //서버로부터 여기서 데이터를 받음
            @Override
            public void onResponse(String response) {
                Log.i("서버 JSON 값", ""+response);
                try {
                    //서버로부터 받는 데이터는 JSON타입의 객체이다.
                    JSONArray jsonResponse = new JSONArray(response);
                    for(int i = 0; i < jsonResponse.length(); i++){
                        Item_block_friend item_block_friend = new Item_block_friend("", "" , "","");
                        item_block_friend.friend_email = jsonResponse.getJSONObject(i).getString("email");
                        item_block_friend.friend_google = jsonResponse.getJSONObject(i).getString("google");
                        item_block_friend.friend_name = jsonResponse.getJSONObject(i).getString("name");
                        item_block_friend.friend_profile_img = jsonResponse.getJSONObject(i).getString("profile");
                        block.add(item_block_friend);
                    }
                    mAdapter = new Adapter_block_friend(block);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.i("error", ""+e);
                    //알림상자를 만들어서 보여줌
                }
            }
        };//responseListener 끝

        //volley 사용법
        //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
        Block_friend_listRequest msg_changeRequest = new Block_friend_listRequest(userEmail, google, responseListener);
        //2. RequestQueue를 생성한다.
        RequestQueue queue = Volley.newRequestQueue(Block_friend.this);
        //3. RequestQueue에 RequestObject를 넘겨준다.
        queue.add(msg_changeRequest);

    }
}
