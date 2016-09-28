package org.unfoldingword.door43client.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by joel on 9/28/16.
 */

abstract class Language extends DatabaseObject implements Comparable {
    public final String slug;
    public final String name;
    public final String direction;

    /**
     * Creates a new language
     * @param slug the language code
     * @param name the name of the language
     * @param direction the written direction of the language
     */
    public Language(String slug, String name, String direction) {
        this.slug = slug;
        this.name = name;
        this.direction = direction;
    }

    /**
     * Returns the object serialized to json
     * @return
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("slug", slug);
        json.put("name", name);
        json.put("direction", direction);
        return json;
    }

    /**
     * Compares a string or another language with this language
     * @param object
     * @return
     */
    @Override
    public int compareTo(Object object) {
        String slug2;
        if(object instanceof String) {
            slug2 = (String)object;
        } else if(object instanceof Language) {
            slug2 = ((Language) object).slug;
        } else if(object == null) {
            return 1;
        } else {
            // assume language is always greater than a non-language
            Log.w("Language", "Unexpected comparable: " + object.toString());
            return 1;
        }
        return this.slug.compareToIgnoreCase(slug2);
    }
}
