package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

public class UpdateAnswer {
    private int answer_id;
    private String text;

    public UpdateAnswer(){

    }

    public UpdateAnswer(int answer_id, String text){
        this.answer_id = answer_id;
        this.text = text;
    }

    public int getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(int answer_id) {
        this.answer_id = answer_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
