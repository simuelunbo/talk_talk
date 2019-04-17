package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class Find_friend_nameRequest extends StringRequest {

    final static private String URL = "http://115.71.239.124/talktalk/find_friend_name.php";
    private Map<String, String> parameters;

    public Find_friend_nameRequest(String userEmail, String userName, String userGoogle, String friendName, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userEmail", userEmail);
        parameters.put("userName", userName);
        parameters.put("userGoogle", userGoogle);
        parameters.put("friendName", friendName);
    }
    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
