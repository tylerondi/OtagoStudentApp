package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

public class CasebookInfo implements Comparable<CasebookInfo> {
    public int userCasebook_id;
    public String title;
    public String answer;
    public int questionID;
    public int answerID;
    public int groupOrder;
    public int user_casebook_id;
    public int serverQuestion_id;

    @Override
    public int compareTo(CasebookInfo another) {
        int compareOrder = ((CasebookInfo) another).groupOrder;

        return this.groupOrder - compareOrder;
    }

    public CasebookInfo copy() {
        CasebookInfo copy = new CasebookInfo();

        try {
            copy.user_casebook_id = user_casebook_id;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.title = title;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.answer = answer;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.questionID = questionID;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.answerID = answerID;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.groupOrder = groupOrder;
        } catch (NullPointerException npe) {
            // Do nothing
        }
        try {
            copy.user_casebook_id = user_casebook_id;
        } catch (NullPointerException npe) {
            // Do nothing
        }

        return copy;
    }
}
