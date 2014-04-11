package dk.dtu.imm.experiencesampling.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import dk.dtu.imm.experiencesampling.adapters.items.Item;
import dk.dtu.imm.experiencesampling.adapters.items.PlaceEntryItem;
import dk.dtu.imm.experiencesampling.adapters.items.PlaceSectionItem;

import java.util.ArrayList;


public class PlaceAdapter extends ArrayAdapter<Item> {

    private Context context;
    private ArrayList<Item> items;
    private LayoutInflater inflater;

    public PlaceAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        final Item item = items.get(position);
        if (item != null) {
            if (item.isSection()) {
                PlaceSectionItem sectionItem = (PlaceSectionItem) item;
                v = inflater.inflate(android.R.layout.preference_category, null);

                TextView sectionView = (TextView) v.findViewById(android.R.id.title);
                sectionView.setText(sectionItem.getSectionTitle());
            } else {
                PlaceEntryItem entryItem = (PlaceEntryItem) item;
                v = inflater.inflate(android.R.layout.simple_list_item_1, null);

                // todo: set setOnLongClickListener to present delete label view

                TextView sectionView = (TextView) v.findViewById(android.R.id.text1);
                sectionView.setText(entryItem.getPlaceLabel());
            }
        }

        return v;
    }
}
