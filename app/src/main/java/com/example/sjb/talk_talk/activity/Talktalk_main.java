package com.example.sjb.talk_talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Service.Chat_service;
import com.example.sjb.talk_talk.fragment.calls;
import com.example.sjb.talk_talk.fragment.chats;
import com.example.sjb.talk_talk.fragment.contacts;
import com.example.sjb.talk_talk.fragment.etc;

public class Talktalk_main extends AppCompatActivity {

    ViewPager pager;
    pagerAdapter mAdapter;
    Button btn_second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.talktalk_main);


        mAdapter = new pagerAdapter(getSupportFragmentManager());

        pager = (ViewPager)findViewById(R.id.pager);
        Button btn_first = (Button)findViewById(R.id.contacts); // 연락처
        btn_second = (Button)findViewById(R.id.chats);
        Button btn_third = (Button)findViewById(R.id.calls);
        ImageButton btn_forth = (ImageButton)findViewById(R.id.dotdotdot);

        pager.setAdapter(mAdapter);
        //pager.setCurrentItem 이 페이지 이동해줌
        Intent intent = new Intent(this.getIntent());
        String s = intent.getStringExtra("chats");
        if(s != null){
            pager.setCurrentItem(1);
        }
        else {
            pager.setCurrentItem(0);
        }

        View.OnClickListener movePageListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                int tag = (int)view.getTag();
                pager.setCurrentItem(tag);
            }
        };


        btn_first.setOnClickListener(movePageListener);
        btn_first.setTag(0);
        btn_second.setOnClickListener(movePageListener);
        btn_second.setTag(1);
        btn_third.setOnClickListener(movePageListener);
        btn_third.setTag(2);
        btn_forth.setOnClickListener(movePageListener);
        btn_forth.setTag(3);
    }

    public void refresh(){
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();

        Intent intent1 = new Intent(Talktalk_main.this,Chat_service.class);
        stopService(intent1);
        Log.i("서비스 종료", "서비스 종료 Talktalk_main 부분");

        Intent intent2 = new Intent(Talktalk_main.this,Chat_service.class);
        startService(intent2);
        Log.i("서비스 시작", "서비스 시작 Talktalk_main 부분");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    private class pagerAdapter extends FragmentStatePagerAdapter
    {
        public pagerAdapter(FragmentManager fm )
        {
            super(fm);
            FragmentTransaction ft = fm.beginTransaction();
            ft.detach(this.getItem(3)).attach(this.getItem(3)).commit();
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 0:
                    return new contacts();
                case 1:
                    return new chats();
                case 2:
                    return new calls();
                case 3:
                    return new etc();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // total page count
            return 4;
        }
    }

}
