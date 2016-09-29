package org.unfoldingword.door43client.models;

/**
 * Represents a single question in a questionnaire
 */
public class Question {
    public final String text;
    public final String help;
    public final boolean isRequired;
    public final String inputType;
    public final int sort;
    public final long dependsOn;
    public final long tdId;

    /**
     *
     * @param text the question
     * @param help optional help text
     * @param isRequired indicates if this question requires an answer
     * @param inputType the type of form input used to display this question e.g. input text, boolean
     * @param sort the sorting order of this question
     * @param dependsOn the translation database id of the question that this question depends on.
     * @param tdId the translation database id of this question (server side)
     */
    public Question(String text, String help, boolean isRequired, String inputType, int sort, long dependsOn, long tdId) {
        this.text = text;
        this.help = help;
        this.isRequired = isRequired;
        this.inputType = inputType;
        this.sort = sort;
        this.dependsOn = dependsOn;
        this.tdId = tdId;
    }

}
