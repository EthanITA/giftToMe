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
        View thisFragment = inflater.inflate(R.layout.fragment_log_in, container, false);

        usernameAlreadyTaken = thisFragment.findViewById(R.id.username_already_taken_text);
        logInUsername = thisFragment.findViewById(R.id.login_username);

        logInButton = thisFragment.findViewById(R.id.login_button);
        logInButton.setOnClickListener(v -> saveUsername());

        return thisFragment;    }

    public void saveUsername() {
        String newUsername = logInUsername.getText().toString();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("username", newUsername);
        editor.apply();
        Toast.makeText(getActivity(), "username saved", Toast.LENGTH_SHORT).show();
    }
}
