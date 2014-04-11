package dk.dtu.imm.experiencesampling.fragments.questions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.custom.DateTimeFromSelector;
import dk.dtu.imm.experiencesampling.custom.PlaceSelector;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;
import dk.dtu.imm.experiencesampling.models.Place;
import dk.dtu.imm.experiencesampling.models.answers.CurrentLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class QCurrentLocationFragment extends BaseQuestionFragmentLocation {

    PlaceSelector placeSelector;
    DateTimeFromSelector dateSelector;

    public static final QCurrentLocationFragment newInstance(List<Place> topPlaces, List<Place> allPlaces) {
        QCurrentLocationFragment fragment = new QCurrentLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(TOP_PLACES_KEY, (ArrayList<Place>) topPlaces);
        bundle.putSerializable(ALL_PLACES_KEY, (ArrayList<Place>) allPlaces);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topPlaces = (ArrayList<Place>) getArguments().getSerializable(TOP_PLACES_KEY);
            allPlaces = (ArrayList<Place>) getArguments().getSerializable(ALL_PLACES_KEY);
        }

        if (topPlaces == null || allPlaces == null) {
            if (getActivity() != null)
                getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getView() != null) {
            dateSelector.setCurrentTime();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_q_location_current, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load buttons
        btnSubmit = (Button) view.findViewById(R.id.location_q_btn_submit);
        Button btnNoThanks = (Button) view.findViewById(R.id.location_q_btn_no_thanks);

        placeSelector = (PlaceSelector) view.findViewById(R.id.location_q_place_selector);
        placeSelector.setPlaceLabelsListPopupWindow(topPlaces, allPlaces);
        placeSelector.setPlaceEditTextWatcher(getValidatorTextWatcher());

        dateSelector = (DateTimeFromSelector) view.findViewById(R.id.location_q_date_time_from_selector);
        dateSelector.setTextWatcher(getValidatorTextWatcher());

        // Create onClickListener for submit buttons
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null && getView() != null) {

                    AnswerType answerType;
                    String place = placeSelector.getPlace();
                    Date from = null;

                    int viewId = view.getId();
                    if (viewId == R.id.location_q_btn_submit) {
                        // Get info from view and add to answer
                        answerType = AnswerType.ANSWERED;
                        from = dateSelector.getSelectedDate();

                        // If place is empty, show error message and return
                        if (place == null || place.isEmpty()) {
                            Toast.makeText(getActivity(), "Please type a label", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (from.after(new Date())) {
                            Toast.makeText(getActivity(), "From should be before now", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        answerType = AnswerType.NO_THANKS;
                    }

                    // Create answer
                    CurrentLocation answer = new CurrentLocation(QuestionType.LOCATION_CURRENT, answerType, place, from, startTimestamp, new Date().getTime(), firstSeenTimestamp);
                    saveQuestion(answer);
                    getActivity().finish();
                }
            }
        };

        // Set defined listener from above
        btnSubmit.setOnClickListener(onClickListener);
        btnNoThanks.setOnClickListener(onClickListener);
    }

    // Create validator. Place must not be empty and time must not be after current time.
    private TextWatcher getValidatorTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (placeSelector.getPlace() == null || placeSelector.getPlace().equals("")) {
                    dateSelector.disable();
                    enableSubmitButton(false);
                    return;
                } else {
                    dateSelector.enable();
                    enableSubmitButton(true);
                }

                Date date = dateSelector.getSelectedDate();
                if (date.after(new Date())) {
                    // Selected date should be before now
                    dateSelector.setDateValid(false);
                    enableSubmitButton(false);
                } else {
                    dateSelector.setDateValid(true);
                    enableSubmitButton(true);
                }
            }
        };
    }
}
