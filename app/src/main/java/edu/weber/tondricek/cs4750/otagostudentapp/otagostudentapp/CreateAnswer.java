package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

public class CreateAnswer {
    private int user_casebook_id;
    private int question_id;
    private String text;

    public CreateAnswer(){

    }

    public CreateAnswer(int user_casebook_id, int question_id, String text){
        this.user_casebook_id = user_casebook_id;
        this.question_id = question_id;
        this.text = text;
    }

    public int getUser_casebook_id() {
        return user_casebook_id;
    }

    public void setUser_casebook_id(int user_casebook_id) {
        this.user_casebook_id = user_casebook_id;
    }

    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
