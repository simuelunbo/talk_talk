package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_Find_friend;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_contacts;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_find_friend;
import com.example.sjb.talk_talk.Request.Add_friendRequest;
import com.example.sjb.talk_talk.Request.Session_Request;
import com.example.sjb.talk_talk.fragment.etc;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class Adapter_find_friend extends RecyclerView.Adapter<Holder_find_friend>{

    ArrayList<Item_Find_friend> find_friends;
    Context mContext;
    String userName, userEmail, userGoogle;

    public Adapter_find_friend(ArrayList contact){
        find_friends = contact;
    }

    @Override
    public Holder_find_friend onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_recycler,parent,false);
        mContext = parent.getContext();
        Holder_find_friend holder_find_friend = new Holder_find_friend(view);
        return holder_find_friend;
    }

    @Override
    public void onBindViewHolder(Holder_find_friend holder, final int position) {

        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        userGoogle = sp.getString("google","");

        Holder_find_friend holder_find_friend = (Holder_find_friend) holder;

        ImageView imageView = ((Holder_find_friend) holder).friend_img;
        String currentUrl = find_friends.get(position).friend_profile_img;
        if(currentUrl.equals("null")==true)
        {
            imageView.setImageResource(R.drawable.profile);
        }
        else {
            Glide.with(mContext).load("http://115.71.239.124" + currentUrl).into(imageView); //glide 웹 url
        }
        holder.friend_name.setText(find_friends.get(position).friend_name);
        holder.friend_email.setText(find_friends.get(position).friend_email);
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("response값", ""+response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            //서버에서 보내준 값이 true이면?
                            if (success) {
                                Toast.makeText(mContext,"친구 추가 하였습니다", Toast.LENGTH_SHORT).show();
                                find_friends.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, find_friends.size());
                            } else {
                                Toast.makeText(mContext,"이미 친구 추가 하였습니다", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Add_friendRequest addFriendRequest = new Add_friendRequest(userEmail, userName, userGoogle, find_friends.get(position).friend_email,
                        find_friends.get(position).friend_name, find_friends.get(position).friend_google, responseListener);
                RequestQueue queue = Volley.newRequestQueue(mContext);
                queue.add(addFriendRequest);

            }
        });
    }


    @Override
    public int getItemCount() {
        return find_friends.size();
    }
}