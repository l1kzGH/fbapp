package com.likz.firebaseapp.menu.ui.music_player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.likz.firebaseapp.R;
import com.likz.firebaseapp.entity.Song;
import com.likz.firebaseapp.menu.ui.ProfileFragment;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SongsCatalog {
    private List<Song> songs;
    private int currSongID = 0;

    public SongsCatalog() {
        songs = new ArrayList<>();
    }

    public void searchTracks(String accessToken, String inputText, int tracksLength) {

        try {
            HttpResponse<JsonNode> tracksSearchRequest = Unirest.get("https://api.spotify.com/v1/search")
                    .field("q", inputText)
                    .field("type", "track")
                    .field("limit", tracksLength)
                    .field("offset", songs.size())
                    .header("Authorization", "Bearer " + accessToken)
                    .asJsonAsync().get();

            if (tracksSearchRequest.getCode() == 200) {
                JSONObject tracks = tracksSearchRequest.getBody().getObject();
                JSONArray tracksArr = tracks.getJSONObject("tracks").getJSONArray("items");
                for (int i = 0; i < tracksLength; i++) {
                    addSong(tracksArr.getJSONObject(i), i + songs.size());
                }
            }

        } catch (ExecutionException | InterruptedException | JSONException e) {
            // todo .. codes
//            getActivity().runOnUiThread(() -> {
//                TextView error = getView().findViewById(R.id.mplayer_error_search);
//                error.setVisibility(View.VISIBLE);
//            });
        }
    }

    public void searchLibTracks(String accessToken, String songsUrl) {
        try {
            HttpResponse<JsonNode> tracksSearchRequest = Unirest.get("https://api.spotify.com/v1/tracks")
                    .field("ids", songsUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .asJsonAsync().get();

            if (tracksSearchRequest.getCode() == 200) {
                JSONArray tracks = tracksSearchRequest.getBody().getObject().getJSONArray("tracks");
                for (int i = 0; i < tracks.length(); i++) {
                    addSong(tracks.getJSONObject(i), i + songs.size());
                }
            }

        } catch (ExecutionException | InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void addSong(JSONObject jsonTrackObject, int id) {
        String name = null, artists = null, duration = null,
                imageUrl = null, url = null, playUrl = null;

        try {
            name = jsonTrackObject.getString("name");

            int countArtists = jsonTrackObject.getJSONArray("artists").length();
            artists = jsonTrackObject.getJSONArray("artists").getJSONObject(0).getString("name");
            if (countArtists > 1) {
                artists += " feat. ";
                for (int i = 1; i < countArtists; i++) {
                    artists += jsonTrackObject.getJSONArray("artists").getJSONObject(i).getString("name");
                    artists += " ";
                }
            }

            int durationFull = jsonTrackObject.getInt("duration_ms") / 1000;
            int durationMin = durationFull / 60;
            int durationSec = durationFull - (durationMin * 60);

            String durationMinStr = durationMin < 10 ? ("0" + durationFull / 60) : ("" + durationFull / 60);
            String durationSecStr = durationSec < 10 ? ("0" + durationSec) : ("" + durationSec);

            duration = durationMinStr + ":" + durationSecStr;

            // [640x640, 300x300, 64x64]
            imageUrl = jsonTrackObject.getJSONObject("album")
                    .getJSONArray("images")
                    .getJSONObject(1).getString("url");

            url = jsonTrackObject.getString("id");
            playUrl = jsonTrackObject.getString("preview_url");

        } catch (JSONException e) {
            //throw new RuntimeException(e);
            System.err.println("unknown error in MPFragment/238");
        }

        Song song = new Song(id, name, artists, duration, url, playUrl);
        songs.add(song);

        String finalImageUrl = imageUrl;
        Thread f = new Thread(() -> {
            try {
                InputStream in = new URL(finalImageUrl).openStream();
                Bitmap imageBitmap = BitmapFactory.decodeStream(in);
                song.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            f.start();
            f.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LinearLayout> songsToUI(Context context, int start) {
        List<LinearLayout> list = new ArrayList<>();

        for (int i = start; i < songs.size(); i++) {
            TrackView track = new TrackView(
                    context,
                    songs.get(i).getName(),
                    songs.get(i).getArtists(),
                    songs.get(i).getDuration(),
                    songs.get(i).getUrl());

            track.getImageView().setImageBitmap(songs.get(i).getImageBitmap());

            String url = songs.get(i).getUrl();
            track.getAddRemoveButton().setOnClickListener(v -> {
                if (MusicPlayerFragment.songsUrl.contains(url)) {
                    removeFromLibrary(url);
                    track.getAddRemoveButton().setImageResource(R.drawable.baseline_add_24);
                } else {
                    addToLibrary(url);
                    track.getAddRemoveButton().setImageResource(R.drawable.baseline_check_24);
                }
            });

            list.add(track.getMainLayout());
        }

        return list;
    }

    public void setCurrSongID(int currSongID) {
        this.currSongID = currSongID;
    }

    public int getCurrSongID() {
        return currSongID;
    }

    public List<Song> getSongs() {
        return songs;
    }

    private void addToLibrary(String url) {
        new Thread(() -> {
            DatabaseReference dbRef = ProfileFragment.database.getReference();

            dbRef.child("users_music_lib")
                    .child(ProfileFragment.userLogin)
                    .child("songs_ids").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().getValue() != null) {
                                String songs = task.getResult().getValue().toString();
                                dbRef.child("users_music_lib")
                                        .child(ProfileFragment.userLogin)
                                        .child("songs_ids").setValue(songs + "/" + url);
                                MusicPlayerFragment.songsUrl = songs + "/" + url;
                            } else {
                                dbRef.child("users_music_lib")
                                        .child(ProfileFragment.userLogin)
                                        .child("songs_ids").setValue(url);
                                MusicPlayerFragment.songsUrl = url;
                            }
                        }
                    });
        }).start();
    }

    private void removeFromLibrary(String url) {
        new Thread(() -> {
            DatabaseReference dbRef = ProfileFragment.database.getReference();

            dbRef.child("users_music_lib")
                    .child(ProfileFragment.userLogin)
                    .child("songs_ids").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().getValue() != null) {
                            if (MusicPlayerFragment.songsUrl.equals(url)) {
                                MusicPlayerFragment.songsUrl = null;
                            } else {
                                MusicPlayerFragment.songsUrl =
                                        MusicPlayerFragment.songsUrl.replaceAll(url + "/", "");
                                MusicPlayerFragment.songsUrl =
                                        MusicPlayerFragment.songsUrl.replaceAll("/" + url, "");
                            }

                            dbRef.child("users_music_lib")
                                    .child(ProfileFragment.userLogin)
                                    .child("songs_ids").setValue(MusicPlayerFragment.songsUrl);
                        }
                    });
        }).start();
    }

}
