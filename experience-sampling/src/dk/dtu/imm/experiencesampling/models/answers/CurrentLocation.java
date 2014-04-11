package dk.dtu.imm.experiencesampling.models.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.LocationStatus;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;
import java.util.Date;


@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "first_seen_timestamp", "place_label", "from", "latitude", "longitude", "accuracy", "location_status"})
public class CurrentLocation extends Answer implements Serializable {

    @JsonProperty("place_label")
    private String placeLabel;
    @JsonProperty("from")
    private Date from;
    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("accuracy")
    private float accuracy;
    @JsonProperty("location_status")
    private LocationStatus locationStatus;

    public CurrentLocation() {
        super();
    }

    public CurrentLocation(QuestionType questionType, AnswerType answerType, String placeLabel, Date from, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, firstSeenTimestamp);
        this.id = String.format("%s:%s:%s:%s:%s", questionType, answerType, placeLabel, (from != null) ? from.getTime() : null, endTimestamp);
        this.placeLabel = placeLabel;
        this.from = from;
    }

    public String getPlaceLabel() {
        return placeLabel;
    }

    public void setPlaceLabel(String placeLabel) {
        this.placeLabel = placeLabel;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public LocationStatus getLocationStatus() {
        return locationStatus;
    }

    public void setLocationStatus(LocationStatus locationStatus) {
        this.locationStatus = locationStatus;
    }
}
