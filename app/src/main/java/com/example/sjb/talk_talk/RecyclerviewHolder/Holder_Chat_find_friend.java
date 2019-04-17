package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_Chat_find_friend extends RecyclerView.ViewHolder {
    public ImageView mprofile_img;
    public TextView mname;
    public TextView memail;
    public CheckBox checkBox;
    public Holder_Chat_find_friend(View v){
        super(v);
        mprofile_img = (ImageView)v.findViewById(R.id.ChatFindProfile_img);
        mname = (TextView)v.findViewById(R.id.ChatFindUser_name);
        memail = (TextView)v.findViewById(R.id.ChatFIndEmail);
        checkBox = (CheckBox)v.findViewById(R.id.ChatFindcheckBox);
    }
}
