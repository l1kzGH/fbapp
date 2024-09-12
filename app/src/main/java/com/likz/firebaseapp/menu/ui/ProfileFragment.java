package com.likz.firebaseapp.menu.ui;

import static android.app.Activity.RESULT_OK;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    public static String userLogin;
    private static final int SELECT_PICTURE = 1;
    private Bundle arguments;
    private TextView name, surname, login, dateOfBirth;
    private Button updateProfileButton;
    private boolean isUpdating = false;
    private ImageView photo;
    public static FirebaseDatabase database;
    public static FirebaseStorage storage;
    private String userStringData;
    private byte[] userPhotoBytesData;
    private Calendar dateAndTime;
    private boolean isPhotoUpdated = false;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dateAndTime = getInstance();
        arguments = Objects.requireNonNull(getActivity()).getIntent().getExtras();
        userStringData = arguments.getString("userData");
        userPhotoBytesData = arguments.getByteArray("userPhoto");

        name = view.findViewById(R.id.profile_name);
        surname = view.findViewById(R.id.profile_surname);
        login = view.findViewById(R.id.profile_login);
        dateOfBirth = view.findViewById(R.id.profile_dateOfBirth);
        photo = view.findViewById(R.id.profile_photo);

        init();

        updateProfileButton = view.findViewById(R.id.home_edit_button);
        updateProfileButton.setOnClickListener(v -> {
            updateProfile();
        });

        view.findViewById(R.id.home_change_password_button).setOnClickListener(v -> {
            switch (view.findViewById(R.id.home_update_password_layout).getVisibility()) {
                case View.INVISIBLE:
                    view.findViewById(R.id.home_update_password_layout).setVisibility(View.VISIBLE);
                    break;
                case View.VISIBLE:
                    view.findViewById(R.id.home_update_password_layout).setVisibility(View.INVISIBLE);
                    break;
            }
        });

        view.findViewById(R.id.home_submit_change_password_button).setOnClickListener(v -> {
            updatePassword();
        });
    }

    private void init() {
        try {
            JSONObject userData = new JSONObject(userStringData);

            name.setText(userData.getString("name"));
            surname.setText(userData.getString("surname"));
            userLogin = userData.getString("login");
            login.setText(userLogin);
            dateOfBirth.setText(userData.getString("dateOfBirth"));

            if (userPhotoBytesData != null) {
                getImage();
            } else {
                photo.setImageResource(R.drawable.def_img);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePassword() {
        EditText et = getView().findViewById(R.id.home_edit_password);
        String newPassword = et.getText().toString();

        if (TextUtils.isEmpty(newPassword)) {
            et.setError("Password can't be empty");
            return;
        }

        try {
            JSONObject userData = new JSONObject(userStringData);
            DatabaseReference dbRef = database.getReference();

            Map<String, Object> updates = new HashMap<>();
            updates.put("/users/" + userData.getString("login") + "/password", newPassword);
            dbRef.updateChildren(updates);

            // set update menu invisible after updating pass
            getView().findViewById(R.id.home_update_password_layout).setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            System.err.println("something going wrong (" + e + ")");
        }
    }

    private void getImage() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(userPhotoBytesData, 0, userPhotoBytesData.length);
        photo.setImageBitmap(bitmap);
    }

    private void updateProfile() {
        isUpdating = !isUpdating;
        Button uploadImg = getView().findViewById(R.id.profile_upload_image);
        Button updateDOB = getView().findViewById(R.id.profile_update_dateOfBirth);
        uploadImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICTURE);
        });
        updateDOB.setOnClickListener(v -> setDate(getView()));

        if (isUpdating) {
            // getting default background for edittext
            Drawable defaultBackground = getView().findViewById(R.id.home_edit_password).getBackground();

            name.setCursorVisible(true);
            name.setFocusable(true);
            name.setTextIsSelectable(true);
            name.setBackground(defaultBackground);

            surname.setCursorVisible(true);
            surname.setFocusable(true);
            surname.setTextIsSelectable(true);
            surname.setBackground(defaultBackground);

            updateProfileButton.setText("Update!");
            updateProfileButton.setBackgroundColor(Color.rgb(0, 150, 0));

            //
            uploadImg.setVisibility(View.VISIBLE);

            updateDOB.setVisibility(View.VISIBLE);

        } else {
            updateProfileButton.setText("Edit");
            updateProfileButton.setBackgroundColor(Color.rgb(0, 0, 0));

            name.setCursorVisible(false);
            name.setFocusable(false);
            name.setTextIsSelectable(false);
            name.setBackground(null);

            surname.setCursorVisible(false);
            surname.setFocusable(false);
            surname.setTextIsSelectable(false);
            surname.setBackground(null);

            uploadImg.setVisibility(View.GONE);
            updateDOB.setVisibility(View.INVISIBLE);

            updateRequest();

            if(isPhotoUpdated){
                updImg();
                isPhotoUpdated = false;
            }
        }

    }

    @SuppressLint("DefaultLocale")
    void setChosenDate() {
        dateOfBirth.setText(
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

    void updateRequest() {
        try {
            JSONObject userData = new JSONObject(userStringData);
            DatabaseReference dbRef = database.getReference();

            Map<String, Object> updates = new HashMap<>();
            updates.put("/users/" + userData.getString("login") + "/name", name.getText().toString());
            updates.put("/users/" + userData.getString("login") + "/surname", surname.getText().toString());
            updates.put("/users/" + userData.getString("login") + "/dateOfBirth", dateOfBirth.getText().toString());
            dbRef.updateChildren(updates);

            Toast.makeText(getContext(), "Profile data updated", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            System.err.println("something going wrong (" + e + ")");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
                photo.setImageBitmap(bitmap);
                isPhotoUpdated = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast toast = Toast.makeText(getContext(), "No image received", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    void updImg() {
        try {
            JSONObject userData = new JSONObject(userStringData);
            String login = userData.getString("login");
            StorageReference storageRef = storage.getReference();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getPhotoBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            UploadTask uploadTask = storageRef.child("images/" + login + "/").putBytes(bytes);
            uploadTask.addOnFailureListener(exception -> {
                        System.err.println("Error with upload profile image");
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    });
            arguments.putByteArray("userPhoto", bytes);
            getActivity().getIntent().replaceExtras(arguments);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Bitmap getPhotoBitmap(){
        photo.setDrawingCacheEnabled(true);
        photo.buildDrawingCache();
        return ((BitmapDrawable) photo.getDrawable()).getBitmap();
    }
}