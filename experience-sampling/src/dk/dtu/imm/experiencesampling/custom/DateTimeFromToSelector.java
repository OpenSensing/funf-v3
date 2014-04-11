package dk.dtu.imm.experiencesampling.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import dk.dtu.imm.experiencesampling.R;

import java.util.Date;


public class DateTimeFromToSelector extends LinearLayout {

    DateTimeFromSelector fromSelector;
    DateTimeFromSelector toSelector;
    TextView fromDateText;
    TextView toDateText;

    public DateTimeFromToSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        // First inflate the layout for this view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_time_from_to_selector, this);

        // Get custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateTimeFromToSelector, 0, 0);

        // Get date time selectors
        fromSelector = (DateTimeFromSelector) findViewById(R.id.selector_from);
        toSelector = (DateTimeFromSelector) findViewById(R.id.selector_to);

        // Set title
        String title1 = (a != null) ? a.getString(R.styleable.DateTimeFromToSelector_titleText1) : "";
        String title2 = (a != null) ? a.getString(R.styleable.DateTimeFromToSelector_titleText2) : "";
        fromSelector.setTitle(title1);
        toSelector.setTitle(title2);

        // Set validator on from- and to date
        fromDateText = (TextView) fromSelector.findViewById(R.id.selector_date);
        toDateText = (TextView) toSelector.findViewById(R.id.selector_date);
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        fromDateText.addTextChangedListener(textWatcher);
        toDateText.addTextChangedListener(textWatcher);
    }

    public void disable() {
        fromSelector.disable();
        toSelector.disable();
    }

    public void enable() {
        fromSelector.enable();
        toSelector.enable();
    }

    public void setCurrentTime() {
        fromSelector.setCurrentTime();
        toSelector.setCurrentTime();
    }

    public void setFromDateValid(boolean valid) {
        if (valid) {
            fromDateText.setPaintFlags(fromDateText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        } else {
            fromDateText.setPaintFlags(fromDateText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public void setToDateValid(boolean valid) {
        if (valid) {
            toDateText.setPaintFlags(toDateText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        } else {
            toDateText.setPaintFlags(toDateText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public Date getFromDate() {
       return fromSelector.getSelectedDate();
    }

    public Date getToDate() {
        return toSelector.getSelectedDate();
    }

}
