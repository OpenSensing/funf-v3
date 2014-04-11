package dk.dtu.imm.experiencesampling.fragments.questions;

import android.graphics.drawable.Drawable;
import android.widget.SeekBar;
import dk.dtu.imm.experiencesampling.custom.RatingBar;


public abstract class BaseQuestionFragmentSocial extends BaseQuestionFragment {

    RatingBar ratingBar;

    protected String getFacebookImageUrl(String userId) {
        return String.format("https://graph.facebook.com/%s/picture?height=300&width=320", userId);
    }

    // The listener sets thumb alpha to 100 on touch.
    protected class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Drawable thumb = seekBar.getThumb();
            if (thumb != null) {
                thumb.setAlpha(100);
                seekBar.setThumb(thumb);
            }
            ratingBar.setSeekBarClicked(true);
            enableSubmitButton(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ratingBar.setRating(seekBar.getProgress() + 1);
        }
    }

}
