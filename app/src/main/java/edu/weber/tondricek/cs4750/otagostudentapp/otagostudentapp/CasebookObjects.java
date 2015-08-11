package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

/**
 * Class filled in by the JSON Parser in the OtagoAPI class
 */
public class CasebookObjects {

    public static class User {
        public int uid;
        public String userName;
        public String password;
        public String token;

        public User(int uid, String userName, String password, String token) {
            this.uid = uid;
            this.userName = userName;
            this.password = password;
            this.token = token;
        }
    }

    public static class Casebooks {
        public Data[] data;

        public class Data {
            public int userCasebook_id;
            public Casebook casebook;
        }

        public static class Casebook {

            public int casebook_id;
            public String name;
            public Category[] categories;

            public class Category {

                public int category_id;
                public String name;
                public int order;
                public Category[] categories;
                public Question[] questions;

                public class Question {

                    public int question_id;
                    public int order;
                    public String text;
                    public Answer[] answers;

                    public class Answer {

                        public int answer_id;
                        public int question_id;
                        public String text;
                        public String feedback;
                    }
                }
            }
        }
    }
}
