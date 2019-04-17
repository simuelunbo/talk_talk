package com.example.sjb.talk_talk.activity;

import android.content.ClipData;
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
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_Find_friend;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_find_friend;
import com.example.sjb.talk_talk.Request.Add_friendRequest;
import com.example.sjb.talk_talk.Request.Find_friend_emailRequest;
import com.example.sjb.talk_talk.Request.Find_friend_nameRequest;
import com.example.sjb.talk_talk.Request.Session_Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Find_friend extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    RadioButton Email,name;
    EditText find_email, find_name;
    Button find_user;
    String userName, userEmail, google;
    ArrayList<Item_Find_friend> friends = new ArrayList<>();
    Adapter_find_friend mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_freind);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        Email = (RadioButton)findViewById(R.id.select_Email);
        name = (RadioButton)findViewById(R.id.select_name);

        find_email = (EditText)findViewById(R.id.find_email);
        find_name = (EditText)findViewById(R.id.find_name);

        find_user = (Button)findViewById(R.id.find_user);

        recyclerView = (RecyclerView)findViewById(R.id.add_friend_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);// 가로세로 지정
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Email.setChecked(true); // 체크된 상태
        find_email.setVisibility(View.VISIBLE);
        find_name.setVisibility(View.GONE);

        Email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find_email.setVisibility(View.VISIBLE);
                find_name.setVisibility(View.GONE);
            }
        });
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find_email.setVisibility(View.GONE);
                find_name.setVisibility(View.VISIBLE);
            }
        });
        find_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friends.clear();
                mAdapter = new Adapter_find_friend(friends);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                if(Email.isChecked() || name.isChecked() == true){
                    if (Email.isChecked() == true){
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("json이 어떻게 오는건지 확인좀", ""+response);
                                try {
                                    JSONArray jsonResponse = new JSONArray(response);
                                        for (int i = 0; i < jsonResponse.length(); i++) {
                                            Item_Find_friend item_find_friend = new Item_Find_friend("", "", "", "");
                                            if(userEmail.equals(jsonResponse.getJSONObject(i).getString("email"))==true) {
                                            }
                                            else {
                                                item_find_friend.friend_name = jsonResponse.getJSONObject(i).getString("name");
                                                item_find_friend.friend_email = jsonResponse.getJSONObject(i).getString("email");
                                                item_find_friend.friend_profile_img = jsonResponse.getJSONObject(i).getString("profile");
                                                item_find_friend.friend_google = jsonResponse.getJSONObject(i).getString("google");
                                                friends.add(item_find_friend);
                                            }
                                        }
                                        mAdapter = new Adapter_find_friend(friends);
                                        recyclerView.setAdapter(mAdapter);
                                        mAdapter.notifyDataSetChanged();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Find_friend_emailRequest find_friend_emailRequest = new Find_friend_emailRequest(userEmail, userName, google, find_email.getText().toString(), responseListener);
                        RequestQueue queue = Volley.newRequestQueue(Find_friend.this);
                        queue.add(find_friend_emailRequest);
                    }
                    else{
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i("json이 어떻게 오는건지 확인좀", ""+response);
                                try {
                                    JSONArray jsonResponse = new JSONArray(response);
                                    for(int i=0;i<jsonResponse.length();i++)
                                    {
                                            Item_Find_friend item_find_friend = new Item_Find_friend("", "", "", "");
                                        if(userName.equals(jsonResponse.getJSONObject(i).getString("name"))==true &&
                                                userEmail.equals(jsonResponse.getJSONObject(i).getString("email"))==true) {
                                        }
                                        else {
                                            item_find_friend.friend_name = jsonResponse.getJSONObject(i).getString("name");
                                            item_find_friend.friend_email = jsonResponse.getJSONObject(i).getString("email");
                                            item_find_friend.friend_profile_img = jsonResponse.getJSONObject(i).getString("profile");
                                            item_find_friend.friend_google = jsonResponse.getJSONObject(i).getString("google");
                                            friends.add(item_find_friend);
                                        }
                                    }
                                    mAdapter = new Adapter_find_friend(friends);
                                    recyclerView.setAdapter(mAdapter);
                                    mAdapter.notifyDataSetChanged();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i("error", ""+e);
                                }
                            }
                        };

                        Find_friend_nameRequest find_friend_nameRequest = new Find_friend_nameRequest(userEmail, userName, google, find_name.getText().toString(), responseListener);
                        RequestQueue queue = Volley.newRequestQueue(Find_friend.this);
                        queue.add(find_friend_nameRequest);

                    }
                }
                else {
                    Toast.makeText(Find_friend.this, "이름 또는 Email을 체크해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
