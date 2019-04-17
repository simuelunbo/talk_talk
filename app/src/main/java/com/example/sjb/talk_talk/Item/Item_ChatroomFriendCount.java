package com.example.sjb.talk_talk.Item;

public class Item_ChatroomFriendCount {
    public String friendEmail;
    public String friendName;

    public String getFriendEmail() {
        return friendEmail;
    }

    public String getFriendName() {
        return friendName;
    }
    public Item_ChatroomFriendCount(String friendEmail, String friendName){
        this.friendEmail = friendEmail;
        this.friendName = friendName;
    }
}
