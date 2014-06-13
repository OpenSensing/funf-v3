package dk.dtu.imm.experiencesampling.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import dk.dtu.imm.experiencesampling.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimeFromSelector extends LinearLayout {

    final String[] daysPickerValues = {"Today", "Yesterday", "Two days ago", "Three days ago"};
    final String[] minutesPickerValues = getDisplayedValuesForPicker(0, 45, 15);
    final String[] hoursPickerValues = getDisplayedValuesForPicker(0, 23, 1);

    Animation animSideDown;
    Animation animSideUp;

    LinearLayout mainViewContainer;
    LinearLayout pickerContainer;

    TextView titleText;
    TextView dateText;

    boolean validDate = false;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd. MMM HH:mm");

    public DateTimeFromSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        // First inflate the layout for this view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_time_selector, this);

        // Get custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateTimeFromSelector, 0, 0);

        // Set title
        String title = (a != null) ? a.getString(R.styleable.DateTimeFromSelector_titleText) : "";

        // Load text fields
        dateText = (TextView) findViewById(R.id.selector_date);
        titleText = (TextView) findViewById(R.id.selector_title);

        // Set current time as default
        setCurrentTime();
        setTitle(title);

        // Load the animation
        animSideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        animSideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);

        // Setup date/time pickers
        pickerContainer = (LinearLayout) findViewById(R.id.selector_picker_container);
        setupDateTimePicker(pickerContainer);

        // Set on click event etc.
        mainViewContainer = (LinearLayout) findViewById(R.id.selector_container);
        mainViewContainer.setOnClickListener(getOnClickListener());
    }

    public void setTitle(String title) {
        titleText.setText(title);
    }

    public void setCurrentTime() {
        dateText.setText(simpleDateFormat.format(getCurrentDateTime().getTime()).toLowerCase());

        if (pickerContainer != null) {
            setupDateTimePicker(pickerContainer);
        }
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        dateText.addTextChangedListener(textWatcher);
    }

    public void disable() {
        mainViewContainer.setClickable(false);
        dateText.setTextColor(getResources().getColor(R.color.disable_date_color));
        titleText.setTextColor(getResources().getColor(R.color.disable_date_color));
        hideAll();
    }

    public void enable() {
        mainViewContainer.setClickable(true);
        dateText.setTextColor(getResources().getColor(android.R.color.white));
        titleText.setTextColor(getResources().getColor(android.R.color.white));
    }

    public Date getSelectedDate() {
        return getDateFromNumberPickers();
    }

    public void setDateValid(boolean valid) {
        if (valid) {
            validDate = true;
            dateText.setPaintFlags(dateText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        } else {
            validDate = false;
            dateText.setPaintFlags(dateText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private OnClickListener getOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // First close all of these views existing in the root/main view
                hideAll();

                // Next open clicked view
                if (pickerContainer.getVisibility() == GONE) {
                    pickerContainer.setAnimation(animSideDown);
                    pickerContainer.setVisibility(VISIBLE);
                    if (pickerContainer.getAnimation() != null)
                        pickerContainer.getAnimation().start();
                } else {
                    pickerContainer.setVisibility(GONE);
                }
            }
        };
    }

    private void hideAll() {
        ViewGroup viewGroup = (ViewGroup) getRootView();
        List<View> selectorViews = getViewsByTag(viewGroup, "selector_picker_container");
        // Set visibility GONE for all DateTimeFromSelector views except the current one.
        for (View view : selectorViews) {
            if (!pickerContainer.equals(view)) {
                view.setVisibility(GONE);
            }
        }
    }

    private ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

    private void setupDateTimePicker(View pickerContainer) {
        Calendar currentDate = getCurrentDateTime();

        int dayValue = 0; // today
        int hourValue = currentDate.get(Calendar.HOUR_OF_DAY);
        int minuteValue = getNearestQuarterIndex(currentDate.get(Calendar.MINUTE));

        // Minutes number picker
        NumberPicker npMinutes = (NumberPicker) pickerContainer.findViewById(R.id.selector_picker_minute);
        npMinutes.setSaveFromParentEnabled(false);
        npMinutes.setSaveEnabled(true);
        npMinutes.setMaxValue(minutesPickerValues.length - 1);
        npMinutes.setMinValue(0);
        npMinutes.setValue(minuteValue);
        npMinutes.setWrapSelectorWheel(true);
        npMinutes.setDisplayedValues(minutesPickerValues);

        // Hours number picker
        NumberPicker npHours = (NumberPicker) pickerContainer.findViewById(R.id.selector_picker_hour);
        npHours.setSaveFromParentEnabled(false);
        npHours.setSaveEnabled(true);
        npHours.setMaxValue(hoursPickerValues.length - 1);
        npHours.setMinValue(0);
        npHours.setValue(hourValue);
        npHours.setWrapSelectorWheel(true);
        npHours.setDisplayedValues(hoursPickerValues);

        // Days number picker
        NumberPicker npDays = (NumberPicker) pickerContainer.findViewById(R.id.selector_picker_day);
        npDays.setSaveFromParentEnabled(false);
        npDays.setSaveEnabled(true);
        npDays.setMaxValue(daysPickerValues.length - 1);
        npDays.setMinValue(dayValue);
        npDays.setValue(dayValue);
        npDays.setWrapSelectorWheel(true);
        npDays.setDisplayedValues(daysPickerValues);

        NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Date selectedDate = getDateFromNumberPickers();
                TextView dateText = (TextView) findViewById(R.id.selector_date);
                dateText.setText(simpleDateFormat.format(selectedDate).toLowerCase());
            }
        };

        npMinutes.setOnValueChangedListener(onValueChangeListener);
        npHours.setOnValueChangedListener(onValueChangeListener);
        npDays.setOnValueChangedListener(onValueChangeListener);
    }

    private Date getDateFromNumberPickers() {
        int day = ((NumberPicker) pickerContainer.findViewById(R.id.selector_picker_day)).getValue();
        int hour = ((NumberPicker) pickerContainer.findViewById(R.id.selector_picker_hour)).getValue();
        int minute = ((NumberPicker) pickerContainer.findViewById(R.id.selector_picker_minute)).getValue();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hoursPickerValues[hour]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minutesPickerValues[minute]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, - day);
        return calendar.getTime();
    }

    private String[] getDisplayedValuesForPicker(int from, int to, int interval) {
        List<String> displayedValues = new ArrayList<String>();
        for (int i = from; i <= to; i += interval) {
            displayedValues.add(String.format("%02d", i));
        }
        return displayedValues.toArray(new String[displayedValues.size()]);
    }

    private int getNearestQuarterIndex(int nearestQuarter) {
        // Find index for nearest quarter
        int nearestQuarterIndex = 0;
        //int nearestQuarter = getNearestQuarter();
        for (int i = 0 ; i < minutesPickerValues.length ; i++) {
            if (Integer.parseInt(minutesPickerValues[i]) == nearestQuarter) {
                nearestQuarterIndex = i;
                break;
            }
        }
        return nearestQuarterIndex;
    }

    private int getNearestQuarter() {
        // Find nearest 15 min (00, 15, 30 or 45)
        int currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);
        int modMinutes = currentMinutes % 15;
        int diffToNearestQuarter = (modMinutes < 8) ? -modMinutes : (15-modMinutes);
        return currentMinutes + diffToNearestQuarter;
    }

    private Calendar getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();

        int nearestQuarter = getNearestQuarter();

        // if time index = -1, because 00m has been set 15m back to 45m, then the hour should be decrease by 1.
        int minuteIndex = getNearestQuarterIndex(nearestQuarter);

        // If nearest quarter is in the future, then take the previous quarter. Because a value after now cannot be selected.
        minuteIndex = (Calendar.getInstance().get(Calendar.MINUTE) > nearestQuarter) ? minuteIndex : minuteIndex -1;

        if (minuteIndex < 0) {
            // if the minutes is 00 then set them back to 45 and decrease the hours by one.
            if (calendar.get(Calendar.MINUTE) == 0) {
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                calendar.set(Calendar.MINUTE, Integer.parseInt(minutesPickerValues[minutesPickerValues.length - 1]));
            }
        } else {
            calendar.set(Calendar.MINUTE, Integer.parseInt(minutesPickerValues[minuteIndex]));
        }

        // ignore seconds and millis
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

}
