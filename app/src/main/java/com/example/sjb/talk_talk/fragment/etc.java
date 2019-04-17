package com.example.sjb.talk_talk.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Request.Session_Request;
import com.example.sjb.talk_talk.activity.Block_friend;
import com.example.sjb.talk_talk.activity.Find_friend;
import com.example.sjb.talk_talk.activity.Login;
import com.example.sjb.talk_talk.activity.My_info;
import com.example.sjb.talk_talk.activity.Talktalk_main;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class etc extends Fragment {

    ImageView profile1;
    TextView profile2, profile3;
    Button logout, add_friend, block_friend;
    String img_url,userName,userEmail,google;

    public etc()
    {
        // required
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout layout = (ConstraintLayout)inflater.inflate(R.layout.etc, container, false);
        Log.i("시작", "onCreateView: 시작~~~~~!!!!!!!!!!!");



        profile1 = (ImageView)layout.findViewById(R.id.my_img);
        profile2 = (TextView)layout.findViewById(R.id.my_name);
        profile3 = (TextView)layout.findViewById(R.id.my_email);
        logout = (Button)layout.findViewById(R.id.Logout);
        add_friend = (Button)layout.findViewById(R.id.add_friend);
        block_friend = (Button)layout.findViewById(R.id.block_friend);

        SharedPreferences sp = getActivity().getSharedPreferences("login_user",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etc.this.getActivity(),Find_friend.class);
                startActivity(intent);
            }
        });

        profile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etc.this.getActivity(),My_info.class);
                startActivity(intent);
            }
        });
        profile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etc.this.getActivity(),My_info.class);
                startActivity(intent);
            }
        });
        profile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etc.this.getActivity(),My_info.class);
                startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                editor.remove("user_Email").commit();
                editor.remove("user_Name").commit();
                editor.remove("google").commit(); //로그아웃일때 sharedpreference에 저장된 유저 정보 삭제

                Intent intent = new Intent(etc.this.getActivity(),Login.class);
                FirebaseAuth.getInstance().signOut(); // 구글 소셜 로그인 로그아웃
                startActivity(intent);
                getActivity().finish();
            }
        });
        block_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etc.this.getActivity(),Block_friend.class);
                startActivity(intent);
            }
        });

        return layout;
    }

    @Override
    public void onResume() { // onresume에 텍스트와 이미지 값을 넣음 화면이 이동되고 다시 되돌아 올때 resume에서 시작됨
        super.onResume();
        profile2.setText(userName);
        profile3.setText(userEmail);


        //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    //서버에서 보내준 값이 true이면?
                    if (success) {
                        img_url = "http://115.71.239.124"+jsonResponse.getString("profile"); // 주소값

                        Log.i("이미지 주소값", ""+img_url);

                        if(img_url.equals("http://115.71.239.124null")==true)
                        {
                            profile1.setImageResource(R.drawable.profile);
                        }
                        else {
                            Glide.with(etc.this.getActivity()).load(img_url).into(profile1);
                        }
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Session_Request sessionRequest = new Session_Request(userEmail, userName, google, responseListener);
        RequestQueue queue = Volley.newRequestQueue(etc.this.getActivity());
        queue.add(sessionRequest);

        ((Talktalk_main)getActivity()).refresh();
    }
}
