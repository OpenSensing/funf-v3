package dk.dtu.imm.experiencesampling.adapters.items;

public class PlaceEntryItem implements Item {

    public final String placeLabel;

    public PlaceEntryItem(String placeLabel) {
        this.placeLabel = placeLabel;
    }

    public String getPlaceLabel() {
        return placeLabel;
    }

    @Override
    public boolean isSection() {
        return false;
    }
}
