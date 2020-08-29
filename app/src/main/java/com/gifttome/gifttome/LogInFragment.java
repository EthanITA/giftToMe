package com.gifttome.gifttome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;


public class LogInFragment extends Fragment {

    Button logInButton;
    MaterialTextView usernameAlreadyTaken;
    EditText logInUsername;

    public LogInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View thisFragment = inflater.inflate(R.layout.fragment_log_in, container, false);
        usernameAlreadyTaken = thisFragment.findViewById(R.id.username_already_taken_text);
        logInUsername = thisFragment.findViewById(R.id.login_username);

        logInButton = thisFragment.findViewById(R.id.login_button);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUsername();
            }
        });


        return thisFragment;    }

    public void saveUsername() {
        String thisUsername = logInUsername.getText().toString();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setUsername(thisUsername);

        editor.putString("username", logInUsername.getText().toString());
        editor.apply();
        Toast.makeText(mainActivity, "username saved", Toast.LENGTH_SHORT).show();
    }
}
