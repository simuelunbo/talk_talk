package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomMsgUpdateRequest extends StringRequest {
    final static private String URL = "http://115.71.239.124/talktalk/chatroom_msg_update.php";
    private Map<String, String> parameters;

    public ChatRoomMsgUpdateRequest(String roomID, String lastMsg, String time, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("roomID", roomID);
        parameters.put("lastMsg", lastMsg);
        parameters.put("time", time);
    }
    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
