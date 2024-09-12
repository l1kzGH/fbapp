package com.likz.firebaseapp.entity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.gson.Gson;

import java.io.Serializable;

public class LastSong implements Serializable {

    String name;
    String artists;
    String dataSource;
    Bitmap imageBitmap;
    int pos;

    public LastSong(String name, String artists, String dataSource, Bitmap imageBitmap) {
        this(name, artists, dataSource, imageBitmap, 0);
    }

    public LastSong(String name, String artists, String dataSource, Bitmap imageBitmap, int pos) {
        this.name = name;
        this.artists = artists;
        this.dataSource = dataSource;
        this.imageBitmap = imageBitmap;
        this.pos = pos;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public String getArtists() {
        return artists;
    }

    public String getDataSource() {
        return dataSource;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public int getPos() {
        return pos;
    }

    public void saveSong(SharedPreferences mPrefs, Gson gson){
        SharedPreferences.Editor editor = mPrefs.edit();
        String json = gson.toJson(this);
        editor.putString("lastSong", json);
        editor.apply();
    }

    public static LastSong getSong(SharedPreferences mPrefs, Gson gson){
        String json = mPrefs.getString("lastSong", "");
        if(!json.equals("")){
            return gson.fromJson(json, LastSong.class);
        }
        return null;
    }

}
