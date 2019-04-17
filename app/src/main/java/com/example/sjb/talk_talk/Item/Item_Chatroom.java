package com.example.sjb.talk_talk.Item;

public class Item_Chatroom {
    public String friendEmail;
    public String friend;
    public String chat;
    public String friendimg;
    public int who;
    public String time;
    public int Read;
    public Boolean Readcheck;
    public String chatID;

    public String getFriendEmail() {
        return friendEmail;
    }

    public String getFriend() {
        return friend;
    }

    public String getChat() {
        return chat;
    }

    public int getWho() {
        return who;
    }

    public String getTime() {return time;}

    public int getRead() {
        return Read;
    }

    public Boolean getReadcheck() {
        return Readcheck;
    }

    public String getFriendimg() {
        return friendimg;
    }

    public String getChatID() {
        return chatID;
    }

    public Item_Chatroom(String chatID, String friendEmail, String friend, String friendimg, String chat, int who, String time, int Read, Boolean Readcheck){
        this.chatID = chatID;
        this.friendEmail =friendEmail;
        this.friend = friend;
        this.friendimg = friendimg;
        this.chat = chat;
        this.who = who;
        this.time = time;
        this.Read = Read;
        this.Readcheck = Readcheck;
    }
}
