package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/7/2016.
 */
public class Questionnaire {
    public static String languageSlug;
    public static String languageName;
    public static String languageDirection;
    public static int tdId;

    public Questionnaire(String languageSlug, String languageName, String languageDirection, int tdId) {
        this.languageSlug = languageSlug;
        this.languageName = languageName;
        this. languageDirection = languageDirection;
        this.tdId = tdId;
    }
}
