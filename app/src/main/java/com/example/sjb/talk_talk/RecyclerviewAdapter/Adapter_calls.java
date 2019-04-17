package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_contacts;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_contacts;
import com.example.sjb.talk_talk.Request.Block_friendRequest;
import com.example.sjb.talk_talk.Service.Chat_service;
import com.example.sjb.talk_talk.activity.CallActivity;
import com.example.sjb.talk_talk.activity.Talktalk_main;
import com.example.sjb.talk_talk.fragment.contacts;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Adapter_calls extends RecyclerView.Adapter<Holder_contacts>{
    private static final String UPPER_ALPHA_DIGITS = "ACEFGHJKLMNPQRVWXY123456789";
    ArrayList<Item_contacts> item_contact;
    Context mContext;
    String userName,userEmail,usergoogle,friendEmail, friendName, friendGoogle;
    public Adapter_calls(ArrayList contact){
        item_contact = contact;
    }

    @Override
    public Holder_contacts onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_recycler,parent,false);
        mContext = parent.getContext();
        Holder_contacts holder_contacts = new Holder_contacts(view);
        return holder_contacts;
    }

    @Override
    public void onBindViewHolder(Holder_contacts holder, final int position) {
        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        usergoogle = sp.getString("google","");

        Holder_contacts holder_contacts = (Holder_contacts) holder;

        ImageView imageView = ((Holder_contacts) holder).mprofile_img;
        String currentUrl = item_contact.get(position).profile_img;
        if(currentUrl.equals("null")==true)
        {
            imageView.setImageResource(R.drawable.profile);
        }
        else {
            Glide.with(mContext).load("http://115.71.239.124"+currentUrl).into(imageView); //glide 웹 url
        }
        holder.mname.setText(item_contact.get(position).name);

        TextView textView = ((Holder_contacts) holder).mstatus;
        String msg = item_contact.get(position).status_msg;
        if(msg.equals("null")==true)
        {
            textView.setText("");
        }
        else {
            textView.setText(msg);
        }
        holder.mcardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return item_contact.size();
    }
}
