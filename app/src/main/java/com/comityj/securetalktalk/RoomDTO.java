package com.comityj.securetalktalk;

import android.graphics.drawable.Drawable;

public class RoomDTO {
    private Drawable roomicon;
    private String roomname;
    private String roommsg;

    public RoomDTO(){}

    public RoomDTO(Drawable roomicon, String roomname, String roommsg){
        this.roomicon = roomicon;
        this.roomname = roomname;
        this.roommsg = roommsg;
    }

    public Drawable getRoomicon(){
        return this.roomicon;
    }

    public void setRoomicon(Drawable icon){
        roomicon = icon;
    }

    public String getRoomname(){
        return this.roomname;
    }

    public void setRoomname(String name){
        roomname = name;
    }

    public String getRoommsg(){
        return this.roommsg;
    }

    public void setRoommsg(String msg){
        roommsg = msg;
    }

}
