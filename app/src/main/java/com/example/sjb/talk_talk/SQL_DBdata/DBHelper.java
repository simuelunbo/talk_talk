package com.example.sjb.talk_talk.SQL_DBdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.sjb.talk_talk.Item.Item_Chatroom;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {// 테이블 생성
        // 채팅방 ID값, 친구 이메일, 친구 이름, 친구 이미지, 메세지, 누구인지 구분값, 시간, 읽은 유저 표시, 읽었는지 안읽었는지 구분
        db.execSQL("CREATE TABLE Chatroom_record (chatID TEXT, friendEmail TEXT, friendName TEXT, " +
                "friendimg TEXT, chat TEXT, who INTEGER, time TEXT, read INTEGER, readcheck INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String chatID, String friendEmail, String friendName, String friendimg, String chat, int who, String time, int read, int readcheck ){
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO Chatroom_record VALUES('"+ chatID +"','"+ friendEmail +"','"+ friendName + "','" + friendimg + "','"+ chat +"','" + who + "','" + time + "','" + read + "','" + readcheck + "');");
        db.close();
    }

    public void readupdate(String chatID, int read) { // 읽음표시 업데이트 int형은 ''표시 안함
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 정보 수정 (해당 채팅방이고 메세지 readcheck값이 1일때 read값을 -1 감소시키고 readcheck값을 0으로 바꿈)

        db.execSQL("UPDATE Chatroom_record SET read=" + read + " WHERE chatID='" + chatID + "';");

        db.close();
    }
    public void readupdate2(String chatID, int read, int readcheck, int readcheckone) { // 읽음표시 업데이트 int형은 ''표시 안함
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 정보 수정 (해당 채팅방이고 메세지 readcheck값이 1일때 read값을 -1 감소시키고 readcheck값을 0으로 바꿈)

        db.execSQL("UPDATE Chatroom_record SET read=" + read + ", readcheck = " + readcheck +" WHERE chatID='" + chatID + "' AND readcheck = "+ readcheckone + ";");

        db.close();
    }

    public void chatID_update(String chatID, String changechatID) { // 어느 유저가 초대하거나 나갈때 방 ID값 변경
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE Chatroom_record SET chatID='" + changechatID + "' WHERE chatID='" + chatID + "';");
        db.close();
    }

    public void delete(String chatID) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM Chatroom_record WHERE chatID='" + chatID + "';");
        db.close();
    }

    public ArrayList<Item_Chatroom> getResult(String chatID) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Item_Chatroom> msg = new ArrayList<>();
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM Chatroom_record WHERE chatID ='" + chatID + "'", null);
        while (cursor.moveToNext()) {
            try {
                Item_Chatroom item_chatroom = new Item_Chatroom("", "", "", "", "", 100, "", 0, false);
                item_chatroom.chatID = cursor.getString(0);
                item_chatroom.friendEmail = cursor.getString(1);
                item_chatroom.friend = cursor.getString(2);
                item_chatroom.friendimg = cursor.getString(3);
                item_chatroom.chat = cursor.getString(4);
                item_chatroom.who = cursor.getInt(5);
                item_chatroom.time = cursor.getString(6);
                item_chatroom.Read = cursor.getInt(7);
                if (cursor.getInt(8) == 0) {// 0일때 참
                    item_chatroom.Readcheck = true;
                } else { // 1일때 거짓
                    item_chatroom.Readcheck = false;
                }
                msg.add(item_chatroom);
            }
            catch (Exception e){
                Log.i("getResult() ERROR", ""+e);
            }
        }
        cursor.close();
        db.close();
        return msg;
    }
    public ArrayList<Item_Chatroom> Allresult(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Item_Chatroom> msg = new ArrayList<>();
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM Chatroom_record", null);
        while (cursor.moveToNext()) {
            try {
                Item_Chatroom item_chatroom = new Item_Chatroom("", "", "", "", "", 100, "", 0, false);
                item_chatroom.chatID = cursor.getString(0);
                item_chatroom.friendEmail = cursor.getString(1);
                item_chatroom.friend = cursor.getString(2);
                item_chatroom.friendimg = cursor.getString(3);
                item_chatroom.chat = cursor.getString(4);
                item_chatroom.who = cursor.getInt(5);
                item_chatroom.time = cursor.getString(6);
                item_chatroom.Read = cursor.getInt(7);
                if (cursor.getInt(8) == 0) {// 0일때 참
                    item_chatroom.Readcheck = true;
                } else { // 1일때 거짓
                    item_chatroom.Readcheck = false;
                }
                msg.add(item_chatroom);
            }
            catch (Exception e){
                Log.i("AllResult() ERROR", ""+e);
            }
        }
        cursor.close();
        db.close();
        return msg;
    }

}
