package dk.dtu.imm.experiencesampling.fragments.questions;

import dk.dtu.imm.experiencesampling.models.Place;

import java.util.List;

public abstract class BaseQuestionFragmentLocation extends BaseQuestionFragment {

    protected static final String TOP_PLACES_KEY = "top_places";
    protected static final String ALL_PLACES_KEY = "all_places";

    protected List<Place> topPlaces;
    protected List<Place> allPlaces;

}
