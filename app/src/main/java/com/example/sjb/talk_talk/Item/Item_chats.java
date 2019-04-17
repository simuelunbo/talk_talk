package com.example.sjb.talk_talk.Item;

public class Item_chats {

    public String room_num;

    public String profile_img;
    public String name;
    public String msg;
//    public String f_google;
//    public String f_email;
    public String time;
    public boolean stopnamechange;


    public String getName(){
        return name;
    }
    public String getmsg(){
        return msg;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public String getTime() {
        return time;
    }

    public String getRoom_num() {
        return room_num;
    }

    public boolean isStopnamechange() {
        return stopnamechange;
    }

    public Item_chats(String room_num, String profile_img, String name, String msg, String time,boolean stopnamechange)
    {
        this.room_num = room_num;
        this.profile_img=profile_img;
        this.name = name;
        this.msg = msg;
        this.time = time;
        this.stopnamechange = stopnamechange;
    }
}
