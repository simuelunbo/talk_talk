package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LeaveTheRoomRequst extends StringRequest {
    final static private String URL = "http://115.71.239.124/talktalk/leavetheroom.php";
    private Map<String, String> parameters;

    public LeaveTheRoomRequst(String roomID, String changeroomID, String userEmail, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("roomID", roomID);
        parameters.put("changeroomID", changeroomID);
        parameters.put("email", userEmail);
    }
    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
