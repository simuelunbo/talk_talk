package com.example.sjb.talk_talk.RecyclerviewAdapter;

import android.content.Context;
import android.content.DialogInterface;
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
import com.example.sjb.talk_talk.activity.Talktalk_main;
import com.example.sjb.talk_talk.fragment.contacts;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Adapter_contacts extends RecyclerView.Adapter<Holder_contacts>{
    ArrayList<Item_contacts> item_contact;
    Context mContext;
    String userName,userEmail,usergoogle,friendEmail, friendName, friendGoogle;
    public Adapter_contacts(ArrayList contact){
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
        holder.mcardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu p = new PopupMenu(mContext,v);
                p.inflate(R.menu.select_friend);
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.friend1:
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                                builder.setTitle("차단")
                                        .setMessage("차단하시겠습니까? 차단하면 차단한 친구가 보내는 메세지를 받을 수 없으며, 친구목록에서 삭제됩니다.")
                                        .setCancelable(false)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Response.Listener<String> responseListener = new Response.Listener<String>() {

                                                    //서버로부터 여기서 데이터를 받음
                                                    @Override
                                                    public void onResponse(String response) {
                                                        try {
                                                            //서버로부터 받는 데이터는 JSON타입의 객체이다.
                                                            JSONObject jsonResponse = new JSONObject(response);
                                                            //그중 Key값이 "success"인 것을 가져온다.
                                                            boolean success = jsonResponse.getBoolean("success");
                                                            //상태 메세지 등록 성공시 success값이 true임
                                                            if (success) {
                                                                item_contact.remove(position);
                                                                notifyItemRemoved(position);
                                                                notifyItemRangeChanged(position, item_contact.size()); // 삭제

                                                            }
                                                            //상태 메세지 등록 실패시 응답이 success값이 false
                                                            else {
                                                                Toast.makeText(mContext.getApplicationContext(), "일시적인 오류입니다 다시 한번 등록 해주세요", Toast.LENGTH_SHORT).show();
                                                            }

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            //알림상자를 만들어서 보여줌
                                                        }
                                                    }
                                                };//responseListener 끝

                                                //volley 사용법
                                                //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
                                                Block_friendRequest msg_changeRequest = new Block_friendRequest(userEmail, userName, usergoogle,
                                                        item_contact.get(position).email, item_contact.get(position).name , item_contact.get(position).google,responseListener);
                                                //2. RequestQueue를 생성한다.
                                                RequestQueue queue = Volley.newRequestQueue(mContext);
                                                //3. RequestQueue에 RequestObject를 넘겨준다.
                                                queue.add(msg_changeRequest);

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
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                p.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return item_contact.size();
    }
}
