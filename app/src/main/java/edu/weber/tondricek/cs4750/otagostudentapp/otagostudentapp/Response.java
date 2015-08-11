package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

public class Response {
    private String result;
    private AuthToken data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public AuthToken getData() {
        return data;
    }

    public void setData(AuthToken data) {
        this.data = data;
    }
}
