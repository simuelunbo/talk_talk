package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.example.sjb.talk_talk.activity.Chat_find_friend;

import java.util.HashMap;
import java.util.Map;

public class Chat_find_friendRequest extends StringRequest {

    final static private String URL = "http://115.71.239.124/talktalk/chat_find_friend.php";
    private Map<String, String> parameters;

    public Chat_find_friendRequest(String friendname, String userEmail, String userGoogle, Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("friendName", friendname);
        parameters.put("userEmail", userEmail);
        parameters.put("userGoogle", userGoogle);
    }
    @Override
    protected Map<String, String> getParams() throws AuthFailureError{
        return parameters;
    }
}
