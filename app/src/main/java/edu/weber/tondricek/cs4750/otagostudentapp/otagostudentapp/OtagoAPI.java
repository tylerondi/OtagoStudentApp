package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OtagoAPI {

    private OtagoDBConnector dbc;
    private CasebookObjects.User user;
    private MainActivity mActivity;

    public OtagoAPI(Context context) {
        dbc = new OtagoDBConnector(context);
        user = dbc.getUser();
        mActivity = (MainActivity) context;
    }

    /**
     * Fires an Async class that returns all casebooks for the logged in user
     * The Async class calls the MainActivity when finished
     */
    protected void getCasebooks() {
        user = dbc.getUser();
        DownloadCasebooksTask downloadCB = new DownloadCasebooksTask();
        downloadCB.execute(dbc.getURL() + "/api/authentication/login/", dbc.getURL() + "/api/casebook/", user.userName, user.password);
    }

    /**
     * Fires an Async class that uploads all casebooks for the logged in user to the server
     * The Async class calls the MainActivity when finished
     */
    protected void uploadCasebooks() {
        user = dbc.getUser();
        UploadCasebooksTask uploadCB = new UploadCasebooksTask();
        uploadCB.execute(dbc.getURL() + "/api/authentication/login/", dbc.getURL() + "api/casebook/question/", user.userName, user.password);
    }

    class DownloadCasebooksTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
                String authenticationUrl = params[0];
                String urlStr = params[1];
                String username = params[2];
                String password = params[3];

                String token = doAuthentication(authenticationUrl, username, password);

                if (token == null) {
                    return "Username or password invalid";
                }

                String rawJson = "";

                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Application-Authorization", token);
                    conn.connect();
                    int status = conn.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            rawJson = br.readLine();
                            break;
                        default:
                            Log.d("errors", "not connected: " + status);
                    }
                } catch (IOException ioe) {
                    Log.d("errors", ioe.getMessage());
                }

                return rawJson;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("Username or password invalid")) {
                mActivity.downloadCasebooksInvalidLogin();
            } else {
                CasebookObjects.Casebooks casebooks = jsonParse(s);
                mActivity.downloadCasebooksComplete(casebooks);
                //mActivity.downloadCasebooksComplete(jsonParse(HardCodedJson.rawJson));
            }
        }

        // use the Gson lib to parse out the Json
        private CasebookObjects.Casebooks jsonParse(String rawJson)
        {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();

            CasebookObjects.Casebooks casebooks = null;

            try
            {
                casebooks = gson.fromJson(rawJson, CasebookObjects.Casebooks.class);
                Log.d("test", "Number of casebooks returned is: " + casebooks.data.length);
            }
            catch (Exception e)
            {
                Log.d("errors", e.getMessage());
            }

            return casebooks;
        }
    }

    class UploadCasebooksTask extends AsyncTask<String, Integer, String> {

        BufferedReader in = null;
        OutputStream out = null;

        @Override
        protected String doInBackground(String... params) {
            String authenticationUrl = params[0];
            String urlStr = params[1];
            String username = params[2];
            String password = params[3];

            String token = doAuthentication(authenticationUrl, username, password);

            if (token == null) {
                return "Username or password invalid";
            }

            try {
                Cursor cursor = dbc.getAllAnswers();
                while (cursor.moveToNext()) {
                    Gson gson = new Gson();

                    URL url = new URL(urlStr);

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    if (cursor.getInt(1) == -1) {
                        con.setRequestMethod("POST");
                    } else {
                        con.setRequestMethod("PUT");
                    }
                    con.setRequestProperty("Application-Authorization", token);
                    con.setDoOutput(true);

                    out = new BufferedOutputStream(con.getOutputStream());

                    String output = gson.toJson(new UpdateAnswer(cursor.getInt(0), cursor.getString(1)));
                    if (cursor.getInt(1) == -1) {
                        output = gson.toJson(new CreateAnswer(cursor.getInt(3), cursor.getInt(2), cursor.getString(1)));
                    }

                    out.write(output.getBytes());
                    out.flush();

                    in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String input = "";
                    String buffer;
                    while( (buffer = in.readLine()) != null){
                        input += buffer;
                    }
                }

               return "Upload Successful";
            } catch(Exception e) {
                return e.toString();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    }catch(Exception e) {
                        Log.d("errors", e.getMessage());
                    }
                }
                if(in != null) {
                    try {
                        in.close();
                    } catch(Exception e) {
                        Log.d("errors", e.getMessage());
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("Username or password invalid")) {
                mActivity.downloadCasebooksInvalidLogin();
            } else {
                mActivity.uploadCasebooksComplete(s);
            }
        }
    }

    protected String doAuthentication(String authUrl,String username,String password){

        BufferedReader in = null;
        OutputStream out = null;

        try {
            Gson gson = new Gson();

            URL url = new URL(authUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            out = new BufferedOutputStream(con.getOutputStream());

            String output = gson.toJson(new Auth(username,password));

            out.write(output.getBytes());
            out.flush();

            in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String input = "";
            String buffer;
            while( (buffer = in.readLine()) != null){
                input += buffer;
            }

            Response resp = gson.fromJson(input,Response.class);

            if(!"OK".equals(resp.getResult())){
                return null;
            }

            return resp.getData().getToken();

        } catch(Exception e) {
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                }catch(Exception e) {
                    Log.d("errors", e.getMessage());
                }
            }
            if(in != null) {
                try {
                    in.close();
                } catch(Exception e) {
                    Log.d("errors", e.getMessage());
                }
            }
        }
    }
}
