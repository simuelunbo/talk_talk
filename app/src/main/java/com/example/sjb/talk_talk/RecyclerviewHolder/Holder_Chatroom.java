package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_Chatroom extends RecyclerView.ViewHolder {
    public ImageView f_img;
    public TextView f_txt;
    public TextView f_time;
    public TextView f_read;
    public TextView f_name;

    public TextView my_txt;
    public TextView my_time;
    public TextView my_read;
    public TextView coming_txt;

    public ConstraintLayout f_layout;
    public ConstraintLayout my_layout;
    public ConstraintLayout coming_layout;
    public ConstraintLayout my_img_layout; //내가 이미지 메세지 보낼때 쓰는 레이아웃
    public ConstraintLayout f_img_layout; // 친구가 이미지 메세지 보낼때 쓰는 레이아웃

    public ImageView my_msg_img;
    public TextView my_msg_img_time;
    public TextView my_msg_img_read;

    public ImageView f_msg_img;// 이미지 메세지
    public ImageView f_msg_img_img; // 친구 이미지메세지 프로필사진
    public TextView f_msg_img_time;
    public TextView f_msg_img_name;
    public TextView f_msg_img_read;

    public Holder_Chatroom(View v) {
        super(v);
        f_img = (ImageView)v.findViewById(R.id.friendImgView);
        f_txt = (TextView)v.findViewById(R.id.friendMsgTxtView);
        f_time = (TextView)v.findViewById(R.id.F_Time);
        f_read = (TextView)v.findViewById(R.id.FriendRead);
        my_txt = (TextView)v.findViewById(R.id.myMsgTxtView);
        my_time = (TextView)v.findViewById(R.id.My_Time);
        my_read = (TextView)v.findViewById(R.id.MyRead);
        f_layout = (ConstraintLayout)v.findViewById(R.id.friendChatLayout);
        my_layout = (ConstraintLayout)v.findViewById(R.id.myChatLayout);
        coming_layout = (ConstraintLayout)v.findViewById(R.id.comingLayout);
        f_name = (TextView)v.findViewById(R.id.friendName);
        coming_txt = (TextView)v.findViewById(R.id.status);
        my_img_layout = (ConstraintLayout)v.findViewById(R.id.myChatImgLayout);
        f_img_layout = (ConstraintLayout)v.findViewById(R.id.friendChatImgLayout);
        my_msg_img = (ImageView)v.findViewById(R.id.myMsgImgView);
        my_msg_img_time = (TextView)v.findViewById(R.id.My_Time2);
        f_msg_img = (ImageView)v.findViewById(R.id.friendMsgImgView);
        f_msg_img_img = (ImageView)v.findViewById(R.id.friendImgView2);
        f_msg_img_time = (TextView)v.findViewById(R.id.F_Time2);
        f_msg_img_name = (TextView)v.findViewById(R.id.friendName2);
        my_msg_img_read = (TextView)v.findViewById(R.id.MyRead2);
        f_msg_img_read = (TextView)v.findViewById(R.id.FriendRead2);
    }
}
