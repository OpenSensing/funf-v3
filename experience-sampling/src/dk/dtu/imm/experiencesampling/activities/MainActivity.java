package dk.dtu.imm.experiencesampling.activities;

import android.os.Bundle;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.fragments.MainFragment;

// todo: not necessary when used within the sensible dtu data-collector
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }

    }

}
