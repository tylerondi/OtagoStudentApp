package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class OtagoDBConnector {

    private static final String DATABASE_NAME;
    private static final int DATABASE_VERSION;

    OtagoDBHelper helper;

    public OtagoDBConnector(Context context) {
        helper = new OtagoDBHelper(context);
    }

    /**
     * Inserts a URL into the URL table.
     *
     * @param url
     * @return long result of the insert query
     */
    public long insertURL(String url) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("Otago_URL", null, null);

        ContentValues contentValues = new ContentValues();
        contentValues.put("url", url);

        return db.insert("Otago_URL", null, contentValues);
    }

    /**
     * Gets the URL entered from the URL table.
     *
     * @return String URL
     */
    public String getURL() {
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] cols = {"url"};
        Cursor cursor = db.query("Otago_URL", cols, null, null, null, null, null);

        if (cursor.moveToNext()) {
            String url = cursor.getString(0);
            cursor.close();
            return url;
        }

        cursor.close();

        return null;
    }

    /**
     * Inserts a new user into the Person table.
     *
     * @param user
     * @return long result of the insert query
     */
    public long insertUser(CasebookObjects.User user) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("active", 0);
        db.update("Person", contentValues, null, null);

        contentValues = new ContentValues();
        contentValues.put("user_name", user.userName);
        contentValues.put("password", user.password);
        contentValues.put("token", user.token);
        contentValues.put("active", 1);

        return db.insert("Person", null, contentValues);
    }
    
    /**
     * Updates the user's password given their userName and updated password
     *
     * @param userName
     * @param password
     * @return long result of the insert query
     */
    public long updateUserPassword(String userName, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("password", password);

        return db.update("Person", contentValues, "user_name=?", new String[]{userName});
    }

    /**
     * Gets the currently logged in user from the Person table.
     *
     * @return CasebookObjects.User object of the user
     */
    public CasebookObjects.User getUser() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"_id", "user_name", "password", "token"};
        Cursor cursor = db.query("Person", cols, "active = 1", null, null, null, null);

        if (cursor.moveToNext()) {
            int uid = cursor.getInt(0);
            String name = cursor.getString(1);
            String password = cursor.getString(2);
            String token = cursor.getString(3);
            cursor.close();

            return new CasebookObjects.User(uid, name, password, token);
        }

        cursor.close();

        return null;
    }

    /**
     * Checks to see if a given userName is in the Person table
     *
     * @param userName
     * @return CasebookObjects.User object of the user or NULL if not found
     */
    public CasebookObjects.User userExists(String userName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"_id", "user_name", "password", "token"};
        Cursor cursor = db.query("Person", cols, "user_name=?", new String[]{userName}, null, null, null);

        if (cursor.moveToNext()) {
            int uid = cursor.getInt(0);
            String name = cursor.getString(1);
            String password = cursor.getString(2);
            String token = cursor.getString(3);
            cursor.close();

            return new CasebookObjects.User(uid, name, password, token);
        }

        cursor.close();

        return null;
    }

    /**
     * Switches the currently logged in user to a new user given their userName
     *
     * @param userName
     * @return
     */
    public int switchUser(String userName) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("active", 0);
        db.update("Person", contentValues, null, null);

        contentValues = new ContentValues();
        contentValues.put("active", 1);
        return db.update("Person", contentValues, "user_name=?", new String[]{userName});
    }

    /**
     * Logs-out the currently logged in user
     */
    public void logout() {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("active", 0);
        db.update("Person", contentValues, null, null);
    }

    /**
     * Inserts all casebooks for the currently logged in user into the database
     *
     * @param casebooks object from JSON Parser
     * @return
     */
    public boolean insertCasebooks(CasebookObjects.Casebooks casebooks) {
        SQLiteDatabase db = helper.getWritableDatabase();
        CasebookObjects.User user = getUser();

        // Insert all User Casebooks
        if (casebooks.data != null) {
            for (int i = 0; i < casebooks.data.length; i++) {
                // Insert Casebook table
                ContentValues contentValues = new ContentValues();
                contentValues.put("casebook_id", casebooks.data[i].casebook.casebook_id);
                contentValues.put("user_casebook_id", casebooks.data[i].userCasebook_id);
                contentValues.put("casebook_name", casebooks.data[i].casebook.name);
                if (db.insert("Casebook", null, contentValues) < 0) {
                    return false;
                }

                // Insert User_Casebooks table
                contentValues = new ContentValues();
                contentValues.put("user_casebook_id", casebooks.data[i].userCasebook_id);
                contentValues.put("casebook_type_id", casebooks.data[i].casebook.casebook_id);
                contentValues.put("user_id", user.uid);
                db.insert("User_Casebooks", null, contentValues);

                // Insert Question_Category table
                if (casebooks.data[i].casebook.categories != null) {
                    for (int j = 0; j < casebooks.data[i].casebook.categories.length; j++) {
                        insertCategory(casebooks.data[i].casebook.categories[j], casebooks.data[i].casebook.casebook_id, -1, casebooks.data[i].userCasebook_id, db);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Recursive call to insert a category and all sub-categories for the given category
     *
     * @param category object from the JSON parser
     * @param casebookID the ID of the casebook that this category belongs to
     * @param parentCategoryID the Parent Category ID of this category's parent
     * @param userCasebookID the User Casebook ID that this category belongs to
     * @param db the SQLiteDatabase object from getWritableDatabase()
     */
    public void insertCategory(CasebookObjects.Casebooks.Casebook.Category category,
                               long casebookID, long parentCategoryID, long userCasebookID, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("category_id", category.category_id);
        contentValues.put("casebook_type_id", casebookID);
        contentValues.put("category_name", category.name);
        if (parentCategoryID > 0) {
            contentValues.put("parent_category_id", (int) parentCategoryID);
        }
        contentValues.put("group_order", category.order);

        long categoryID = db.insert("Question_Category", null, contentValues);

        if (category.categories != null) {
            for (int i = 0; i < category.categories.length; i++) {
                insertCategory(category.categories[i], casebookID, categoryID, userCasebookID, db);
            }
        }

        // Insert Question_Text
        if (category.questions != null) {
            for (int i = 0; i < category.questions.length; i++) {
                contentValues = new ContentValues();
                contentValues.put("question_id", category.questions[i].question_id);
                contentValues.put("category_id", categoryID);
                contentValues.put("question_order", category.questions[i].order);
                contentValues.put("question_text", category.questions[i].text);
                db.insert("Question_Text", null, contentValues);
                // Insert Answer
                for (int j = 0; j < category.questions[i].answers.length; j++) {
                    contentValues = new ContentValues();
                    contentValues.put("answer_id", category.questions[i].answers[j].answer_id);
                    contentValues.put("user_casebook_id", userCasebookID);
                    contentValues.put("question_id", category.questions[i].question_id);
                    contentValues.put("server_question_id", category.questions[i].answers[j].question_id);
                    contentValues.put("answer_text", category.questions[i].answers[j].text);
                    contentValues.put("feedback", category.questions[i].answers[j].feedback);
                    db.insert("Answer", null, contentValues);
                }
            }
        }
    }

    /**
     * Gets all casebook's for the currently logged in user
     *
     * @return casebooks for the currently logged in user
     */
    public Cursor getCasebooks() {
        SQLiteDatabase db = helper.getWritableDatabase();

        return db.query("Casebook", new String[]{"casebook_id", "user_casebook_id", "casebook_name"}, null, null, null, null, null);
    }

    /**
     * Deletes all casebooks from the database by destroying the tables and re-creating them.
     */
    public void deleteCasebooks() {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Drop tables
        String dropQuery = "DROP TABLE IF EXISTS Answer;";
        db.execSQL(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS Question_Text;";
        db.execSQL(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS Question_Category;";
        db.execSQL(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS User_Casebooks;";
        db.execSQL(dropQuery);

        dropQuery = "DROP TABLE IF EXISTS Casebook;";
        db.execSQL(dropQuery);

        // Recreate tables
        String createQuery = "CREATE TABLE User_Casebooks" +
                "(user_casebook_id INTEGER primary key autoincrement," +
                "casebook_type_id INTEGER, user_id INTEGER," +
                "FOREIGN KEY(casebook_type_id) REFERENCES Casebook(casebook_id) ON DELETE CASCADE," +
                "FOREIGN KEY(user_id) REFERENCES Person(_id) ON DELETE CASCADE);";
        db.execSQL(createQuery);

        createQuery =  "CREATE TABLE Casebook" +
                "(casebook_id INTEGER primary key autoincrement," +
                "user_casebook_id, casebook_name TEXT);";
        db.execSQL(createQuery);

        createQuery =  "CREATE TABLE Question_Category" +
                "(category_id INTEGER primary key autoincrement," +
                "casebook_type_id INTEGER, category_name TEXT, parent_category_id INTEGER," +
                "group_order INTEGER," +
                "FOREIGN KEY(casebook_type_id) REFERENCES Casebook(casebook_id) ON DELETE CASCADE," +
                "FOREIGN KEY(parent_category_id) REFERENCES Question_Category(category_id) ON DELETE CASCADE);";
        db.execSQL(createQuery);

        createQuery =  "CREATE TABLE Question_Text" +
                "(question_id INTEGER primary key autoincrement," +
                "category_id INTEGER, question_order INTEGER, question_text TEXT," +
                "FOREIGN KEY(category_id) REFERENCES Question_Category(category_id) ON DELETE CASCADE);";
        db.execSQL(createQuery);

        createQuery =  "CREATE TABLE Answer" +
                "(answer_id INTEGER primary key autoincrement," +
                "user_casebook_id INTEGER, question_id INTEGER, server_question_id INTEGER, answer_text TEXT, feedback TEXT," +
                "FOREIGN KEY(user_casebook_id) REFERENCES User_Casebooks(user_casebook_id) ON DELETE CASCADE," +
                "FOREIGN KEY(question_id) REFERENCES Question_Text(question_id) ON DELETE CASCADE);";
        db.execSQL(createQuery);
    }

    /**
     * Gets a cursor to the Parent Categories of a given casebook
     *
     * @param casebookID that the Parent Categories belongs to
     * @return the main Parent categories for the specified casebook ID
     */
    public Cursor getParentCategories(int casebookID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"category_id", "category_name", "group_order"};
        return db.query("Question_Category", cols, "casebook_type_id = ? AND parent_category_id IS NULL", new String[]{"" + casebookID}, null, null, null);
    }

    /**
     * Gets all child categories for a given Casebook ID and Parent Category ID
     *
     * @param casebookID
     * @param parentID Parent Category ID of the desired sub-category
     * @return a Cursor to the children categories for a specified parent and casebook ID
     */
    public Cursor getChildCategories(int casebookID, int parentID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"category_id", "category_name", "group_order"};
        return db.query("Question_Category", cols, "casebook_type_id = ? AND parent_category_id = ?", new String[]{"" + casebookID, "" + parentID}, null, null, null);
    }

    /**
     *
     *
     * @param categoryID
     * @return a Cursor to questions for the specified category ID
     */
    public Cursor getQuestions(int categoryID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"question_id", "question_order", "question_text"};
        return db.query("Question_Text", cols, "category_id = ?", new String[]{"" + categoryID}, null, null, null);
    }

    /**
     *
     * @param questionID
     * @return a Cursor to answers for the specified question ID
     */
    public Cursor getAnswers(int questionID) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"answer_id", "answer_text"};
        return db.query("Answer", cols, "question_id = ?", new String[]{"" + questionID}, null, null, null);
    }

    /**
     *
     * @return a Cursor to all answers in the Answer table
     */
    public Cursor getAllAnswers() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] cols = {"answer_id", "answer_text", "question_id", "user_casebook_id"};
        return db.query("Answer", cols, null, null, null, null, null);
    }

    /**
     * Inserts a new answer into the Answer table
     *
     * @param userCasebookID of the answer
     * @param questionID of the question that the answer is answering
     * @param answerText a String for the text of the answer
     * @return
     */
    public long insertNewAnswer(int userCasebookID, int questionID, String answerText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("answer_id", -1);
        contentValues.put("user_casebook_id", userCasebookID);
        contentValues.put("question_id", questionID);
        contentValues.put("answer_text", answerText);
        contentValues.put("feedback", "");
        return db.insert("Answer", null, contentValues);
    }

    /**
     * Sets the answer text for a given answer in the Answer table
     *
     * @param answerID to be updated
     * @param answerText to change the answer's text to
     * @return
     */
    public int setAnswer(int answerID, String answerText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("answer_text", answerText);
        return db.update("Answer", contentValues, "answer_id=?", new String[]{"" + answerID});
    }

    static {
        DATABASE_NAME  = "otagodatabase";
        DATABASE_VERSION = 157;
    }

    /**
     * Static class that creates, destroys, and maintains the SQLite Database
     */
    static class OtagoDBHelper extends SQLiteOpenHelper
    {
        public OtagoDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createQuery = "CREATE TABLE Otago_URL" +
                    "(_id INTEGER primary key autoincrement," +
                    "url TEXT);";
            db.execSQL(createQuery);

            createQuery = "CREATE TABLE Person" +
                    "(_id INTEGER primary key autoincrement," +
                    "user_name TEXT, password TEXT, token TEXT, active NUMERIC);";
            db.execSQL(createQuery);

            createQuery = "CREATE TABLE User_Casebooks" +
                    "(user_casebook_id INTEGER primary key autoincrement," +
                    "casebook_type_id INTEGER, user_id INTEGER," +
                    "FOREIGN KEY(casebook_type_id) REFERENCES Casebook(casebook_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(user_id) REFERENCES Person(_id) ON DELETE CASCADE);";
            db.execSQL(createQuery);

            createQuery =  "CREATE TABLE Casebook" +
                    "(casebook_id INTEGER primary key autoincrement," +
                    "user_casebook_id, casebook_name TEXT);";
            db.execSQL(createQuery);

            createQuery =  "CREATE TABLE Question_Category" +
                    "(category_id INTEGER primary key autoincrement," +
                    "casebook_type_id INTEGER, category_name TEXT, parent_category_id INTEGER," +
                    "group_order INTEGER," +
                    "FOREIGN KEY(casebook_type_id) REFERENCES Casebook(casebook_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(parent_category_id) REFERENCES Question_Category(category_id) ON DELETE CASCADE);";
            db.execSQL(createQuery);

            createQuery =  "CREATE TABLE Question_Text" +
                    "(question_id INTEGER primary key autoincrement," +
                    "category_id INTEGER, question_order INTEGER, question_text TEXT," +
                    "FOREIGN KEY(category_id) REFERENCES Question_Category(category_id) ON DELETE CASCADE);";
            db.execSQL(createQuery);

            createQuery =  "CREATE TABLE Answer" +
                    "(answer_id INTEGER primary key autoincrement," +
                    "user_casebook_id INTEGER, question_id INTEGER, server_question_id INTEGER, answer_text TEXT, feedback TEXT," +
                    "FOREIGN KEY(user_casebook_id) REFERENCES User_Casebooks(user_casebook_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(question_id) REFERENCES Question_Text(question_id) ON DELETE CASCADE);";
            db.execSQL(createQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropQuery = "DROP TABLE IF EXISTS Otago_URL";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS Answer;";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS Question_Text;";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS Question_Category;";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS User_Casebooks;";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS Casebook;";
            db.execSQL(dropQuery);

            dropQuery = "DROP TABLE IF EXISTS Person;";
            db.execSQL(dropQuery);

            onCreate(db);
        }
    }
}
