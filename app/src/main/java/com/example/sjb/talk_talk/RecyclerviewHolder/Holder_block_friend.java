package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_block_friend extends RecyclerView.ViewHolder {
    public ImageView friend_img;
    public TextView friend_name;
    public TextView friend_email;
    public Button block;
    public Holder_block_friend(View v)
    {
        super(v);
        friend_img = (ImageView)v.findViewById(R.id.block_img);
        friend_name = (TextView)v.findViewById(R.id.block_name);
        friend_email = (TextView)v.findViewById(R.id.block_email);
        block = (Button)v.findViewById(R.id.unblock_friend);
    }
}
