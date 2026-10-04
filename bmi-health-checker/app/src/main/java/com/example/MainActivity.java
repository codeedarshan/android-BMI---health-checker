package com.example;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity for the BMI Health Checker application.
 * Features real-time interactive sliders, gender & age personalization,
 * healthy target weight estimations, water goals, BMR calculations, and history.
 */
public class MainActivity extends AppCompatActivity {

    // Layout Containers
    private View rootLayout;
    private NestedScrollView scrollView;

    // UI Controls
    private MaterialButtonToggleGroup unitToggleGroup;
    private MaterialButtonToggleGroup genderToggleGroup;
    private TextInputLayout inputLayoutAge;
    private TextInputEditText etAge;
    private TextInputLayout inputLayoutHeight;
    private TextInputLayout inputLayoutWeight;
    private TextInputEditText etHeight;
    private TextInputEditText etWeight;
    private Slider sliderHeight;
    private Slider sliderWeight;
    private MaterialButton btnCalculate;
    private MaterialButton btnReset;
    private MaterialButton btnHistory;

    // Result Display
    private MaterialCardView cardResult;
    private TextView tvBmiValue;
    private TextView tvBmiCategory;
    private LinearProgressIndicator bmiProgressIndicator;

    // Interactive Insights
    private MaterialCardView cardInsights;
    private TextView tvTargetWeightRange;
    private TextView tvWeightDiff;
    private TextView tvWaterGoal;
    private TextView tvBmrEstimate;

    // Advice Display
    private MaterialCardView cardAdvice;
    private ImageView ivAdviceIcon;
    private TextView tvAdviceText;

    // State Flags
    private boolean isMetric = true;
    private boolean isUpdatingFromSlider = false;
    private boolean isUpdatingFromText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        syncInitialSliders();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        scrollView = findViewById(R.id.scrollView);

        unitToggleGroup = findViewById(R.id.unitToggleGroup);
        genderToggleGroup = findViewById(R.id.genderToggleGroup);
        inputLayoutAge = findViewById(R.id.inputLayoutAge);
        etAge = findViewById(R.id.etAge);

        inputLayoutHeight = findViewById(R.id.inputLayoutHeight);
        inputLayoutWeight = findViewById(R.id.inputLayoutWeight);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        sliderHeight = findViewById(R.id.sliderHeight);
        sliderWeight = findViewById(R.id.sliderWeight);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnReset = findViewById(R.id.btnReset);
        btnHistory = findViewById(R.id.btnHistory);

        cardResult = findViewById(R.id.cardResult);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        bmiProgressIndicator = findViewById(R.id.bmiProgressIndicator);

        cardInsights = findViewById(R.id.cardInsights);
        tvTargetWeightRange = findViewById(R.id.tvTargetWeightRange);
        tvWeightDiff = findViewById(R.id.tvWeightDiff);
        tvWaterGoal = findViewById(R.id.tvWaterGoal);
        tvBmrEstimate = findViewById(R.id.tvBmrEstimate);

        cardAdvice = findViewById(R.id.cardAdvice);
        ivAdviceIcon = findViewById(R.id.ivAdviceIcon);
        tvAdviceText = findViewById(R.id.tvAdviceText);
    }

    private void syncInitialSliders() {
        etHeight.setText(String.format(Locale.US, "%.0f", sliderHeight.getValue()));
        etWeight.setText(String.format(Locale.US, "%.1f", sliderWeight.getValue()));
    }

    /**
     * Safely updates Material Slider boundaries, step size, and value without triggering
     * IllegalArgumentException when transitioning between different unit scale bounds.
     */
    private void updateSliderSafely(Slider slider, float newFrom, float newTo, float newValue, float stepSize) {
        slider.setStepSize(0f);
        slider.setValueFrom(Math.min(slider.getValueFrom(), newFrom));
        slider.setValueTo(Math.max(slider.getValueTo(), newTo));
        slider.setValue(newValue);
        slider.setValueFrom(newFrom);
        slider.setValueTo(newTo);
        slider.setStepSize(stepSize);
    }

    /**
     * Hides the soft keyboard cleanly and clears focused inputs to avoid IME insets timeouts.
     */
    private void hideKeyboard() {
        View currentFocusView = getCurrentFocus();
        if (currentFocusView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
            }
            currentFocusView.clearFocus();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        // Dismiss keyboard when tapping empty space or scrolling
        if (rootLayout != null) {
            rootLayout.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                }
                return false;
            });
        }
        if (scrollView != null) {
            scrollView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                }
                return false;
            });
        }

        // Unit System Selection Toggle
        unitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                hideKeyboard();
                if (checkedId == R.id.btnUnitMetric) {
                    isMetric = true;
                    inputLayoutHeight.setHint(getString(R.string.label_height_cm));
                    inputLayoutWeight.setHint(getString(R.string.label_weight_kg));
                    updateSliderSafely(sliderHeight, 100f, 220f, 175f, 1.0f);
                    updateSliderSafely(sliderWeight, 30f, 180f, 70f, 0.5f);
                } else if (checkedId == R.id.btnUnitImperial) {
                    isMetric = false;
                    inputLayoutHeight.setHint(getString(R.string.label_height_in));
                    inputLayoutWeight.setHint(getString(R.string.label_weight_lbs));
                    updateSliderSafely(sliderHeight, 40f, 90f, 68f, 1.0f);
                    updateSliderSafely(sliderWeight, 70f, 400f, 154f, 0.5f);
                }
                syncInitialSliders();
                clearErrors();
            }
        });

        // Hide keyboard when user interacts with sliders
        sliderHeight.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(Slider slider) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(Slider slider) {}
        });

        sliderWeight.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(Slider slider) {
                hideKeyboard();
            }

            @Override
            public void onStopTrackingTouch(Slider slider) {}
        });

        // Slider Height Realtime Change Listener
        sliderHeight.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && !isUpdatingFromText) {
                isUpdatingFromSlider = true;
                etHeight.setText(String.format(Locale.US, isMetric ? "%.0f" : "%.1f", value));
                isUpdatingFromSlider = false;
            }
        });

        // Slider Weight Realtime Change Listener
        sliderWeight.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && !isUpdatingFromText) {
                isUpdatingFromSlider = true;
                etWeight.setText(String.format(Locale.US, "%.1f", value));
                isUpdatingFromSlider = false;
            }
        });

        // Height EditText Watcher with safe step snapping
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingFromSlider && s != null && s.length() > 0) {
                    try {
                        String cleanStr = s.toString().trim().replace(',', '.');
                        if (!cleanStr.isEmpty()) {
                            float val = Float.parseFloat(cleanStr);
                            if (val >= sliderHeight.getValueFrom() && val <= sliderHeight.getValueTo()) {
                                float step = sliderHeight.getStepSize();
                                if (step > 0) {
                                    val = Math.round((val - sliderHeight.getValueFrom()) / step) * step + sliderHeight.getValueFrom();
                                    val = Math.max(sliderHeight.getValueFrom(), Math.min(sliderHeight.getValueTo(), val));
                                }
                                isUpdatingFromText = true;
                                sliderHeight.setValue(val);
                                isUpdatingFromText = false;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Weight EditText Watcher with safe step snapping
        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdatingFromSlider && s != null && s.length() > 0) {
                    try {
                        String cleanStr = s.toString().trim().replace(',', '.');
                        if (!cleanStr.isEmpty()) {
                            float val = Float.parseFloat(cleanStr);
                            if (val >= sliderWeight.getValueFrom() && val <= sliderWeight.getValueTo()) {
                                float step = sliderWeight.getStepSize();
                                if (step > 0) {
                                    val = Math.round((val - sliderWeight.getValueFrom()) / step) * step + sliderWeight.getValueFrom();
                                    val = Math.max(sliderWeight.getValueFrom(), Math.min(sliderWeight.getValueTo(), val));
                                }
                                isUpdatingFromText = true;
                                sliderWeight.setValue(val);
                                isUpdatingFromText = false;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle Keyboard Action Done on Weight input
        etWeight.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                calculateBmiAndInsights();
                return true;
            }
            return false;
        });

        // Calculate Button
        btnCalculate.setOnClickListener(v -> {
            hideKeyboard();
            animateButtonClick(v);
            calculateBmiAndInsights();
        });

        // Reset Button
        btnReset.setOnClickListener(v -> {
            hideKeyboard();
            animateButtonClick(v);
            resetInputs();
        });

        // History Dialog Button
        btnHistory.setOnClickListener(v -> {
            hideKeyboard();
            animateButtonClick(v);
            showHistoryDialog();
        });
    }

    private void calculateBmiAndInsights() {
        clearErrors();

        String heightStr = etHeight.getText() != null ? etHeight.getText().toString().trim().replace(',', '.') : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString().trim().replace(',', '.') : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";

        boolean isValid = true;
        if (heightStr.isEmpty()) {
            inputLayoutHeight.setError(getString(R.string.error_empty_height));
            isValid = false;
        }

        if (weightStr.isEmpty()) {
            inputLayoutWeight.setError(getString(R.string.error_empty_weight));
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please enter both height and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        double heightVal;
        double weightVal;
        int ageVal = 25; // Default age if empty

        try {
            heightVal = Double.parseDouble(heightStr);
            if (heightVal <= 10 || heightVal > 300) {
                inputLayoutHeight.setError(getString(R.string.error_invalid_height));
                return;
            }
        } catch (NumberFormatException e) {
            inputLayoutHeight.setError(getString(R.string.error_invalid_height));
            return;
        }

        try {
            weightVal = Double.parseDouble(weightStr);
            if (weightVal <= 2 || weightVal > 600) {
                inputLayoutWeight.setError(getString(R.string.error_invalid_weight));
                return;
            }
        } catch (NumberFormatException e) {
            inputLayoutWeight.setError(getString(R.string.error_invalid_weight));
            return;
        }

        if (!ageStr.isEmpty()) {
            try {
                int parsedAge = Integer.parseInt(ageStr);
                if (parsedAge > 0 && parsedAge <= 120) {
                    ageVal = parsedAge;
                }
            } catch (NumberFormatException ignored) {}
        }

        // 1. BMI Calculation Formula
        double bmi;
        if (isMetric) {
            double heightInMeters = heightVal / 100.0;
            bmi = weightVal / (heightInMeters * heightInMeters);
        } else {
            bmi = (weightVal / (heightVal * heightVal)) * 703.0;
        }

        // 2. Category & Advice Selection
        String category;
        int colorRes;
        String advice;

        if (bmi < 18.5) {
            category = getString(R.string.category_underweight);
            colorRes = R.color.bmi_underweight;
            advice = getString(R.string.advice_underweight);
        } else if (bmi <= 24.9) {
            category = getString(R.string.category_normal);
            colorRes = R.color.bmi_normal;
            advice = getString(R.string.advice_normal);
        } else if (bmi <= 29.9) {
            category = getString(R.string.category_overweight);
            colorRes = R.color.bmi_overweight;
            advice = getString(R.string.advice_overweight);
        } else {
            category = getString(R.string.category_obese);
            colorRes = R.color.bmi_obese;
            advice = getString(R.string.advice_obese);
        }

        // 3. Health & Body Insights Calculations
        String targetRangeStr;
        String weightDiffStr;
        double minTargetWeight;
        double maxTargetWeight;

        if (isMetric) {
            double heightM = heightVal / 100.0;
            minTargetWeight = 18.5 * heightM * heightM;
            maxTargetWeight = 24.9 * heightM * heightM;
            targetRangeStr = String.format(Locale.US, "%.1f kg – %.1f kg", minTargetWeight, maxTargetWeight);

            if (weightVal < minTargetWeight) {
                weightDiffStr = String.format(Locale.US, "Gain ~%.1f kg to reach healthy BMI", (minTargetWeight - weightVal));
            } else if (weightVal > maxTargetWeight) {
                weightDiffStr = String.format(Locale.US, "Lose ~%.1f kg to reach healthy BMI", (weightVal - maxTargetWeight));
            } else {
                weightDiffStr = "You are within your ideal healthy weight range!";
            }
        } else {
            double heightM = heightVal * 0.0254;
            minTargetWeight = (18.5 * heightM * heightM) * 2.20462;
            maxTargetWeight = (24.9 * heightM * heightM) * 2.20462;
            targetRangeStr = String.format(Locale.US, "%.1f lbs – %.1f lbs", minTargetWeight, maxTargetWeight);

            if (weightVal < minTargetWeight) {
                weightDiffStr = String.format(Locale.US, "Gain ~%.1f lbs to reach healthy BMI", (minTargetWeight - weightVal));
            } else if (weightVal > maxTargetWeight) {
                weightDiffStr = String.format(Locale.US, "Lose ~%.1f lbs to reach healthy BMI", (weightVal - maxTargetWeight));
            } else {
                weightDiffStr = "You are within your ideal healthy weight range!";
            }
        }

        // Water Intake Calculation
        String waterGoalStr;
        if (isMetric) {
            double waterLiters = weightVal * 0.035;
            int glasses = (int) Math.round(waterLiters / 0.25);
            waterGoalStr = String.format(Locale.US, "%.2f Liters (~%d glasses / day)", waterLiters, glasses);
        } else {
            double waterOz = weightVal * 0.5;
            int glasses = (int) Math.round(waterOz / 8.0);
            waterGoalStr = String.format(Locale.US, "%.1f fl oz (~%d glasses / day)", waterOz, glasses);
        }

        // BMR Calculation (Mifflin-St Jeor)
        double weightKg = isMetric ? weightVal : (weightVal / 2.20462);
        double heightCm = isMetric ? heightVal : (heightVal * 2.54);
        boolean isMale = (genderToggleGroup.getCheckedButtonId() == R.id.btnMale);

        double bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * ageVal) + (isMale ? 5 : -161);
        String bmrStr = String.format(Locale.US, "~%,d kcal/day (Resting Energy)", Math.round(bmr));

        // 4. Update UI Display
        displayResultsAndInsights(bmi, category, colorRes, advice, targetRangeStr, weightDiffStr, waterGoalStr, bmrStr);

        // 5. Save Record
        String unitName = isMetric ? "Metric" : "Imperial";
        BmiRecord record = new BmiRecord(System.currentTimeMillis(), heightVal, weightVal, bmi, category, unitName);
        BmiHistoryManager.saveRecord(this, record);

        // Smoothly scroll down to show the results
        if (scrollView != null) {
            scrollView.post(() -> scrollView.smoothScrollTo(0, cardResult.getTop()));
        }

        Toast.makeText(this, getString(R.string.toast_calculated), Toast.LENGTH_SHORT).show();
    }

    private void displayResultsAndInsights(
        double bmi, String category, int colorRes, String advice,
        String targetRange, String weightDiff, String waterGoal, String bmrStr
    ) {
        tvBmiValue.setText(String.format(Locale.US, "%.2f", bmi));
        tvBmiCategory.setText(category);

        int categoryColor = ContextCompat.getColor(this, colorRes);

        GradientDrawable badgeBackground = new GradientDrawable();
        badgeBackground.setCornerRadius(48f);
        badgeBackground.setColor(categoryColor);
        tvBmiCategory.setBackground(badgeBackground);

        bmiProgressIndicator.setIndicatorColor(categoryColor);
        int targetProgress = (int) Math.min(Math.max(bmi, 0), 50);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(bmiProgressIndicator, "progress", 0, targetProgress);
        progressAnimator.setDuration(800);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();

        // Update Insights
        tvTargetWeightRange.setText(targetRange);
        tvWeightDiff.setText(weightDiff);
        tvWaterGoal.setText(waterGoal);
        tvBmrEstimate.setText(bmrStr);

        // Update Advice
        tvAdviceText.setText(advice);
        ivAdviceIcon.setColorFilter(categoryColor);
        cardAdvice.setStrokeColor(categoryColor);

        // Animate Cards Entry
        revealCard(cardResult, 0);
        revealCard(cardInsights, 100);
        revealCard(cardAdvice, 200);
    }

    private void revealCard(View card, long delay) {
        if (card.getVisibility() != View.VISIBLE) {
            card.setAlpha(0f);
            card.setVisibility(View.VISIBLE);
            card.animate().alpha(1f).setDuration(400).setStartDelay(delay).start();
        }
    }

    private void resetInputs() {
        if (isMetric) {
            updateSliderSafely(sliderHeight, 100f, 220f, 175f, 1.0f);
            updateSliderSafely(sliderWeight, 30f, 180f, 70f, 0.5f);
        } else {
            updateSliderSafely(sliderHeight, 40f, 90f, 68f, 1.0f);
            updateSliderSafely(sliderWeight, 70f, 400f, 154f, 0.5f);
        }
        syncInitialSliders();
        clearErrors();

        hideCard(cardResult);
        hideCard(cardInsights);
        hideCard(cardAdvice);

        Toast.makeText(this, getString(R.string.toast_reset), Toast.LENGTH_SHORT).show();
    }

    private void hideCard(View card) {
        if (card.getVisibility() == View.VISIBLE) {
            card.animate().alpha(0f).setDuration(250).withEndAction(() -> card.setVisibility(View.GONE)).start();
        }
    }

    private void clearErrors() {
        inputLayoutHeight.setError(null);
        inputLayoutWeight.setError(null);
    }

    private void animateButtonClick(View view) {
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
    }

    private void showHistoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bmi_history);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        RecyclerView rvHistory = dialog.findViewById(R.id.rvHistory);
        TextView tvEmptyHistory = dialog.findViewById(R.id.tvEmptyHistory);
        MaterialButton btnClearHistory = dialog.findViewById(R.id.btnClearHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        List<BmiRecord> records = BmiHistoryManager.getHistory(this);

        if (records.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            btnClearHistory.setEnabled(false);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            btnClearHistory.setEnabled(true);
        }

        BmiHistoryAdapter adapter = new BmiHistoryAdapter(this, records, position -> {
            if (position >= 0 && position < records.size()) {
                BmiHistoryManager.deleteRecord(this, position);
                records.remove(position);
                dialog.dismiss();
                showHistoryDialog();
            }
        });

        rvHistory.setAdapter(adapter);

        btnClearHistory.setOnClickListener(v -> {
            BmiHistoryManager.clearHistory(this);
            Toast.makeText(this, getString(R.string.toast_history_cleared), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
