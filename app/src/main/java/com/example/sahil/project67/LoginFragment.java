package com.example.sahil.project67;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LoginFragment extends Fragment {

    private Button loginButton;
    private Button createNewAccountButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_login, null, false);

        loginButton = (Button) view.findViewById(R.id.loginButton);
        createNewAccountButton = (Button) view.findViewById(R.id.newAccountButton);


        return view;
    }
}
