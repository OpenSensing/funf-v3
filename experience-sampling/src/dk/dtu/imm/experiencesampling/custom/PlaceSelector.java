package dk.dtu.imm.experiencesampling.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import dk.dtu.imm.experiencesampling.R;
import dk.dtu.imm.experiencesampling.adapters.PlaceAdapter;
import dk.dtu.imm.experiencesampling.adapters.items.Item;
import dk.dtu.imm.experiencesampling.adapters.items.PlaceEntryItem;
import dk.dtu.imm.experiencesampling.adapters.items.PlaceSectionItem;
import dk.dtu.imm.experiencesampling.models.Place;

import java.util.ArrayList;
import java.util.List;


public class PlaceSelector extends LinearLayout {

    View popupAnchorView;
    EditText placeEdit;
    Context context;

    public PlaceSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        // First inflate the layout for this view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.place_selector, this);

        // Set context for use in other methods
        this.context = context;

        // Get custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlaceSelector, 0, 0);
        String title = (a != null) ? a.getString(R.styleable.PlaceSelector_title) : "";
        String description = (a != null) ? a.getString(R.styleable.PlaceSelector_description) : "";

        // Set values
        setTitle(title);
        setDescription(description);

        // Set place list popup anchor view
        popupAnchorView = findViewById(R.id.place_label_btn);

        // Set place edit text
        placeEdit = (EditText) findViewById(R.id.place_edit);
    }

    public String getPlace() {
        return (placeEdit.getText() != null) ? placeEdit.getText().toString().trim().toLowerCase() : null;
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.place_title)).setText(title);
    }

    public void setDescription(String title) {
        ((TextView) findViewById(R.id.place_description)).setText(title);
    }

    public void setPlaceEditTextWatcher(TextWatcher textWatcher) {
        placeEdit.addTextChangedListener(textWatcher);
    }

    public void setPlaceLabelsListPopupWindow(final List<Place> topPlaces, final List<Place> allPlaces) {
        final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
        final List<Place> combinedPlaces = new ArrayList<Place>();

        ArrayList<Item> items = new ArrayList<Item>();

        if (allPlaces.size() > 5) {
            // Used to get the right label from the list position
            combinedPlaces.add(new Place()); // quick and dirty fix for section index problem
            combinedPlaces.addAll(topPlaces);

            items.add(new PlaceSectionItem("Top 5 places"));
            for (Place place : topPlaces) {
                items.add(new PlaceEntryItem(place.getPlace()));
            }

            ArrayList<Place> placesInCommon = new ArrayList<Place>(allPlaces);
            placesInCommon.retainAll(topPlaces);
            allPlaces.removeAll(placesInCommon); // Remove the places that the top list already has.

            items.add(new PlaceSectionItem("Alphabetical"));
            for (Place place : allPlaces) {
                if (!topPlaces.contains(place)) {
                    items.add(new PlaceEntryItem(place.getPlace()));
                }
            }
            combinedPlaces.add(new Place()); // quick and dirty fix for section index problem
            combinedPlaces.addAll(allPlaces);
        } else {
            combinedPlaces.addAll(topPlaces);
            for (Place place : topPlaces) {
                items.add(new PlaceEntryItem(place.getPlace()));
            }
        }

        PlaceAdapter adapter = new PlaceAdapter(context, items);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setAnchorView(popupAnchorView);
        listPopupWindow.setModal(true);
        listPopupWindow.setWidth(719); // todo: set width in another way when this bug is fixed: https://code.google.com/p/android/issues/detail?id=43174
        listPopupWindow.setHorizontalOffset(-635);

        popupAnchorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allPlaces.size() <= 0) {
                    Toast.makeText(context, "No labels. Please type a label instead.", Toast.LENGTH_LONG).show();
                } else {
                    listPopupWindow.show();
                }
            }
        });

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                placeEdit.setText(combinedPlaces.get((int) id).getPlace());
                listPopupWindow.dismiss();
            }
        });
    }

}
