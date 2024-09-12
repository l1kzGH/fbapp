package com.likz.firebaseapp.authentication;

import static android.app.Activity.RESULT_OK;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.likz.firebaseapp.R;
import com.likz.firebaseapp.entity.User;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class RegisterFragment extends Fragment {

    private static final int SELECT_PICTURE = 1;
    private static final int CAMERA_REQUEST = 1888;

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Calendar dateAndTime;
    private TextView nameTV, surnameTV, loginTV, passTV, repassTV, chosenDate;
    private ImageView photoPreview;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dateAndTime = getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        chosenDate = view.findViewById(R.id.register_chosen_date);
        photoPreview = view.findViewById(R.id.register_photo_preview);
        nameTV = view.findViewById(R.id.register_edit_name);
        surnameTV = view.findViewById(R.id.register_edit_surname);
        loginTV = view.findViewById(R.id.register_edit_login);
        passTV = view.findViewById(R.id.register_edit_password);
        repassTV = view.findViewById(R.id.register_edit_repassword);

        view.findViewById(R.id.register_date_button).setOnClickListener(v -> setDate(view));

        view.findViewById(R.id.register_upload_avatar).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICTURE);
        });

        view.findViewById(R.id.register_photo).setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });

        view.findViewById(R.id.register_button).setOnClickListener(v -> {
            registerUser();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    photoPreview.setImageURI(uri);
                    photoPreview.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(getContext(), "No image received", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    photoPreview.setImageBitmap(photo);
                    photoPreview.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(getContext(), "No image received", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            default:
                System.out.println("?");
        }

    }

    @SuppressLint("DefaultLocale")
    void setChosenDate() {
        chosenDate.setText(
                String.format("%d.%d.%d",
                        dateAndTime.get(DAY_OF_MONTH),
                        dateAndTime.get(MONTH) + 1,
                        dateAndTime.get(YEAR))
        );
    }

    void setDate(View v) {
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateAndTime.set(YEAR, year);
                dateAndTime.set(MONTH, monthOfYear);
                dateAndTime.set(DAY_OF_MONTH, dayOfMonth);
                setChosenDate();
            }
        },
                dateAndTime.get(YEAR),
                dateAndTime.get(MONTH),
                dateAndTime.get(DAY_OF_MONTH))
                .show();
    }

    private void registerUser() {
        String name, surname, login, password, rePassword, dateOfBirth;

        name = nameTV.getText().toString();
        surname = surnameTV.getText().toString();
        login = loginTV.getText().toString();
        password = passTV.getText().toString();
        rePassword = repassTV.getText().toString();
        dateOfBirth = chosenDate.getText().toString();

        // validate login and password/repassword
        if (validate(name, surname, login, password, rePassword, dateOfBirth)) {
            User user = new User(name, surname, login, password, dateOfBirth);

            DatabaseReference dbRef = database.getReference();
            dbRef.child("users").child(login).setValue(user);

            if (photoPreview.getDrawable() != null) {
                uploadImage();
            }

            Toast.makeText(getContext(), "SUCCESS!", Toast.LENGTH_LONG).show();
            ReplaceFragmentManager rfm = new ReplaceFragmentManager();
            rfm.replace(this, new LoginFragment(), R.id.main_fragment);
        }

    }

    private boolean validate(String name, String surname, String login,
                             String password, String rePassword, String dateOfBirth) {

        if (!password.equals(rePassword)) {
            repassTV.setError("Password and repeated password must be same");
            //Toast.makeText(getContext(), "Password and repeated password must be same", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(name)) {
            nameTV.setError("Name can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(surname)) {
            surnameTV.setError("Surname can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(login)) {
            loginTV.setError("Login can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passTV.setError("Password can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(rePassword)) {
            repassTV.setError("Repeated password can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(dateOfBirth)) {
            chosenDate.setError("Date of birth can't be empty");
            return false;
        }

        return true;
    }

    private void uploadImage() {
        String login = loginTV.getText().toString();
        StorageReference storageRef = storage.getReference();

        photoPreview.setDrawingCacheEnabled(true);
        photoPreview.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) photoPreview.getDrawable()).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        UploadTask uploadTask = storageRef.child("images/" + login + "/").putBytes(bytes);
        uploadTask.addOnFailureListener(exception -> {
                    System.err.println("Error with upload profile image");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // ...
                });

    }

}