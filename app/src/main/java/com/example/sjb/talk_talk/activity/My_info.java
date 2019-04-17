package com.example.sjb.talk_talk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.sjb.talk_talk.R;
import com.example.sjb.talk_talk.Request.Image_UploadRequest;
import com.example.sjb.talk_talk.Request.Session_Request;
import com.example.sjb.talk_talk.fragment.etc;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class My_info extends AppCompatActivity {

    private  static final int MY_PERMISSION_CAMERA = 1111;
    private  static final int REQUEST_TAKE_PHOTO = 2222;
    private  static  final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    Uri imageUri;
    Uri photoURI, albumURI;
    Bitmap bitmap;

    ImageView my_img;
    LinearLayout my_name, my_msg;
    TextView modify_my_name, modify_my_msg;
    String recieve_img,userName,mCurrentPhotoPath,userEmail,google;
    String send_img_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_info);

        checkPermission();

        my_img = (ImageView) findViewById(R.id.modify_my_img);
        modify_my_name = (TextView) findViewById(R.id.modify_my_name);
        modify_my_msg = (TextView) findViewById(R.id.modify_my_msg);

        my_name = (LinearLayout) findViewById(R.id.linearlayout_name);
        my_msg = (LinearLayout) findViewById(R.id.linearlayout_msg);

        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

        userName = sp.getString("user_Name","");
        userEmail = sp.getString("user_Email","");
        google = sp.getString("google","");

        modify_my_name.setText(userName);

        my_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getApplicationContext(),v);
                getMenuInflater().inflate(R.menu.select_img, p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.m1:
                                captureCamera();
                                break;
                            case R.id.m2:
                                getAlbum();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                p.show();
            }
        });
        my_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(My_info.this,Msg_change.class);
                intent.putExtra("text",String.valueOf(modify_my_msg.getText()));
                startActivityForResult(intent, 10);
            }
        });
        //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    //서버에서 보내준 값이 true이면?
                    if (success) {

                        recieve_img = "http://115.71.239.124"+jsonResponse.getString("profile"); // 주소값
                        String msg = jsonResponse.getString("status_msg");// 상태메세지
//
//                        Log.i("상태메세지 null값인가 확인", ""+msg);
                        if(msg.equals("null")==true)
                        {
                            modify_my_msg.setText("");
                        }
                        else {
                            modify_my_msg.setText(msg);
                        }

//                        Log.i("이미지 주소값", ""+ recieve_img);
                        if(recieve_img.equals("http://115.71.239.124null")==true)
                        {
                            my_img.setImageResource(R.drawable.profile);
                        }
                        else {
                            Glide.with(My_info.this).load(recieve_img).into(my_img);
                        }
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Session_Request sessionRequest = new Session_Request(userEmail, userName, google, responseListener);
        RequestQueue queue = Volley.newRequestQueue(My_info.this);
        queue.add(sessionRequest);
    }


    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함

                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File createImageFile() throws IOException {//저장경로 파일
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "sjb");

        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }
    private void getAlbum(){
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
    public void cropImage(){
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Log.i("REQUEST_TAKE_PHOTO", "OK");

                        galleryAddPic();
                        File albumFile = null;
                        albumFile = createImageFile();
                        photoURI = imageUri;
                        albumURI = Uri.fromFile(albumFile);
                        cropImage();

//                        iv_view.setImageURI(imageUri);
                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                } else {
                    Toast.makeText(My_info.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {

                    if(data.getData() != null){
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        }catch (Exception e){
                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    galleryAddPic();
                    Log.i("album", albumURI+"<<<<<<<<<<<<<<<albumURI");
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(albumURI);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        my_img.setImageBitmap(bitmap);
                        String imageData = imageToString(bitmap);

                        SharedPreferences sp = getSharedPreferences("login_user",MODE_PRIVATE);

                        userName = sp.getString("user_Name","");
                        userEmail = sp.getString("user_Email","");
                        google = sp.getString("google","");

                        //4. 콜백 처리부분(volley 사용을 위한 ResponseListener 구현 부분)
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");

                                    //서버에서 보내준 값이 true이면?
                                    if (success) {

                                        send_img_url = "http://sjb6113.vps.phps.kr"+jsonResponse.getString("profile");
                                        Log.i("이미지 주소", ""+send_img_url);
//                                        Glide.with(My_info.this).load(send_img_url).into(my_img);

                                    } else {

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        Image_UploadRequest image_uploadRequest = new Image_UploadRequest(userEmail, userName, google, imageData, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(My_info.this);
                        queue.add(image_uploadRequest);
                    }
                    catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
//                    my_img.setImageURI(albumURI);

                }
                break;
            case 10: // 상태메세지
                if (resultCode == RESULT_OK) {
                    String status_msg = data.getStringExtra("msg");
                    modify_my_msg.setText(status_msg);
                }
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(My_info.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서..
                break;
        }
    }

    private String imageToString(Bitmap bitmap) {//비트맵 이미지를 Base64 string값으로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
