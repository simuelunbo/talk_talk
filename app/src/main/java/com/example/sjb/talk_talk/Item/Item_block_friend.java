package com.example.sjb.talk_talk.Item;

public class Item_block_friend {
    public String friend_profile_img;
    public String friend_name;
    public String friend_email;
    public String friend_google;

    public String getFriend_name(){
        return friend_name;
    }
    public String getFriend_email(){
        return friend_email;
    }

    public String getFriend_profile_img() {
        return friend_profile_img;
    }

    public String getFriend_google(){
        return friend_google;
    }

    public Item_block_friend(String friend_profile_img, String friend_name, String friend_email, String friend_google)
    {
        this.friend_email=friend_email;
        this.friend_name = friend_name;
        this.friend_profile_img = friend_profile_img;
        this.friend_google = friend_google;
    }
}
