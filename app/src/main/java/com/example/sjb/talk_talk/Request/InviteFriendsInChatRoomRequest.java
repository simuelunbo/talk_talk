package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class InviteFriendsInChatRoomRequest extends StringRequest {
    final static private String URL = "http://115.71.239.124/talktalk/invite_friends_chat.php";
    private Map<String, String> parameters;

    public InviteFriendsInChatRoomRequest(String roomID, String changeroomID, String friendslist, String time , Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("roomID", roomID);
        parameters.put("changeroomID", changeroomID);
        parameters.put("friendslist", friendslist);
        parameters.put("time", time);
    }
    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
