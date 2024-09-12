package com.likz.firebaseapp.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.likz.firebaseapp.R;
import com.likz.firebaseapp.menu.MenuActivity;

public class LoginFragment extends Fragment {

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private EditText loginTV, passwordTV;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        loginTV = view.findViewById(R.id.edit_login);
        passwordTV = view.findViewById(R.id.edit_password);
        Button confirm = view.findViewById(R.id.confirm_button);
        Button register = view.findViewById(R.id.register_button);

        confirm.setOnClickListener(v -> {
            loginUser();
        });

        register.setOnClickListener(v -> {
            ReplaceFragmentManager rfm = new ReplaceFragmentManager();
            rfm.replace(this, new RegisterFragment(), R.id.main_fragment);
        });

        passwordTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEND){
                    loginUser();
                    return true;
                }
                return false;
            }
        });
    }

    private void loginUser() {
        String login, password;

        login = loginTV.getText().toString();
        password = passwordTV.getText().toString();

        if (!validate(login, password)) return;

        DatabaseReference dbRef = database.getReference();
        dbRef.child("users").child(login).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getContext(), "Error with getting data", Toast.LENGTH_LONG).show();
                    System.err.println("Error getting data");
                } else {
                    if (task.getResult().getValue() != null) {
                        if (task.getResult().child("password").getValue().equals(password)) {

                            Toast.makeText(getContext(), "Logged!", Toast.LENGTH_SHORT).show();

                            Intent myIntent = new Intent(getActivity(), MenuActivity.class);
                            myIntent.putExtra("userData", task.getResult().getValue().toString());

                            StorageReference storageRef = storage.getReference().child("images/" + login);
                            storageRef.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    myIntent.putExtra("userPhoto", bytes);
                                    startActivity(myIntent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    startActivity(myIntent);
                                }
                            });

                        } else {
                            passwordTV.setError("Wrong password");
                        }
                    }
                }
            }
        });

    }

    private boolean validate(String login, String password) {
        if (TextUtils.isEmpty(login)) {
            loginTV.setError("Login can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordTV.setError("Password can't be empty");
            return false;
        }

        return true;
    }
}