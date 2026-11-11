package com.example.ass2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.Holder> {
    public interface OnItemClickListener {
        void onItemClick(DBHelper.ActivityRecord item);
    }

    private final List<DBHelper.ActivityRecord> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ActivityAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitItems(List<DBHelper.ActivityRecord> history) {
        items.clear();
        items.addAll(history);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DBHelper.ActivityRecord item = items.get(position);
        holder.date.setText(item.getTimestamp());
        holder.summary.setText(holder.itemView.getContext().getString(R.string.history_summary,
                item.getSteps(), item.getShakes(), item.getDurationFormatted(), item.getStepPace()));
        holder.stepRecord.setVisibility(item.isNewStepRecord() ? View.VISIBLE : View.GONE);
        holder.shakeRecord.setVisibility(item.isNewShakeRecord() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView date, summary, stepRecord, shakeRecord;

        Holder(View view) {
            super(view);
            date = view.findViewById(R.id.history_date);
            summary = view.findViewById(R.id.history_summary);
            stepRecord = view.findViewById(R.id.step_record);
            shakeRecord = view.findViewById(R.id.shake_record);
        }
    }
}
