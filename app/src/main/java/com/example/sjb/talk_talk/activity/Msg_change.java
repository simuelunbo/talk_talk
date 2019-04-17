package com.example.sjb.talk_talk.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Request.Msg_changeRequest;
import com.example.sjb.talk_talk.Request.RegisterRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Msg_change extends AppCompatActivity {

    String userName,userEmail,google;

    EditText msg_box;
    Button save;
    ImageButton Xbutton;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_change);
        Log.i("상태메세지", "Msg_change 클래스");
        msg_box = (EditText)findViewById(R.id.modify_my_msg2);
        save = (Button)findViewById(R.id.save_msg);
        Xbutton = (ImageButton)findViewById(R.id.X_delete);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        Log.i("사용자 이름", ""+userName);

        final Intent intent = new Intent(this.getIntent());
        String s = intent.getStringExtra("text");
        msg_box.setText(s); // My_info 클래스에서 인텐트로 넘겨받은 상태 메세지 값

//        send_msg = msg_box.getText().toString(); // EditText에 넣은 값

        Xbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg_box.getText().clear();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("통과???333333","통과??????");

                //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
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
                                Intent intent1 = new Intent();
                                intent1.putExtra("msg",msg_box.getText().toString());
                                setResult(RESULT_OK, intent1);
                                finish();
                            }
                            //상태 메세지 등록 실패시 응답이 success값이 false
                            else {
                                Toast.makeText(getApplicationContext(), "일시적인 오류입니다 다시 한번 등록 해주세요", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //알림상자를 만들어서 보여줌
                        }
                    }
                };//responseListener 끝

                //volley 사용법
                //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
                Msg_changeRequest msg_changeRequest = new Msg_changeRequest(userEmail, userName, google, msg_box.getText().toString(), responseListener);
                Log.i("통과???1111","통과??????");
                //2. RequestQueue를 생성한다.
                RequestQueue queue = Volley.newRequestQueue(Msg_change.this);
                //3. RequestQueue에 RequestObject를 넘겨준다.
                Log.i("통과???2222222","통과??????");
                queue.add(msg_changeRequest);
            }
        });
    }
}
