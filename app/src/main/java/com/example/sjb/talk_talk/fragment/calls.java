package com.example.sjb.talk_talk.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.Item.Item_contacts;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewAdapter.Adapter_contacts;
import com.example.sjb.talk_talk.Request.ContactsRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class calls extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    Adapter_contacts mAdapter;
    ArrayList<Item_contacts> contact = new ArrayList<>();
    String userEmail;
    String userName;
    String google;

    public calls()
    {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.calls, container, false);
        SharedPreferences sp = getActivity().getSharedPreferences("login_user", Activity.MODE_PRIVATE); // 로그인 유저 정보 저장
        userEmail = sp.getString("user_Email","");
        userName = sp.getString("user_Name","");
        google = sp.getString("google","");

        recyclerView = (RecyclerView)layout.findViewById(R.id.call_RecyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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
                        Item_contacts item_contacts = new Item_contacts("", "" , "","","");
                        item_contacts.email = jsonResponse.getJSONObject(i).getString("email");
                        item_contacts.google = jsonResponse.getJSONObject(i).getString("google");
                        item_contacts.profile_img = jsonResponse.getJSONObject(i).getString("profile");
                        item_contacts.name = jsonResponse.getJSONObject(i).getString("name");
                        item_contacts.status_msg = jsonResponse.getJSONObject(i).getString("msg");
                        contact.add(item_contacts);
                    }
                    mAdapter = new Adapter_contacts(contact);
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        //3. RequestQueue에 RequestObject를 넘겨준다.
        queue.add(contactsRequest);

        return layout;
    }
}
