package com.example.sjb.talk_talk.Item;

public class Item_Chat_find_friend {
    public String email;
    public String profile_img;
    public String name;
    public String status_msg;
    public String google;
    public boolean isSelected;

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

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean isSelected){
        this.isSelected =isSelected;
    }

    public Item_Chat_find_friend(String email, String google, String profile_img, String name, String status_msg)
    {
        this.email=email;
        this.google=google;
        this.profile_img=profile_img;
        this.name = name;
        this.status_msg = status_msg;
        this.isSelected = isSelected;
    }
}
