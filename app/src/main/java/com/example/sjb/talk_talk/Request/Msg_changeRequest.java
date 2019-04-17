package com.example.sjb.talk_talk.Request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class Msg_changeRequest extends StringRequest {

    final static private String URL = "http://115.71.239.124/talktalk/msg_request.php";
    private Map<String, String> parameters;

    public Msg_changeRequest(String userEmail, String userName, String google, String send_msg, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        Log.i("사용자 유저111111", ""+userName);
        Log.i("사용자 유저111111", ""+send_msg);
        parameters.put("userEmail", userEmail);
        parameters.put("userName", userName);
        parameters.put("google", google);
        parameters.put("status_msg", send_msg);
    }

    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }

}
