package com.comityj.securetalktalk;

public class UserDTO {

    private String useruid, useremail;

    public UserDTO(){}

    public UserDTO(String useruid, String useremail){
        this.useruid = useruid;
        this.useremail = useremail;
    }

    public String getUserUid(){ return useruid; }

    public void setUserUid(String useruid){ this.useruid = useruid; }

    public String getUserEmail(){ return useremail; }

    public void setUserEmail(String useruid){ this.useremail = useremail; }
}
