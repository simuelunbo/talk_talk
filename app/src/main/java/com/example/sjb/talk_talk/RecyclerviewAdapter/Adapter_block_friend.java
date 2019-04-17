package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.Item.Item_block_friend;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_block_friend;
import com.example.sjb.talk_talk.RecyclerviewHolder.Holder_find_friend;
import com.example.sjb.talk_talk.Request.Add_friendRequest;
import com.example.sjb.talk_talk.Request.Unblock_friendRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class Adapter_block_friend extends RecyclerView.Adapter<Holder_block_friend>{

    ArrayList<Item_block_friend> block_friends;
    Context mContext;
    String userName, userEmail, userGoogle;

    public Adapter_block_friend(ArrayList block){
        block_friends = block;
    }

    @Override
    public Holder_block_friend onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_friend_recycler,parent,false);
        mContext = parent.getContext();
        Holder_block_friend holder_block_friend = new Holder_block_friend(view);
        return holder_block_friend;
    }

    @Override
    public void onBindViewHolder(Holder_block_friend holder, final int position) {

        SharedPreferences sp = mContext.getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        userGoogle = sp.getString("google","");

        Holder_block_friend holder_block_friend = (Holder_block_friend) holder;
        ImageView imageView = ((Holder_block_friend) holder).friend_img;
        String currentUrl = block_friends.get(position).friend_profile_img;
        if(currentUrl.equals("null")==true)
        {
            imageView.setImageResource(R.drawable.profile);
        }
        else {
            Glide.with(mContext).load("http://115.71.239.124" + currentUrl).into(imageView); //glide 웹 url
        }
        holder.friend_name.setText(block_friends.get(position).friend_name);
        holder.friend_email.setText(block_friends.get(position).friend_email);
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("차단해제")
                        .setMessage("차단해제 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Response.Listener<String> responseListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.i("response값", ""+response);
                                        try {
                                            JSONObject jsonResponse = new JSONObject(response);
                                            boolean success = jsonResponse.getBoolean("success");

                                            //서버에서 보내준 값이 true이면?
                                            if (success) {
                                                block_friends.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, block_friends.size());
                                            } else {

                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                Unblock_friendRequest addFriendRequest = new Unblock_friendRequest(userEmail, userName, userGoogle, block_friends.get(position).friend_email,
                                        block_friends.get(position).friend_name, block_friends.get(position).friend_google, responseListener);
                                RequestQueue queue = Volley.newRequestQueue(mContext);
                                queue.add(addFriendRequest);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog =builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
       return block_friends.size();
    }
}
