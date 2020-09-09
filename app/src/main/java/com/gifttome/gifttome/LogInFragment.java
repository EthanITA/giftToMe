package com.gifttome.gifttome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textview.MaterialTextView;


public class LogInFragment extends Fragment {

    Button logInButton;
    MaterialTextView usernameAlreadyTaken;
    EditText logInUsername;
    SavedStateHandle savedStateHandle;
    public LogInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        savedStateHandle = Navigation.findNavController(view)
                .getPreviousBackStackEntry()
                .getSavedStateHandle();
        savedStateHandle.set("good_username", false);
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

        if (!newUsername.equals("")) {
            savedStateHandle.set("good_username", true);
            NavHostFragment.findNavController(this).popBackStack();
        } else {
            usernameAlreadyTaken.setVisibility(View.VISIBLE);
        }

        Toast.makeText(getActivity(), "username saved", Toast.LENGTH_SHORT).show();
    }
}
