package dk.dtu.imm.experiencesampling.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.custom.NumberPickerDialogPreference;

// todo: not necessary when used within the sensible dtu data-collector
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if (getActivity() != null) {

            final NumberPickerDialogPreference numberPickerDialogPreference = (NumberPickerDialogPreference) findPreference("pref_number_picker");

            if (numberPickerDialogPreference != null) {
                numberPickerDialogPreference.setSummary(getSummary(numberPickerDialogPreference.getValue()));
                numberPickerDialogPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(getSummary(newValue));
                        return true;
                    }
                });
            }


        }
    }

    private String getSummary(Object questionLimit) {
        return questionLimit + " answers per day";
    }

}
