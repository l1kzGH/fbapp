package com.likz.firebaseapp.entity;

import android.graphics.Bitmap;

public class Song {

    private int id;
    private String name;
    private String artists;
    private String duration;
    private String url;
    private Bitmap imageBitmap;
    private String playUrl;

    public Song() {
    }

    public Song(int id, String name, String artists, String duration, String url) {
        this(id, name, artists, duration, url, null);
    }

    public Song(int id, String name, String artists, String duration, String url, String playUrl) {
        this(id, name, artists, duration, url, playUrl, null);
    }

    public Song(int id, String name, String artists, String duration, String url, String playUrl, Bitmap imageBitmap) {
        this.id = id;
        this.name = name;
        this.artists = artists;
        this.duration = duration;
        this.url = url;
        this.imageBitmap = imageBitmap;
        this.playUrl = playUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtists() {
        return artists;
    }

    public String getDuration() {
        return duration;
    }

    public String getUrl() {
        return url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artists='" + artists + '\'' +
                ", duration='" + duration + '\'' +
                ", url='" + url + '\'' +
                ", imageBitmap=" + imageBitmap +
                ", playUrl='" + playUrl + '\'' +
                '}';
    }
}
