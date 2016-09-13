package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/8/2016.
 */
public class Question {
    public static String text;
    public static String help;
    public static int is_required;
    public static String input_type;
    public static int sort;
    public static int depends_on;
    public static int td_id;
    public static int questionnaire_id;

    public Question(String text, String help, int is_required, String input_type, int sort, int depends_on, int td_id, int questionnaire_id) {
        this.text = text;
        this.help = help;
        this.is_required = is_required;
        this.input_type = input_type;
        this.sort = sort;
        this.depends_on = depends_on;
        this.td_id = td_id;
        this.questionnaire_id = questionnaire_id;
    }
}
