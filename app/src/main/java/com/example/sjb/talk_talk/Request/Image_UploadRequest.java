package com.example.sjb.talk_talk.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class Image_UploadRequest extends StringRequest {

    final static private String URL = "http://115.71.239.124/talktalk/update_profile_img.php";
    private Map<String, String> parameters;

    public Image_UploadRequest(String userEmail, String userName, String google, String imageData, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userEmail", userEmail);
        parameters.put("userName", userName);
        parameters.put("google", google);
        parameters.put("userImageData", imageData);
    }

    //추후 사용을 위한 부분
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
