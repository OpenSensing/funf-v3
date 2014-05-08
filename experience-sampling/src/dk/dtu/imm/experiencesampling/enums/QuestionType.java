package dk.dtu.imm.experiencesampling.enums;


import java.util.ArrayList;
import java.util.List;

public enum QuestionType {

    SOCIAL_CLOSER_FRIEND,
    SOCIAL_RATE_ONE_FRIEND,
    SOCIAL_RATE_TWO_FRIENDS,
    LOCATION_CURRENT,
    LOCATION_PREVIOUS,

    UNKNOWN;

    public static QuestionType getQuestionType(String type) {
        for (QuestionType q : QuestionType.values()) {
            if (q.name().equalsIgnoreCase(type)) {
                return q;
            }
        }
        return UNKNOWN;
    }

    public static List<QuestionType> getAll() {
        List<QuestionType> questionTypes = new ArrayList<QuestionType>();
        for (QuestionType questionType : QuestionType.values()) {
            if (!UNKNOWN.equals(questionType)) {
                questionTypes.add(questionType);
            }
        }
        return questionTypes;
    }

}
