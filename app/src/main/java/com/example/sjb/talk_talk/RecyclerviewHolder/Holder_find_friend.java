package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_find_friend extends RecyclerView.ViewHolder {
    public ImageView friend_img;
    public TextView friend_name;
    public TextView friend_email;
    public ImageButton add;
    public Holder_find_friend(View v)
    {
        super(v);
        friend_img = (ImageView)v.findViewById(R.id.friend_img);
        friend_name = (TextView)v.findViewById(R.id.friend_name);
        friend_email = (TextView)v.findViewById(R.id.friend_email);
        add = (ImageButton)v.findViewById(R.id.plus_add_friend);
    }
}
