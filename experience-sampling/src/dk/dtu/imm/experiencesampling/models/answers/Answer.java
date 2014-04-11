package dk.dtu.imm.experiencesampling.models.answers;


import com.fasterxml.jackson.annotation.JsonProperty;
import dk.dtu.imm.experiencesampling.enums.AnswerType;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

import java.io.Serializable;

public abstract class Answer implements Serializable {

    @JsonProperty("id")
    protected String id;
    @JsonProperty("question_type")
    protected QuestionType questionType;
    @JsonProperty("answer_type")
    protected AnswerType answerType;
    @JsonProperty("start_timestamp")
    protected long startTimestamp;
    @JsonProperty("end_timestamp")
    protected long endTimestamp;
    @JsonProperty("first_seen_timestamp")
    protected long firstSeenTimestamp;

    protected Answer() {
    }

    protected Answer(QuestionType questionType, AnswerType answerType, long startTimestamp, long endTimestamp, long firstSeenTimestamp) {
        this.questionType = questionType;
        this.answerType = answerType;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.firstSeenTimestamp = firstSeenTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public void setAnswerType(AnswerType answerType) {
        this.answerType = answerType;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getFirstSeenTimestamp() {
        return firstSeenTimestamp;
    }

    public void setFirstSeenTimestamp(long firstSeenTimestamp) {
        this.firstSeenTimestamp = firstSeenTimestamp;
    }
}
