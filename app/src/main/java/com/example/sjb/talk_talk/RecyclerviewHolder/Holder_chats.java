package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_chats extends RecyclerView.ViewHolder {
    public ImageView mprofile_img;
    public TextView mname;
    public TextView mMsg;
    public TextView mtime;
    public ConstraintLayout mlayout;

    public Holder_chats(View v)
    {
        super(v);
        mprofile_img = (ImageView)v.findViewById(R.id.friendProfileImgView);
        mname = (TextView)v.findViewById(R.id.friendNameTxtView);
        mMsg = (TextView)v.findViewById(R.id.friendLastMsgTxtView);
        mtime = (TextView)v.findViewById(R.id.friendLastMsgTimeTxtView);
        mlayout = (ConstraintLayout) v.findViewById(R.id.chatLayout);
    }
}
