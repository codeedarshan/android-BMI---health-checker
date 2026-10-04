package com.example;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying BMI calculation history items.
 */
public class BmiHistoryAdapter extends RecyclerView.Adapter<BmiHistoryAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final Context context;
    private final List<BmiRecord> records;
    private final OnDeleteClickListener deleteClickListener;

    public BmiHistoryAdapter(Context context, List<BmiRecord> records, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.records = records;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bmi_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BmiRecord record = records.get(position);

        holder.tvCategory.setText(record.getCategory());
        holder.tvDate.setText(record.getFormattedDate());
        holder.tvBmi.setText(String.format(Locale.getDefault(), "%.2f", record.getBmiValue()));

        String heightUnit = "Imperial".equals(record.getUnitType()) ? "in" : "cm";
        String weightUnit = "Imperial".equals(record.getUnitType()) ? "lbs" : "kg";
        String details = String.format(Locale.getDefault(), "H: %.1f %s | W: %.1f %s",
            record.getHeight(), heightUnit, record.getWeight(), weightUnit);
        holder.tvDetails.setText(details);

        // Color coding category indicator
        int colorRes = getCategoryColorRes(record.getCategory());
        holder.vCategoryIndicator.setBackgroundColor(ContextCompat.getColor(context, colorRes));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    deleteClickListener.onDeleteClick(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    private int getCategoryColorRes(String category) {
        if (category == null) return R.color.bmi_normal;
        if (category.toLowerCase().contains("underweight")) return R.color.bmi_underweight;
        if (category.toLowerCase().contains("normal")) return R.color.bmi_normal;
        if (category.toLowerCase().contains("overweight")) return R.color.bmi_overweight;
        if (category.toLowerCase().contains("obese")) return R.color.bmi_obese;
        return R.color.bmi_normal;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View vCategoryIndicator;
        final TextView tvCategory;
        final TextView tvDate;
        final TextView tvDetails;
        final TextView tvBmi;
        final ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vCategoryIndicator = itemView.findViewById(R.id.vCategoryIndicator);
            tvCategory = itemView.findViewById(R.id.tvHistoryCategory);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvDetails = itemView.findViewById(R.id.tvHistoryDetails);
            tvBmi = itemView.findViewById(R.id.tvHistoryBmi);
            btnDelete = itemView.findViewById(R.id.btnDeleteRecord);
        }
    }
}
