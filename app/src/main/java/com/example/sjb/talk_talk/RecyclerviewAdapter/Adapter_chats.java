package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_chats;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_chats;
import com.example.sjb.talk_talk.activity.Chatroom;
import com.example.sjb.talk_talk.activity.Talktalk_main;
import com.example.sjb.talk_talk.fragment.chats;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Adapter_chats extends RecyclerView.Adapter<Holder_chats> {
    ArrayList<Item_chats> item_chats;
    Context mContext;
    String userName,userEmail,usergoogle;
    public Adapter_chats(ArrayList chat){
        item_chats = chat;
    }
    @Override
    public Holder_chats onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_recycler,parent,false);
        mContext = parent.getContext();
        Holder_chats holder_chats = new Holder_chats(view);
        return holder_chats;
    }

    @Override
    public void onBindViewHolder(Holder_chats holder, final int i) {
        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        usergoogle = sp.getString("google","");

        Holder_chats holder_chats = (Holder_chats) holder;

        ImageView imageView = ((Holder_chats) holder).mprofile_img;
        String currentUrl = item_chats.get(i).profile_img;
        if(currentUrl.equals("null")==true)
        {
            imageView.setImageResource(R.drawable.profile);
        }
        else if (currentUrl.equals("root")==true){
            imageView.setImageResource(R.drawable.people);
        }
        else {
            Glide.with(mContext).load("http://115.71.239.124"+currentUrl).into(imageView);
        }
        holder.mname.setText(item_chats.get(i).name);

        if(item_chats.get(i).msg.equals("null")==true){
            holder.mMsg.setText("");
        }
        else {
            holder.mMsg.setText(item_chats.get(i).msg);
        }
        holder.mtime.setText(item_chats.get(i).time);

        if (item_chats.get(i).time.equals("null")==true){
            holder.mtime.setText("");
        }
        else {
            holder.mtime.setText(item_chats.get(i).time);
        }
        holder.mlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext,Chatroom.class);
                intent.putExtra("방이름",item_chats.get(i).name);
                intent.putExtra("방ID",item_chats.get(i).room_num);
                mContext.startActivity(intent);
                ((Talktalk_main)mContext).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return item_chats.size();
    }
}
