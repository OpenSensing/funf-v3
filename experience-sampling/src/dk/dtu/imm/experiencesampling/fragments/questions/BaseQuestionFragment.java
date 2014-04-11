package dk.dtu.imm.experiencesampling.fragments.questions;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.models.answers.Answer;
import dk.dtu.imm.experiencesampling.services.QuestionSaveService;

import java.util.Date;

public abstract class BaseQuestionFragment extends Fragment {

    protected long firstSeenTimestamp;
    protected long startTimestamp;

    protected Button btnSubmit;
    protected Drawable btnSubmitEnabled;
    protected Drawable btnSubmitDisabled;

    @Override
    public void onResume() {
        super.onResume();
        if (firstSeenTimestamp == 0) {
            firstSeenTimestamp = new Date().getTime();
        }
        this.startTimestamp = new Date().getTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        btnSubmitEnabled = getResources().getDrawable(R.drawable.greenbutton_btn_default_holo_light);
        btnSubmitDisabled = getResources().getDrawable(R.drawable.greenbutton_btn_default_disabled_holo_light);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void saveQuestion(Answer answer) {
        if (getActivity() != null) {
            Intent saveIntent = new Intent(getActivity(), QuestionSaveService.class);
            saveIntent.putExtra("answer", answer);
            getActivity().startService(saveIntent);
        }
    }

    protected void enableSubmitButton(boolean enable) {
        if (btnSubmit != null) {
            if (enable) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackground(btnSubmitEnabled);
                btnSubmit.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackground(btnSubmitDisabled);
                btnSubmit.setTextColor(getResources().getColor(R.color.disable_date_color));
            }
        }
    }

}
