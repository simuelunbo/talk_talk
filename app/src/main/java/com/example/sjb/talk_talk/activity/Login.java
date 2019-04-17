package com.example.sjb.talk_talk.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Request.GoogleloginRequest;
import com.example.sjb.talk_talk.Request.LoginRequest;
import com.example.sjb.talk_talk.Request.RegisterRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class Login extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient; //구글 api 클라이언트
    private int RC_SIGN_IN=10; // 구글로그인 result 상수
    private FirebaseAuth mAuth; // 파이어베이스 인증 객체 생성
    Bitmap bitmap;
    String userImageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("login_user", MODE_PRIVATE);

        if (preferences.getString("user_Email", "dd").equals("dd")==true) { // sharedpreferences에 userEmail값이 없을 경우 로그인 창 띄우기

            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();// 파이어베이스 인증 객체 선언

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    // Google 로그인을 앱에 통합
                    // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            final EditText emailText = (EditText) findViewById(R.id.loginEmail);
            final EditText passwordText = (EditText) findViewById(R.id.loginPW);

            Button loginbtn = (Button) findViewById(R.id.btnLogin);
            Button registerbtn = (Button) findViewById(R.id.btnLinkToRegisterScreen);
            SignInButton googlebtn = (SignInButton) findViewById(R.id.btn_googleSignIn);

            googlebtn.setOnClickListener(new View.OnClickListener() {// 버튼을 누르게 되면 구글 사용자니 판별하기 위해서 보냄
                @Override
                public void onClick(View v) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
            registerbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent registerIntent = new Intent(Login.this, Register.class);
                    Login.this.startActivity(registerIntent);
                }
            });
            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String userEmail = emailText.getText().toString();
                    final String userPassword = passwordText.getText().toString();


                    //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");

                                //서버에서 보내준 값이 true이면?
                                if (success) {

                                    String userName = jsonResponse.getString("userName");
                                    String useremail = jsonResponse.getString("userEmail");
                                    String google = jsonResponse.getString("google");
                                    //로그인에 성공했으므로 MainActivity로 넘어감
                                    Intent intent = new Intent(Login.this, Talktalk_main.class);

                                    SharedPreferences sp = getSharedPreferences("login_user", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("user_Email", useremail);
                                    editor.putString("user_Name", userName);
                                    editor.putString("google", google);

                                    editor.commit();

                                    Login.this.startActivity(intent);
                                    finish();

                                } else {//로그인 실패시
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                    builder.setMessage("아이디 또는 비밀번호를 다시 입력하세요")
                                            .setNegativeButton("확인", null)
                                            .create()
                                            .show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    LoginRequest loginRequest = new LoginRequest(userEmail, userPassword, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(Login.this);
                    queue.add(loginRequest);
                }
            });
        }
        else {
            Intent intent = new Intent(Login.this,Talktalk_main.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글 로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //구글 로그인 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); // 구글 사용자가 맞으면 파이어 베이스로 넘김
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
        // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null); //파이어베이스에서 유저의 정보(토큰)을 받아서
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

//                            updateUI(user);
                            if (user != null) {
                                // Name, email address, and profile photo Url
                                final String userName = user.getDisplayName();
                                final String userEmail = user.getEmail();
                                String userPassword = user.getUid(); // uid 값을 userpassword값에 넣을 생각
                                final Uri photoUrl = user.getPhotoUrl(); // 구글 프로필 사진

                                Log.i("구글프로필 사진", ""+photoUrl.toString()); //인터넷 주소

                                Thread mThread = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            URL url = new URL(photoUrl.toString());
                                            // Web에서 이미지를 가져온 뒤
                                            // ImageView에 지정할 Bitmap을 만든다
                                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                            conn.setDoInput(true); // 서버로 부터 응답 수신
                                            conn.connect();

                                            InputStream is = conn.getInputStream(); // InputStream 값 가져오기
                                            bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환
                                            userImageData = imageToString(bitmap); // 구글 프로필 비트맵 사진을 string값으로 변환


                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                            Log.i("error", ""+e);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.i("error", ""+e);
                                        }
                                    }
                                };

                                mThread.start(); // Thread 실행


                            //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
                            Response.Listener<String> responseListener = new Response.Listener<String>(){

                                //서버로부터 여기서 데이터를 받음
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        //서버로부터 받는 데이터는 JSON타입의 객체이다.
                                        JSONObject jsonResponse = new JSONObject(response);
                                        //그중 Key값이 "success"인 것을 가져온다.
                                        boolean success = jsonResponse.getBoolean("success");

                                        //회원 등록과 소셜 로그인 성공
                                        if(success){
                                            Intent intent = new Intent(Login.this, Talktalk_main.class);

                                            SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("user_Email",userEmail);
                                            editor.putString("user_Name",userName);
                                            editor.putString("google","yes");

                                            editor.commit();

                                            Login.this.startActivity(intent);
                                            finish();

                                        }
                                        //소셜 로그인 성공 이미 회원이 등록된 사람이기 때문에 success를 false로 받음
                                        else{
                                            Intent intent = new Intent(Login.this, Talktalk_main.class);

                                            SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("user_Email",userEmail);
                                            editor.putString("user_Name",userName);
                                            editor.putString("google","yes");

                                            editor.commit();

                                            Login.this.startActivity(intent);
                                            finish();
                                        }

                                    }catch(JSONException e){
                                        e.printStackTrace();
                                    }

                                }
                            };//responseListener 끝

                                try {
                                    mThread.join(); //쓰레드 끝나면 바로 volley로 보내기

                                    GoogleloginRequest googleloginRequest = new GoogleloginRequest(userEmail, userPassword, userName, userImageData, responseListener);
                                    RequestQueue queue = Volley.newRequestQueue(Login.this);
                                    queue.add(googleloginRequest);
                                    }
                                    catch (Exception e){

                                    }
                            }
                        }
                        else {
                            // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }
    private String imageToString(Bitmap bitmap) {//비트맵 이미지를 Base64 string값으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
