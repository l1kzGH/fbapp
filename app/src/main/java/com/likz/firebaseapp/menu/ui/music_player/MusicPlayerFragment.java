package com.likz.firebaseapp.menu.ui.music_player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.likz.firebaseapp.R;
import com.likz.firebaseapp.entity.LastSong;
import com.likz.firebaseapp.entity.Song;
import com.likz.firebaseapp.menu.ui.ProfileFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MusicPlayerFragment extends Fragment {
    private final static int TRACKS_LENGTH = 10;
    public static String songsUrl;
    static private int catalog = 0; // 0 or 1
    private EditText searchET;
    private ImageButton playStop, library;
    private View loadingView;
    private SharedPreferences mPrefs;
    private Gson gson;
    private LinearLayout mplayerLayout;
    private List<LinearLayout> tracksLayoutArr;
    private SongsCatalog songsCatalog, librarySongsCatalog;
    private MediaPlayer mediaPlayer;
    private LastSong lastSong;

    public MusicPlayerFragment() {
        super(R.layout.fragment_music_player);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        gson = new Gson();
        tracksLayoutArr = new ArrayList<>();
        playStop = view.findViewById(R.id.mplayer_current_track_action);
        mplayerLayout = view.findViewById(R.id.mplayer_layout);
        searchET = view.findViewById(R.id.mplayer_search);
        library = view.findViewById(R.id.mplayer_library);
        loadingView = view.findViewById(R.id.mplayer_loading);
        dotsAnim(0);

        lastSong = LastSong.getSong(mPrefs, gson);
        mediaPlayer = new MediaPlayer();
        if(lastSong != null && !lastSong.getDataSource().equals("null")){
            recoverMediaPlayer();
        }

        DatabaseReference dbRef = ProfileFragment.database.getReference();
        dbRef.child("users_music_lib")
                .child(ProfileFragment.userLogin)
                .child("songs_ids").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getValue() != null) {
                        songsUrl = task.getResult().getValue().toString();
                    }
                });


        searchET.setOnEditorActionListener((v, actionId, event) -> {
            TextView error = view.findViewById(R.id.mplayer_error_search);
            error.setVisibility(View.GONE);

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadingView.setVisibility(View.VISIBLE);

                clearSongsLayouts();

                new Thread(() -> {
                    searchTracks();
                    getActivity().runOnUiThread(() -> {
                        for (int i = 0; i < tracksLayoutArr.size(); i++) {
                            mplayerLayout.addView(tracksLayoutArr.get(i));

                            int finalI = i;
                            tracksLayoutArr.get(i).setOnClickListener(v1 -> {
                                playTrack(finalI);
                            });
                        }
                    });
                }).start();
            }

            return true;
        });

        playStop.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                playStop.setImageResource(R.drawable.play_button);
                mediaPlayer.pause();
            } else {
                playStop.setImageResource(R.drawable.pause_button);
                mediaPlayer.start();
            }
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            // music ends
            playStop.setImageResource(R.drawable.play_button);
        });

        dotsAnim(0);

        // get more songs in bottom of got songs
        ScrollView scrollView = view.findViewById(R.id.scroll_view);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            boolean isBottomReached = false;

            @Override
            public void onScrollChanged() {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                if (diff == 0 && !isBottomReached) {
                    System.out.println("*searchmore");
                    isBottomReached = true;
                    if (catalog == 0) {
                        new Thread(() -> {
                            AccessToken accessToken = getAccessTokenObj();

                            int startCount = songsCatalog.getSongs().size();
                            songsCatalog.searchTracks(accessToken.getAccessToken(), searchET.getText().toString(), TRACKS_LENGTH);
                            tracksLayoutArr.addAll(songsCatalog.songsToUI(getContext(), tracksLayoutArr.size()));
                            int countDiff = songsCatalog.getSongs().size() - startCount;

                            System.out.println(tracksLayoutArr.size());
                            if (countDiff > 0)
                                getActivity().runOnUiThread(() -> {
                                    for (int i = tracksLayoutArr.size() - countDiff; i < tracksLayoutArr.size(); i++) {
                                        mplayerLayout.addView(tracksLayoutArr.get(i));

                                        int finalI = i;
                                        tracksLayoutArr.get(i).setOnClickListener(v1 -> {
                                            playTrack(finalI);
                                        });
                                    }
                                });
                            else {
                                loadingView.setVisibility(View.INVISIBLE);
                            }
                        }).start();
                    } else {
                        String[] songsUrlArr = songsUrl.split("/");
                        if (tracksLayoutArr.size() != songsUrlArr.length)
                            new Thread(() -> {

                                String accessToken = getAccessTokenObj().getAccessToken();

                                String songs10Url = "";
                                for (int i = tracksLayoutArr.size();
                                     i < tracksLayoutArr.size() + 10 && i < songsUrlArr.length; i++) {

                                    songs10Url += songsUrlArr[i];
                                    if (i + 1 != songsUrlArr.length) {
                                        songs10Url += ",";
                                    }
                                }

                                librarySongsCatalog.searchLibTracks(accessToken, songs10Url);
                                tracksLayoutArr.addAll(librarySongsCatalog.songsToUI(getContext(), tracksLayoutArr.size()));

                                System.out.println(tracksLayoutArr.size());
                                int len = songs10Url.split(",").length;
                                getActivity().runOnUiThread(() -> {
                                    for (int i = tracksLayoutArr.size() - len; i < tracksLayoutArr.size(); i++) {
                                        mplayerLayout.addView(tracksLayoutArr.get(i));

                                        int finalI = i;
                                        tracksLayoutArr.get(i).setOnClickListener(v1 -> {
                                            playTrack(finalI);
                                        });
                                    }
                                });
                            }).start();
                        else {
                            loadingView.setVisibility(View.INVISIBLE);
                        }
                    }

                } else if (diff != 0) {
                    isBottomReached = false;
                }

            }
        });

        library.setOnClickListener(v -> {
            if (catalog == 0) {
                catalog = 1;
                library.setBackgroundColor(Color.YELLOW);
                searchET.setText(null);
                searchET.setEnabled(false);
                clearSongsLayouts();
                loadingView.setVisibility(View.VISIBLE);
                new Thread(this::getLibrarySongs).start();

            } else {
                catalog = 0;
                library.setBackgroundColor(Color.WHITE);
                searchET.setEnabled(true);
                clearSongsLayouts();
                loadingView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private AccessToken getAccessTokenObj() {
        AccessToken token;

        // trying to get token from SharedPreferences
        if (mPrefs.getString("token", "").equals("")) {
            // get token from post request
            token = new AccessToken();
            token.saveToken(mPrefs, gson);
        } else {
            // get token from SharedPreferences
            String jsonGet = mPrefs.getString("token", "");
            token = gson.fromJson(jsonGet, AccessToken.class);
        }

        long passTimeMS = token.getCreateTime().getTime().getTime() + token.getExpiresIn() * 1000L;
        if (Calendar.getInstance().getTime().getTime() > passTimeMS) {
            // token expired
            token = new AccessToken();
            token.saveToken(mPrefs, gson);
        }

        return token;
    }

    private void searchTracks() {
        String accessToken = getAccessTokenObj().getAccessToken();

        songsCatalog = new SongsCatalog();
        songsCatalog.searchTracks(accessToken, searchET.getText().toString(), TRACKS_LENGTH);
        tracksLayoutArr = songsCatalog.songsToUI(getContext(), 0);

        System.out.println("end");
    }

    private void getLibrarySongs() {
        String accessToken = getAccessTokenObj().getAccessToken();
        librarySongsCatalog = new SongsCatalog();
        String[] songsUrlArr = songsUrl.split("/");

        String songs10Url = "";
        for (int i = 0; i < TRACKS_LENGTH; i++) {
            songs10Url += songsUrlArr[i];
            if (i + 1 != TRACKS_LENGTH) {
                songs10Url += ",";
            }
        }

        librarySongsCatalog.searchLibTracks(accessToken, songs10Url);

        tracksLayoutArr = librarySongsCatalog.songsToUI(getContext(), 0);
        System.out.println("end2");

        getActivity().runOnUiThread(() -> {
            for (int i = 0; i < tracksLayoutArr.size(); i++) {
                mplayerLayout.addView(tracksLayoutArr.get(i));

                int finalI = i;
                tracksLayoutArr.get(i).setOnClickListener(v1 -> {
                    playTrack(finalI);
                });
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void playTrack(int trackID) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            playStop.setImageResource(R.drawable.play_button);
        }

        Song currSong;
        if(catalog == 0){
            tracksLayoutArr.get(songsCatalog.getCurrSongID()).setBackground(null);
            tracksLayoutArr.get(trackID).setBackground(getContext().getDrawable(R.drawable.mp_current_track));
            songsCatalog.setCurrSongID(trackID);
            currSong = songsCatalog.getSongs().get(songsCatalog.getCurrSongID());
        } else {
            tracksLayoutArr.get(librarySongsCatalog.getCurrSongID()).setBackground(null);
            tracksLayoutArr.get(trackID).setBackground(getContext().getDrawable(R.drawable.mp_current_track));
            librarySongsCatalog.setCurrSongID(trackID);
            currSong = librarySongsCatalog.getSongs().get(librarySongsCatalog.getCurrSongID());
        }

        ImageView image = getView().findViewById(R.id.mplayer_current_track_image);
        TextView name = getView().findViewById(R.id.mplayer_current_track_name);
        TextView artists = getView().findViewById(R.id.mplayer_current_track_artists);

        // save
        lastSong = new LastSong(
                currSong.getName(),
                currSong.getArtists(),
                currSong.getPlayUrl(),
                null);
        if (currSong.getImageBitmap() != null) {
            image.setImageBitmap(currSong.getImageBitmap());
            lastSong.setImageBitmap(currSong.getImageBitmap());
        }

        name.setText(currSong.getName());
        artists.setText(currSong.getArtists());

        startMPlayer(currSong);
    }

    private void startMPlayer(Song currSong) {
        try {
            if (!currSong.getPlayUrl().equals("null")) {
                mediaPlayer.setDataSource(currSong.getPlayUrl());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(v -> {
                    mediaPlayer.start();
                    playStop.setImageResource(R.drawable.pause_button);
                });
                // save
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("dataSource", currSong.getPlayUrl());
                editor.apply();

            } else {
                System.err.println("cannt play - no <playurl>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void dotsAnim(int dotIndex) {
        View dotView = null;
        switch (dotIndex) {
            case 0:
                dotView = getView().findViewById(R.id.mplayer_loading_dot1);
                break;
            case 1:
                dotView = getView().findViewById(R.id.mplayer_loading_dot2);
                break;
            case 2:
                dotView = getView().findViewById(R.id.mplayer_loading_dot3);
                break;
        }

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_out);
        dotView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dotsAnim((dotIndex + 1) % 3);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void clearSongsLayouts() {
        while (mplayerLayout.getChildCount() > 2) {
            mplayerLayout.removeViewAt(2);
        }
    }

    private void recoverMediaPlayer(){
        try {
            mediaPlayer.setDataSource(lastSong.getDataSource());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(lastSong.getPos());

            ImageView image = getView().findViewById(R.id.mplayer_current_track_image);
            TextView name = getView().findViewById(R.id.mplayer_current_track_name);
            TextView artists = getView().findViewById(R.id.mplayer_current_track_artists);

            System.out.println(lastSong.getImageBitmap());
//            if(lastSong.getImageBitmap() != null)
//                image.setImageBitmap(lastSong.getImageBitmap());
            name.setText(lastSong.getName());
            artists.setText(lastSong.getArtists());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // save to SharedPreferences
        lastSong.setPos(mediaPlayer.getCurrentPosition());
        lastSong.saveSong(mPrefs, gson);

        mediaPlayer.release();
    }

}