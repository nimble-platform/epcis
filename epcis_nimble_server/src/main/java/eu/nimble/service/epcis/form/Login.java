package eu.nimble.service.epcis.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Login {

    @NotNull
    private String userName;

    @NotNull
    private String passWord;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
