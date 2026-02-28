package com.dosse.airpods.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dosse.airpods.R;
import com.dosse.airpods.persistence.ConnectionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ViewHolder> {
    private List<ConnectionEvent> events = new ArrayList<>();

    public void setEvents(List<ConnectionEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConnectionEvent event = events.get(position);
        holder.deviceModel.setText(event.deviceModel);
        
        Date date = new Date(event.startTime);
        holder.sessionTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date));
        
        long minutes = event.duration / 60000;
        holder.duration.setText(String.format(Locale.getDefault(), "%d min", minutes));

        // Use alert icon if average battery was low (e.g. <= 2/10 which is 20%)
        if (event.averageBattery <= 2) {
            holder.batteryIcon.setImageResource(R.drawable.ic_battery_alert_red_24dp);
            holder.batteryIcon.setVisibility(View.VISIBLE);
        } else {
            holder.batteryIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceModel, sessionTime, duration;
        ImageView batteryIcon;

        ViewHolder(View itemView) {
            super(itemView);
            deviceModel = itemView.findViewById(R.id.device_model);
            sessionTime = itemView.findViewById(R.id.session_time);
            duration = itemView.findViewById(R.id.duration);
            batteryIcon = itemView.findViewById(R.id.battery_icon);
        }
    }
}
