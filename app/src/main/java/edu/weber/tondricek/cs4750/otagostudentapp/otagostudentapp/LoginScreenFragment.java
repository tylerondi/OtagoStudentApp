package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonRectangle;

public class LoginScreenFragment extends Fragment {

    public static final String PREF_FILE_NAME = "loginValues";

    private EditText edtUserName;
    private EditText edtPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_screen, container, false);

        ButtonRectangle btnLogin = (ButtonRectangle) rootView.findViewById(R.id.btnLogin);
        edtUserName = (EditText) rootView.findViewById(R.id.edtUserName);
        edtPassword = (EditText) rootView.findViewById(R.id.edtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUserName.getText() != null && edtPassword.getText() != null) {
                    ((MainActivity) getActivity()).login(edtUserName.getText().toString(), edtPassword.getText().toString());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToPreferences(getActivity(), "userName", edtUserName.getText().toString());
        saveToPreferences(getActivity(), "password", edtPassword.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        edtUserName.setText(readFromPreferences(getActivity(), "userName", null));
        edtPassword.setText(readFromPreferences(getActivity(), "password", null));
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }
}

