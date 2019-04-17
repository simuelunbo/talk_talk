package com.example.sjb.talk_talk.Item;

public class Item_ChattingProfile {

    public String email;
    public String name;
    public String profile_img;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public Item_ChattingProfile(String email, String name, String profile_img)
    {
        this.email = email;
        this.profile_img=profile_img;
        this.name = name;
    }
}
