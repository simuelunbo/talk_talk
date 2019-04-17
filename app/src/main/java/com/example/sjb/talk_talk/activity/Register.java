package com.example.sjb.talk_talk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Request.RegisterRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText passwordText = (EditText)findViewById(R.id.password);
        final EditText nameText = (EditText)findViewById(R.id.name);
        final EditText emailText = (EditText)findViewById(R.id.email);

        Button regbtn = (Button)findViewById(R.id.btnRegister);
        Button btnLinkToLogin = (Button)findViewById(R.id.btnLinkToLoginScreen);

        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        regbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String userPassword = passwordText.getText().toString();
                String userName = nameText.getText().toString();
                String userEmail = emailText.getText().toString();

                if (userPassword.trim().length()==0 || userName.trim().length()==0 || userEmail.trim().length()==0) {
                    Toast.makeText(getApplicationContext(), "이메일, 비밀번호, 이름을 빠짐 없이 입력 해주세요", Toast.LENGTH_SHORT).show();
                } else {
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
                                //회원 가입 성공시 success값이 true임
                                if (success) {
                                    Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                                    //알림상자를 만들어서 보여줌
                                    finish();
                                }
                                //회원 가입 실패시 응답이 fail로 온다
                                else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                    builder.setMessage("이미 등록된 이메일 입니다.")
                                            .setNegativeButton("ok", null)
                                            .create()
                                            .show();
                                    Toast.makeText(getApplicationContext(),jsonResponse.toString(),Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                //알림상자를 만들어서 보여줌
                                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                                builder.setMessage("이미 등록된 이메일 입니다.")
                                        .setNegativeButton("ok", null)
                                        .create()
                                        .show();
//                                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                            }

                        }
                    };//responseListener 끝

                    //volley 사용법
                    //1. RequestObject를 생성한다. 이때 서버로부터 데이터를 받을 responseListener를 반드시 넘겨준다.
                    RegisterRequest registerRequest = new RegisterRequest(userPassword, userName, userEmail, responseListener);
                    //2. RequestQueue를 생성한다.
                    RequestQueue queue = Volley.newRequestQueue(Register.this);
                    //3. RequestQueue에 RequestObject를 넘겨준다.
                    queue.add(registerRequest);

                }
            }
        });

    }
}
