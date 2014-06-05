package dk.dtu.imm.experiencesampling.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import dk.dtu.imm.experiencesampling.R;


public class ProfilePicture extends FrameLayout {

    private static final String DEFAULT_IMAGE_URL = "drawable://" + R.drawable.ic_no_image;

    ImageLoader imageLoader;
    ImageView imageView;
    TextView nameText;
    ProgressBar imageProgressBar;

    public ProfilePicture(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Setup image loader
        imageLoader = getImageLoader(context);

        // First inflate the layout for this view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.profile_picture, this);

        // Set views
        imageView = (ImageView) findViewById(R.id.profile_picture_image);
        nameText = (TextView) findViewById(R.id.profile_picture_name);
        imageProgressBar = (ProgressBar) findViewById(R.id.profile_picture_progress_bar);
    }

    public void setProfileName(String name) {
        nameText.setText(name);
    }

    public void setProfileImage(String pictureUrl) {
        imageLoader.displayImage(pictureUrl, imageView, new ProgressImageLoadingListener());
    }

    public void setImageClicked() {
        //imageView.setColorFilter(0xff939393, PorterDuff.Mode.MULTIPLY);
        imageView.setColorFilter(0x7f939393, PorterDuff.Mode.LIGHTEN);
    }

    private class ProgressImageLoadingListener implements ImageLoadingListener {
        @Override
        public void onLoadingStarted(String s, View view) {

        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            if (view != null) {
                // handle imageView here and dismiss the progress bar related to this
                if (imageProgressBar != null) {
                    imageProgressBar.setVisibility(View.GONE);
                }
                // insert default fail image
                imageLoader.displayImage(DEFAULT_IMAGE_URL, imageView);
            }
        }

        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
            if (view != null) {
                // handle imageView here and dismiss the progress bar related to this
                if (imageProgressBar != null) {
                    imageProgressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String s, View view) {

        }
    }

    // Normally this would be done only once in the Application onCreate, but this onCreate will never be called in a library module
    // unless it is set in the main app manifest, but that is not good, as it could have another already.
    private ImageLoader getImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(11))
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
        return ImageLoader.getInstance();
    }
}
