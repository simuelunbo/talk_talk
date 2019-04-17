package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_Chat_find_friend;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_Chat_find_friend;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Adapter_Chat_find_friend extends RecyclerView.Adapter<Holder_Chat_find_friend> {
    ArrayList<Item_Chat_find_friend> mItem;
    Context mContext;
    String userName,userEmail,usergoogle;

    public Adapter_Chat_find_friend(ArrayList friend) {
        mItem = friend;
    }

    @Override
    public Holder_Chat_find_friend onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_find_recycler,parent,false);
        mContext = parent.getContext();
        Holder_Chat_find_friend holder_chat_find_friend = new Holder_Chat_find_friend(view);
        return holder_chat_find_friend;
    }

    @Override
    public void onBindViewHolder(Holder_Chat_find_friend holder, final int i) {
        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        usergoogle = sp.getString("google","");

        Holder_Chat_find_friend holder_chat_find_friend = (Holder_Chat_find_friend) holder;

        ImageView imageView = ((Holder_Chat_find_friend) holder).mprofile_img;
        String currentUrl = mItem.get(i).profile_img;
        if(currentUrl.equals("null")==true)
        {
            imageView.setImageResource(R.drawable.profile);
        }
        else {
            Glide.with(mContext).load("http://115.71.239.124"+currentUrl).into(imageView); //glide 웹 url
        }
        holder.mname.setText(mItem.get(i).name);
        holder.memail.setText(mItem.get(i).email);
        holder.checkBox.setChecked(mItem.get(i).isSelected);
        holder.checkBox.setTag(mItem.get(i));

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Item_Chat_find_friend friend = (Item_Chat_find_friend) cb.getTag(); //  아이템 위치 태그 달기
                friend.setSelected(cb.isChecked());
                mItem.get(i).setSelected(cb.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public ArrayList<Item_Chat_find_friend> getmItem(){
        return mItem;
    }
}
