package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/7/2016.
 */
public class Questionnaire {
    public final String languageSlug;
    public final String languageName;
    public final String languageDirection;
    public final long tdId;

    public Questionnaire(String languageSlug, String languageName, String languageDirection, long tdId) {
        this.languageSlug = languageSlug;
        this.languageName = languageName;
        this.languageDirection = languageDirection;
        this.tdId = tdId;
    }
}
