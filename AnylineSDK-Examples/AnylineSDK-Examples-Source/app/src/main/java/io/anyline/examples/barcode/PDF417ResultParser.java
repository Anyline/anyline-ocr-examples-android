package io.anyline.examples.barcode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class PDF417ResultParser {

    private static final String regex = "([a-z])([A-Z]+)";
    private static final String replacement = "$1 $2";

    public static String parsePDF417Result(String pdf417Result) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            JSONObject jsonObject = new JSONObject(pdf417Result);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();

                stringBuilder.append(capitalizeString(replaceCamelCaseWithCapitalEmptyStrings(key)));
                stringBuilder.append(": ");
                stringBuilder.append(jsonObject.optString(key));
                stringBuilder.append("\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String replaceCamelCaseWithCapitalEmptyStrings(String input) {
        return input.replaceAll(regex, replacement);
    }

    private static String capitalizeString(String input) {
        char firstLetter = input.charAt(0);
        return input.replaceFirst(String.valueOf(firstLetter), String.valueOf(Character.toUpperCase(firstLetter)));
    }
}
