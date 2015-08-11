package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.content.Context;
import android.widget.Toast;

public class Message {
    public static void message (Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
