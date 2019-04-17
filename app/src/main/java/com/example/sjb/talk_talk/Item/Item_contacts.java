package com.example.sjb.talk_talk.Item;


public class Item_contacts {

    public String email;
    public String profile_img;
    public String name;
    public String status_msg;
    public String google;

    public String getGoogle() {
        return google;
    }

    public String getEmail() {
        return email;
    }

    public String getName(){
        return name;
    }
    public String getStatus_msg(){
        return status_msg;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public Item_contacts(String email, String google, String profile_img, String name, String status_msg)
    {
        this.email=email;
        this.google=google;
        this.profile_img=profile_img;
        this.name = name;
        this.status_msg = status_msg;
    }
}
