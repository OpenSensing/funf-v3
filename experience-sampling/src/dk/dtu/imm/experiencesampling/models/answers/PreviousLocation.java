package dk.dtu.imm.experiencesampling.models.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;
import java.util.Date;


@JsonPropertyOrder({"question_type", "answer_type", "start_timestamp", "end_timestamp", "first_seen_timestamp", "place_label", "from", "to"})
public class PreviousLocation extends Answer implements Serializable {

    @JsonProperty("place_label")
    private String placeLabel;
    @JsonProperty("from")
    private Date from;
    @JsonProperty("to")
    private Date to;

    public PreviousLocation() {
        super();
    }

    public PreviousLocation(QuestionType questionType, AnswerType answerType, String placeLabel, Date from, Date to, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        super(questionType, answerType, startTimestamp, endTimestamp, firstSeenTimestamp);
        this.id = String.format("%s:%s:%s:%s:%s:%s", questionType, answerType, placeLabel, (from != null) ? from.getTime() : null, (to != null) ? to.getTime() : null, endTimestamp);
        this.placeLabel = placeLabel;
        this.from = from;
        this.to = to;
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

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }
}
