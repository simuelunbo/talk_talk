package com.example.sjb.talk_talk.activity;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.sjb.talk_talk.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name2);
        txtEmail = (TextView) findViewById(R.id.email2);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        Intent intent = new Intent(this.getIntent());
        String name = intent.getStringExtra("userName");
        String email = intent.getStringExtra("userEmail");
        txtName.setText(name+"님");
        txtEmail.setText(email);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut(); // 구글 소셜 로그인 로그아웃

                Intent intent1 = new Intent(MainActivity.this,Login.class);
                MainActivity.this.startActivity(intent1);
                finish();

            }
        });
    }

}