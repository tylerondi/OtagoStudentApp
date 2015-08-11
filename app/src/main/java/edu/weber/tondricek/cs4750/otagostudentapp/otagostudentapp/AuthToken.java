package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

public class AuthToken{
    private String username;
    private String privilege_level;
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrivilege_level() {
        return privilege_level;
    }

    public void setPrivilege_level(String privilege_level) {
        this.privilege_level = privilege_level;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
