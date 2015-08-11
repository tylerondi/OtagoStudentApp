package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.gc.materialdesign.widgets.Dialog;

import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {

    private OtagoDBConnector dbc;
    private OtagoAPI api;

    private NavigationDrawerFragment drawerFragment;
    private CategoryQuestionHolderFragment categoryQuestionHolderFragment;
    private boolean categoryQuestionRan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbc = new OtagoDBConnector(this);
        api = new OtagoAPI(this);
        categoryQuestionHolderFragment = new CategoryQuestionHolderFragment();
        categoryQuestionRan = false;

        setContentView(R.layout.activity_main_appbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        if (dbc.getURL() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new EnterURLFragment(), "enterURLScreen")
                    .commit();
        } else if (savedInstanceState == null && dbc.getUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        } else if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Navigation ------------------------------------------

    /**
     * Switches the currently used URL for where the web app is hosted
     *
     * @param view
     */
    public void switchUrl(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EnterURLFragment(), "enterURLScreen")
                .commit();
    }

    /**
     * If logged in this will switch to the Casebooks Screen
     *
     * @param view
     */
    public void viewCasebooks(View view) {
        if (dbc.getUser() == null) {
            Message.message(this, "You must login to view casebooks.");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                    .commit();
        }
    }

    /**
     * This will import casebooks into the database
     *
     * @param view
     */
    public void importCasebooks(View view) {
        if (dbc.getUser() != null && dbc.getCasebooks() == null) {
            api.getCasebooks();
        } else if (dbc.getUser() != null) {
            showWarningImport();
        } else {
            Message.message(this, "You must login to import casebooks.");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        }
    }

    /**
     * This will upload changes the user made to the server with an API call
     *
     * @param view
     */
    public void uploadChanges(View view) {
        if (dbc.getUser() != null) {
            Message.message(this, "Upload Started (This may take a while...)");
            api.uploadCasebooks();
        } else {
            Message.message(this, "You must login to upload casebooks.");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        }
    }

    /**
     * Logs out the current user and returns to the Login Screen
     *
     * @param view
     */
    public void logout(View view) {
        if (dbc.getUser() != null) {
            showWarningLogout();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        }
    }

    /**
     * Shows a warning dialog and accept button
     */
    public void showWarningImport() {
        Dialog dialog = new Dialog(this, "Overwrite Data?",
                "**WARNING**\n\nIf you allow this import ALL data for ALL users will be replaced " +
                        "with data from the server! This will cause you to lose any work that has not " +
                        "been uploaded. Do you wish to continue? (Tap outside the warning box to decline)");
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View v) {
                                                      dbc.deleteCasebooks();
                                                      api.getCasebooks();
                                                  }
                                              }

        );
        dialog.show();
    }

    /**
     * Shows a warning dialog and accept button
     */
    public void showWarningLogout() {
        Dialog dialog = new Dialog(this, "Overwrite Data?",
                "**WARNING**\n\nIf switch users ALL data for ALL users will be replaced " +
                        "with data from the server! This will cause you to lose any work that has not " +
                        "been uploaded. Do you wish to continue? (Tap outside the warning box to decline)");
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View v) {
                                                      dbc.deleteCasebooks();
                                                      dbc.logout();

                                                      drawerFragment.updateUserName();

                                                      SharedPreferences sharedPreferences = getSharedPreferences("CasebookFragment", Context.MODE_PRIVATE);
                                                      SharedPreferences.Editor editor = sharedPreferences.edit();
                                                      editor.clear();
                                                      editor.apply();

                                                      getSupportFragmentManager().beginTransaction()
                                                              .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                                                              .commit();
                                                  }
                                              }


        );
        dialog.show();
    }
    // End Navigation --------------------------------------

    /**
     * Called from the EnterURLFragment
     *
     * @param enteredURL String URL from the user
     */
    public void enterURL(String enteredURL) {
        dbc.insertURL(enteredURL);
        if (dbc.getUser() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                    .commit();
        }
    }

    /**
     * Called from the LoginScreenFragment
     * If the user is not logged in and uses a new password it checks the server to see if it's valid
     * If the user doesn't exist then it checks the server if it's valid and if it is it enters it into the db
     *
     * @param userName String of the new User Name to be logged in
     * @param password String of the password for the user to be logged in
     */
    public void login(String userName, String password) {
        CheckValidLoginTask validLoginTask = new CheckValidLoginTask();

        if (dbc.userExists(userName) != null &&
            dbc.userExists(userName).password.equals(password)) {
            dbc.switchUser(userName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                    .commit();
        } else if (dbc.getURL() == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EnterURLFragment(), "enterURLScreen")
                    .commit();
            Message.message(this, "You must enter a URL before logging in.");
        } else if (dbc.userExists(userName) != null) {
            try {
                if (validLoginTask.execute(dbc.getURL() + "/api/authentication/login/", userName, password).get() != null) {
                    dbc.updateUserPassword(userName, password);
                    login(userName, password);
                } else {
                    Message.message(this, "Incorrect Username or Password");
                }
            } catch (InterruptedException e) {
                Message.message(this, "Connection to internet interrupted, please try again.");
            } catch (ExecutionException e) {
                Message.message(this, "Connection to internet interrupted, please try again.");
            }
        } else {
            try {
                if (validLoginTask.execute(dbc.getURL() + "/api/authentication/login/", userName, password).get() != null) {
                    dbc.insertUser(new CasebookObjects.User(0, userName, password, "AAA"));
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                            .commit();
                } else {
                    Message.message(this, "Incorrect Username or Password");
                }
            } catch (InterruptedException e) {
                Message.message(this, "Connection to internet interrupted, please try again.");
            } catch (ExecutionException e) {
                Message.message(this, "Connection to internet interrupted, please try again.");
            }
        }

        drawerFragment.updateUserName();
    }
    class CheckValidLoginTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return api.doAuthentication(params[0], params[1], params[2]);
        }
    }

    /**
     * Called when OtagoAPI finishes downloading the casebooks
     * If the user changed their password since login then it takes them to the login screen
     *
     * @param casebooks object from the JSON parser
     */
    public void downloadCasebooksComplete(CasebookObjects.Casebooks casebooks) {
        if (casebooks != null) {
            Message.message(this, "Casebooks Download Complete");
            dbc.insertCasebooks(casebooks);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
                    .commit();
        } else {
            Message.message(this, "Casebooks Download Failed - Please check the URL");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EnterURLFragment(), "enterURLScreen")
                    .commit();
        }
    }
    public void downloadCasebooksInvalidLogin() {
        Message.message(this, "Casebooks Download/Upload Failed - Invalid Username or Password");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginScreenFragment(), "loginScreen")
                .commit();
    }

    /**
     * Called when OtagoAPI finishes uploading the casebooks
     *
     * @param message Result message from the API regarding the upload
     */
    public void uploadCasebooksComplete(String message) {
        Message.message(this, message);
    }

    /**
     * Called from the CasebookFragment
     *
     * @param casebook_id of the last clicked casebook on the Casebook Screen
     */
    public void openParentCategories(int casebook_id) {
        saveToPreferences(this, "selectedCasebookID", casebook_id + "");
        saveToPreferences(this, "isParentCategory", "true");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CategoryFragment(), "categoryQuestionScreen")
                .commit();
    }

    /**
     * Called from the CategoryFragment
     *
     * @param parent_id of the last clicked Category on the Parent Category Screen
     */
    public void openChildCategories(int parent_id) {
        if (!categoryQuestionRan) {
            categoryQuestionHolderFragment.setUp();
            categoryQuestionRan = true;
        }

        if (readFromPreferences(this, "isParentCategory", "true").equals("true")) {
            saveToPreferences(this, "selectedCategoryID", parent_id + "");
            saveToPreferences(this, "isParentCategory", "false");
            categoryQuestionHolderFragment = new CategoryQuestionHolderFragment();
            categoryQuestionHolderFragment.setUp();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, categoryQuestionHolderFragment, "categoryQuestionScreen")
                    .commit();
        } else {
            saveToPreferences(this, "selectedCategoryID", parent_id + "");
            saveToPreferences(this, "isParentCategory", "false");
            categoryQuestionHolderFragment.switchData();
        }
    }

    /**
     * Method to control what happens when the back button is pressed TODO: (not yet implemented)
     */
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        String isParentCategory = readFromPreferences(this, "isParentCategory", "notSet");
//        if (isParentCategory.equals("true")) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new CasebookFragment(), "casebooksScreen")
//                    .commit();
//        } else if (isParentCategory.equals("false")) {
//
//        }
//    }

    /**
     * Saves to Shared Preferences under "CasebookFragment"
     *
     * @param context the Main Activity
     * @param preferenceName that you want to call the shared preference
     * @param preferenceValue value of the shared preference
     */
    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CasebookFragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    /**
     * Reads from the Shared Preferences under "CasebookFragment"
     *
     * @param context of the Main Activity
     * @param preferenceName name the shared preference that you are getting
     * @param defaultValue value of the String if not found in Shared Preferences
     * @return String of the requested Shared Preference
     */
    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CasebookFragment", Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }
}
