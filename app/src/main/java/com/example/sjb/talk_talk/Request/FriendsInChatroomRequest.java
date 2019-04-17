package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FriendsInChatroomRequest extends StringRequest {

    final static private String URL = "http://115.71.239.124/talktalk/FriendsInChatroom.php";
    private Map<String, String> parameters;

    public FriendsInChatroomRequest(String username, String useremail ,String friendlist ,String roomname ,Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("useremail",useremail);
        parameters.put("friendlist", friendlist);
        parameters.put("roomname", roomname);
    }
    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
