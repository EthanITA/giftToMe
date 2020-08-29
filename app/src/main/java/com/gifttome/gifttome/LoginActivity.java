package com.gifttome.gifttome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;

public class LoginActivity extends AppCompatActivity {

    Button logInButton;
    MaterialTextView usernameAlreadyTaken;
    EditText logInUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_log_in);
        usernameAlreadyTaken =findViewById(R.id.username_already_taken_text);
        logInUsername = findViewById(R.id.login_username);

        logInButton = findViewById(R.id.login_button);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUsername();
            }
        });
    }

    public void saveUsername() {
        String thisUsername = logInUsername.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //MainActivity mainActivity = (MainActivity) getActivity();
        //mainActivity.setUsername(thisUsername);

        editor.putString("username", logInUsername.getText().toString());
        editor.apply();
        Toast.makeText(this, "username saved", Toast.LENGTH_SHORT).show();
        Intent toMain = new Intent(this, MainActivity.class);
        startActivity(toMain);
    }


}