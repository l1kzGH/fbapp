package com.likz.firebaseapp.menu.ui.music_player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.likz.firebaseapp.R;

public class TrackView {

    final static int FONT_SIZE = 16, FONT_COLOR = Color.BLACK;
    private Context context;
    private LinearLayout mainLayout;
    private ImageView imageView;
    private ImageButton addRemoveButton;

    public TrackView(Context context, String name, String artists, String duration, String url) {

        this.context = context;

        mainLayout = createMainLayout();

        imageView = createImageLayout();
        mainLayout.addView(imageView);

        LinearLayout innerLayout = createTrackLayout();
        innerLayout.addView(createNameLayout(name));
        innerLayout.addView(createArtistsLayout(artists));
        innerLayout.addView(createDurationLayout(duration));
        mainLayout.addView(innerLayout);

        mainLayout.addView(createSpace());
        addRemoveButton = createAddRemoveButton(url);
        mainLayout.addView(addRemoveButton);
    }

    public ImageButton getAddRemoveButton() {
        return addRemoveButton;
    }

    public LinearLayout getMainLayout() {
        return mainLayout;
    }

    public ImageView getImageView() {
        return imageView;
    }

    private LinearLayout createMainLayout() {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                convertPixelsToDp(70, context)
        );
        params.setMargins(
                convertPixelsToDp(10, context),
                convertPixelsToDp(10, context),
                convertPixelsToDp(10, context),
                0);
        linearLayout.setPadding(
                convertPixelsToDp(5, context),
                convertPixelsToDp(5, context),
                convertPixelsToDp(5, context),
                convertPixelsToDp(5, context)
        );
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        return linearLayout;
    }

    private ImageView createImageLayout() {
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                convertPixelsToDp(60, context),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);

        return imageView;
    }

    private LinearLayout createTrackLayout() {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.setMargins(
                convertPixelsToDp(10, context),
                -3,
                0,
                0);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        return linearLayout;
    }

    private TextView createNameLayout(String name) {

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);

        textView.setText(name);
        textView.setTextColor(FONT_COLOR);
        textView.setTextSize(FONT_SIZE);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setSingleLine();
        textView.setMaxWidth(convertPixelsToDp(240, context));

        return textView;
    }

    private TextView createArtistsLayout(String artists) {

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);

        textView.setText(artists);

        textView.setTextColor(FONT_COLOR);
        textView.setTextSize(FONT_SIZE);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setSingleLine();
        textView.setMaxWidth(convertPixelsToDp(240, context));

        return textView;
    }

    private TextView createDurationLayout(String duration) {

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);

        textView.setText(duration);
        textView.setTextColor(FONT_COLOR);
        textView.setTextSize(FONT_SIZE);

        return textView;
    }

    private Space createSpace(){

        Space spaceView = new Space(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,0);
        params.weight = 1;
        spaceView.setLayoutParams(params);

        return spaceView;
    }

    private ImageButton createAddRemoveButton(String url){

        ImageButton imageButton = new ImageButton(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                convertPixelsToDp(50, context),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.END;
        imageButton.setLayoutParams(params);

        imageButton.setBackground(null);
        if(MusicPlayerFragment.songsUrl.contains(url)){
            imageButton.setImageResource(R.drawable.baseline_check_24);
        } else {
            imageButton.setImageResource(R.drawable.baseline_add_24);
        }

        return imageButton;

    }

    public static int convertPixelsToDp(int pixels, Context context) {
        return (int) (pixels * context.getResources().getDisplayMetrics().density);
    }

}
