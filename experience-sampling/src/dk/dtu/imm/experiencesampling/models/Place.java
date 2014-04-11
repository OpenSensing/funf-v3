package dk.dtu.imm.experiencesampling.models;

import java.io.Serializable;

public class Place implements Serializable {

    private String place;
    private int count;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;

        Place place1 = (Place) o;

        if (count != place1.count) return false;
        if (place != null ? !place.equals(place1.place) : place1.place != null) return false;

        return true;
    }

}
