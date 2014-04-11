package dk.dtu.imm.experiencesampling.activities;

import android.app.Activity;
import android.os.Bundle;
import dk.dtu.imm.experiencesampling.fragments.SettingsFragment;

// todo: not necessary when used within the sensible dtu data-collector
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
