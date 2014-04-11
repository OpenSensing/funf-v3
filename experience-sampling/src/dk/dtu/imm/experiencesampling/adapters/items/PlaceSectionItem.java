package dk.dtu.imm.experiencesampling.adapters.items;

public class PlaceSectionItem implements Item {

    private final String sectionTitle;

    public PlaceSectionItem(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
