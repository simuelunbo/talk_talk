package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_Chatroom;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_Chatroom;
//import com.example.sjb.talk_talk.activity.Chatroom;
import com.example.sjb.talk_talk.activity.Chatroom;
import com.example.sjb.talk_talk.activity.PlayerActivity;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Adapter_Chatroom extends RecyclerView.Adapter<Holder_Chatroom> {

    ArrayList<Item_Chatroom> chatrooms;
    Context mContext;
    String userName,userEmail,usergoogle;
    String RoomName,chatID;
    SocketChannel socketChannel;

    public Adapter_Chatroom(ArrayList chatroom) { chatrooms = chatroom; }

    @Override
    public Holder_Chatroom onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_recycler,parent,false);
        mContext = parent.getContext();
        Holder_Chatroom chatHolder = new Holder_Chatroom(view);
        return chatHolder;
    }

    @Override
    public void onBindViewHolder(Holder_Chatroom holder, int i) {

        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        usergoogle = sp.getString("google","");

        RoomName = ((Chatroom)mContext).RoomName;
        chatID = ((Chatroom)mContext).chatID;

        Holder_Chatroom chatholder = (Holder_Chatroom) holder;

        int who = chatrooms.get(i).who;
        String msg = chatrooms.get(i).chat;
        String name = chatrooms.get(i).friend;
        String img = chatrooms.get(i).friendimg;
        String time = chatrooms.get(i).time;
        int read = chatrooms.get(i).Read;

        switch (who){ //나의 메세지 채팅
            case 0:
                holder.f_layout.setVisibility(View.GONE);
                holder.my_layout.setVisibility(View.VISIBLE);
                holder.coming_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.my_txt.setText(msg);
                holder.my_time.setText(time);
                if(read>0){
                    holder.my_read.setText(""+read);
                }
                else {
                    holder.my_read.setText("");
                }
                break;
            case 1: // 상대방 메세지
                holder.f_layout.setVisibility(View.VISIBLE);
                holder.my_layout.setVisibility(View.GONE);
                holder.coming_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.f_txt.setText(msg);
                if(img.equals("null")==true){
                    holder.f_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+img).into(holder.f_img);
                }
                holder.f_name.setText(name);
                if(read>0){
                    holder.f_read.setText(""+read);
                }
                else {
                    holder.f_read.setText("");
                }
                holder.f_time.setText(time);
                break;
            case 2: // 초대, 나가기 상태 메세지
                holder.coming_layout.setVisibility(View.VISIBLE);
                holder.f_layout.setVisibility(View.GONE);
                holder.my_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.coming_txt.setText(msg);
                break;
            case 3: // 나의 이미지 메세지
                holder.coming_layout.setVisibility(View.GONE);
                holder.f_layout.setVisibility(View.GONE);
                holder.my_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.VISIBLE);
                if(read>0){
                    holder.my_msg_img_read.setText(""+read);
                }
                else {
                    holder.my_msg_img_read.setText("");
                }

                if(msg.equals("null")==true){
                    holder.my_msg_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+msg).into(holder.my_msg_img);
                }
                holder.my_msg_img_time.setText(time);
                break;
            case 4: // 상대방 이미지 메세지
                holder.coming_layout.setVisibility(View.GONE);
                holder.f_layout.setVisibility(View.GONE);
                holder.my_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.VISIBLE);
                holder.my_img_layout.setVisibility(View.GONE);
                if(read>0) {
                    holder.f_msg_img_read.setText(""+read);
                }
                else {
                    holder.f_msg_img_read.setText("");
                }

                if(img.equals("null")==true){
                    holder.f_msg_img_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+img).into(holder.f_msg_img_img);
                }
                if(msg.equals("null")==true){
                    holder.f_msg_img_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+msg).into(holder.f_msg_img);
                }
                holder.f_msg_img_name.setText(name);
                holder.f_msg_img_time.setText(time);
                break;
            case 5: // 상대방 방송시작
                holder.f_layout.setVisibility(View.VISIBLE);
                holder.my_layout.setVisibility(View.GONE);
                holder.coming_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.f_txt.setTextColor(Color.RED); // 글자 빨간색으로
                holder.f_txt.setText(msg);
                if(img.equals("null")==true){
                    holder.f_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+img).into(holder.f_img);
                }
                holder.f_name.setText(name);
                if(read>0){
                    holder.f_read.setText(""+read);
                }
                else {
                    holder.f_read.setText("");
                }
                holder.f_time.setText(time);
                holder.f_txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            socketChannel = ((Chatroom) mContext).socketChannel;
                            socketChannel.close(); // chatroom class 소켓 종료

                            String streamID = chatrooms.get(i).friendEmail; // stream 방 ID값을 friendEmail에 넣어둠
                            Log.i("streamID값", ""+streamID);
                            Intent intent = new Intent(mContext, PlayerActivity.class);
                            intent.putExtra("방이름",RoomName);
                            intent.putExtra("방ID", chatID); // 채팅방 ID값
                            intent.putExtra("streamID",streamID); // 스트리밍 아이디값
                            mContext.startActivity(intent);
                            ((Chatroom) mContext).finish();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case 6: // 상대방 방송 종료
                holder.f_layout.setVisibility(View.VISIBLE);
                holder.my_layout.setVisibility(View.GONE);
                holder.coming_layout.setVisibility(View.GONE);
                holder.my_img_layout.setVisibility(View.GONE);
                holder.f_img_layout.setVisibility(View.GONE);
                holder.f_txt.setTextColor(Color.RED); // 글자 빨간색으로
                holder.f_txt.setText(msg);
                if(img.equals("null")==true){
                    holder.f_img.setImageResource(R.drawable.profile);
                }
                else {
                    Glide.with(mContext).load("http://115.71.239.124"+img).into(holder.f_img);
                }
                holder.f_name.setText(name);
                if(read>0){
                    holder.f_read.setText(""+read);
                }
                else {
                    holder.f_read.setText("");
                }
                holder.f_time.setText(time);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }
}
