package com.example.sjb.talk_talk.RecyclerviewHolder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;

public class Holder_contacts extends RecyclerView.ViewHolder {
    public ImageView mprofile_img;
    public TextView mname;
    public TextView mstatus;
    public CardView mcardView;
    public Holder_contacts(View v)
    {
        super(v);
        mprofile_img = (ImageView)v.findViewById(R.id.profile_img);
        mname = (TextView)v.findViewById(R.id.user_name);
        mstatus = (TextView)v.findViewById(R.id.Status_message);
        mcardView = (CardView)v.findViewById(R.id.contacts_cardview);
    }
}
