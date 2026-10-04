package com.example;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Helper class for persisting and managing BMI calculation history using SharedPreferences.
 */
public class BmiHistoryManager {
    private static final String PREF_NAME = "BmiHealthCheckerPrefs";
    private static final String KEY_HISTORY = "bmi_history_list";
    private static final int MAX_HISTORY_ITEMS = 50;

    /**
     * Saves a new BMI calculation record to SharedPreferences.
     */
    public static void saveRecord(Context context, BmiRecord record) {
        List<BmiRecord> currentHistory = getHistory(context);
        currentHistory.add(0, record); // Prepend to show newest first

        if (currentHistory.size() > MAX_HISTORY_ITEMS) {
            currentHistory = currentHistory.subList(0, MAX_HISTORY_ITEMS);
        }

        saveHistoryList(context, currentHistory);
    }

    /**
     * Retrieves all saved BMI records from SharedPreferences.
     */
    public static List<BmiRecord> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonString = prefs.getString(KEY_HISTORY, "[]");
        List<BmiRecord> records = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                records.add(BmiRecord.fromJSONObject(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Deletes a single record at the specified index position.
     */
    public static void deleteRecord(Context context, int position) {
        List<BmiRecord> history = getHistory(context);
        if (position >= 0 && position < history.size()) {
            history.remove(position);
            saveHistoryList(context, history);
        }
    }

    /**
     * Clears all saved BMI records.
     */
    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    /**
     * Helper to write a list of BmiRecords back to SharedPreferences.
     */
    private static void saveHistoryList(Context context, List<BmiRecord> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for (BmiRecord rec : list) {
            try {
                jsonArray.put(rec.toJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply();
    }
}
