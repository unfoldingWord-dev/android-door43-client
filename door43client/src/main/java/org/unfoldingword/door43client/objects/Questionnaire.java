package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/7/2016.
 */
public class Questionnaire {
    public static String language_slug;
    public static String language_name;
    public static String language_direction;
    public static int td_id;

    public Questionnaire(String language_slug, String language_name, String language_direction, int td_id) {
        this.language_slug = language_slug;
        this.language_name = language_name;
        this. language_direction = language_direction;
        this.td_id = td_id;
    }
}
