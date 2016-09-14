package org.unfoldingword.door43client.objects;

/**
 * Represents a single question in a questionnaire
 */
public class Question {
    public final String text;
    public final String help;
    public final boolean isRequired;
    public final String inputType;
    public final int sort;
    public final int dependsOn;
    public final int tdId;

    public Question(String text, String help, boolean isRequired, String inputType, int sort, int dependsOn, int tdId) {
        this.text = text;
        this.help = help;
        this.isRequired = isRequired;
        this.inputType = inputType;
        this.sort = sort;
        this.dependsOn = dependsOn;
        this.tdId = tdId;
    }
}
