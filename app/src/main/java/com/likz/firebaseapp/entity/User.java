package com.likz.firebaseapp.entity;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name;
    public String surname;
    public String login;
    public String password;
    public String dateOfBirth;

    public User() {
        // default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String surname, String login, String password, String dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }
}
