package dk.dtu.imm.experiencesampling.models.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.dtu.imm.experiencesampling.enums.QuestionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingQuestion {

    QuestionType questionType;

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
}
