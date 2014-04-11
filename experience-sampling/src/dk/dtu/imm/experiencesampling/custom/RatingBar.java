package dk.dtu.imm.experiencesampling.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import dk.dtu.imm.experiencesampling.R;


public class RatingBar extends LinearLayout {

    boolean seekBarClicked;
    SeekBar friendRating;
    int rating;

    public RatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        // First inflate the layout for this view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.rating_selector, this);

        // Get custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBar, 0, 0);
        String title = (a != null) ? a.getString(R.styleable.RatingBar_ratingTitle) : "";
        String lowTitle = (a != null) ? a.getString(R.styleable.RatingBar_ratingLowTitle) : "";
        String highTitle = (a != null) ? a.getString(R.styleable.RatingBar_ratingHighTitle) : "";

        // Load friendRating
        friendRating = (SeekBar) findViewById(R.id.rating_seek_bar);

        setTitle(title);
        setLowTitle(lowTitle);
        setHighTitle(highTitle);

        // Hide seekBar thumb
        hideSeekBarThumpOnInit();
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.rating_title)).setText(title);
    }

    public void setLowTitle(String title) {
        ((TextView) findViewById(R.id.rating_low_title)).setText(title);
    }

    public void setHighTitle(String title) {
        ((TextView) findViewById(R.id.rating_high_title)).setText(title);
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        friendRating.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public void setSeekBarClicked(boolean seekBarClicked) {
        this.seekBarClicked = seekBarClicked;
    }

    public boolean isSeekBarClicked() {
        return seekBarClicked;
    }

    private void hideSeekBarThumpOnInit() {
        SeekBar friendRating = (SeekBar) findViewById(R.id.rating_seek_bar);
        Drawable ratingThumb = friendRating.getThumb();
        if (ratingThumb != null) {
            ratingThumb.setAlpha(0);
            friendRating.setThumb(ratingThumb);
        }
    }

}
