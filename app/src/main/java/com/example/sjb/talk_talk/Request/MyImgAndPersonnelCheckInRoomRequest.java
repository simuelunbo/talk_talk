package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MyImgAndPersonnelCheckInRoomRequest extends StringRequest {
    final static private String URL = "http://115.71.239.124/talktalk/MyImgAndPersonnelCheckInRoomRequest.php";
    private Map<String, String> parameters;

    public MyImgAndPersonnelCheckInRoomRequest(String chatID, String userEmail, String userName, String google, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("chatID", chatID);
        parameters.put("userEmail", userEmail);
        parameters.put("userName", userName);
        parameters.put("google", google);
    }

    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
