package com.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class representing a single BMI calculation record.
 * Contains input values, computed BMI value, category, unit type, and timestamp.
 */
public class BmiRecord {
    private long timestamp;
    private double height;
    private double weight;
    private double bmiValue;
    private String category;
    private String unitType; // "Metric" or "Imperial"

    public BmiRecord(long timestamp, double height, double weight, double bmiValue, String category, String unitType) {
        this.timestamp = timestamp;
        this.height = height;
        this.weight = weight;
        this.bmiValue = bmiValue;
        this.category = category;
        this.unitType = unitType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public double getBmiValue() {
        return bmiValue;
    }

    public String getCategory() {
        return category;
    }

    public String getUnitType() {
        return unitType;
    }

    /**
     * Formats the timestamp into a human-readable date and time string.
     */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Serializes this BmiRecord to a JSONObject for SharedPreferences persistence.
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("timestamp", timestamp);
        json.put("height", height);
        json.put("weight", weight);
        json.put("bmiValue", bmiValue);
        json.put("category", category);
        json.put("unitType", unitType);
        return json;
    }

    /**
     * Constructs a BmiRecord from a JSONObject representation.
     */
    public static BmiRecord fromJSONObject(JSONObject json) throws JSONException {
        return new BmiRecord(
            json.getLong("timestamp"),
            json.getDouble("height"),
            json.getDouble("weight"),
            json.getDouble("bmiValue"),
            json.getString("category"),
            json.optString("unitType", "Metric")
        );
    }
}
