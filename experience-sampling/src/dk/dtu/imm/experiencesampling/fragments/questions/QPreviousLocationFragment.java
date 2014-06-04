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
import dk.dtu.imm.experiencesampling.custom.DateTimeFromToSelector;
import dk.dtu.imm.experiencesampling.custom.PlaceSelector;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.models.Place;
import dk.dtu.imm.experiencesampling.models.answers.PreviousLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QPreviousLocationFragment extends BaseQuestionFragmentLocation {

    PlaceSelector placeSelector;
    DateTimeFromToSelector datesSelector;

    public static final QPreviousLocationFragment newInstance(List<Place> topPlaces, List<Place> allPlaces) {
        QPreviousLocationFragment fragment = new QPreviousLocationFragment();
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
            // Setup again to reset time to current time.
            datesSelector.setCurrentTime();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_q_location_previous, container, false);
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

        datesSelector = (DateTimeFromToSelector) view.findViewById(R.id.location_q_date_time_from_to_selector);
        datesSelector.setTextWatcher(getValidatorTextWatcher());

        // Create onClickListener for submit buttons
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null && getView() != null) {

                    AnswerType answerType;
                    String place = null;
                    Date from = null;
                    Date to = null;

                    int viewId = view.getId();
                    if (viewId == R.id.location_q_btn_submit) {
                        answerType = AnswerType.ANSWERED_SUBMIT;
                        place = placeSelector.getPlace();
                        from = datesSelector.getFromDate();
                        to = datesSelector.getToDate();

                        // If label is empty, show error message and return
                        if (place == null || place.isEmpty()) {
                            Toast.makeText(getActivity(), "Please type a label", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (from.after(new Date())) {
                            Toast.makeText(getActivity(), "From should be before now", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (from.after(to) || from.equals(to)) {
                            Toast.makeText(getActivity(), "To should be after from", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        answerType = AnswerType.ANSWERED_NO_THANKS;
                    }

                    // Create answer
                    PreviousLocation answer = new PreviousLocation(answerType, place, from, to, startTimestamp, new Date().getTime(), loadedTimestamp);
                    saveQuestion(answer);
                    getActivity().finish();
                }
            }
        };

        // Set defined listener from above
        btnSubmit.setOnClickListener(onClickListener);
        btnNoThanks.setOnClickListener(onClickListener);
    }

    // Create from- and to date validator
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
                    datesSelector.disable();
                    enableSubmitButton(false);
                    return;
                } else {
                    datesSelector.enable();
                    enableSubmitButton(true);
                }

                Date fromDate = datesSelector.getFromDate();
                Date toDate = datesSelector.getToDate();
                Date now = new Date();

                // From date check
                if (fromDate.after(now)) {
                    // From date should be before now
                    enableSubmitButton(false);
                    datesSelector.setFromDateValid(false);
                } else {
                    datesSelector.setFromDateValid(true);
                }

                // To date check
                if (toDate.after(now)) {
                    // To should be before now date
                    enableSubmitButton(false);
                    datesSelector.setToDateValid(false);
                } else if (!toDate.after(fromDate)) {
                    // To should be after from
                    enableSubmitButton(false);
                    datesSelector.setToDateValid(false);
                } else {
                    datesSelector.setToDateValid(true);
                }

                // Enable check
                if (fromDate.before(now) && toDate.before(now) && fromDate.before(toDate)) {
                    // success
                    enableSubmitButton(true);
                    datesSelector.setFromDateValid(true);
                    datesSelector.setToDateValid(true);
                }
            }
        };
    }

}
