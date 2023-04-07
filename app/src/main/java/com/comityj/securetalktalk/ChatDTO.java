package com.comityj.securetalktalk;

public class ChatDTO {

    private String msg, username, profileurl, useremail, aeskey, datetime;
    private Boolean encrypt;

    public ChatDTO(){}

    public ChatDTO(String username, String msg, boolean encrypt, String profileurl, String useremail, String aeskey, String datetime){
        this.username = username;
        this.msg = msg;
        this.profileurl = profileurl;
        this.useremail = useremail;
        this.encrypt = encrypt;
        this.aeskey = aeskey;
        this.datetime = datetime;
    }

    public String getMsg(){
        return msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getUsername(){
        return username;
    }

    public void getUsername(String nickname){
        this.username = username;
    }

    public String getProfileUrl(){
        return profileurl;
    }

    public void setProfileUrl(String profileurl){
        this.profileurl = profileurl;
    }

    public String getUserEmail(){
        return useremail;
    }

    public void setUserEmail(String useremail){
        this.useremail = useremail;
    }

    public Boolean getEncrypt(){ return encrypt; }

    public void setEncrypt(Boolean encrypt) { this.encrypt = encrypt; }

    public String getAesKey(){ return aeskey; }

    public void setAesKey(String aeskey) { this.aeskey = aeskey; }

    public String getDateTime(){ return datetime; }

    public void setDateTime(String datetime) { this.datetime = datetime; }

}
