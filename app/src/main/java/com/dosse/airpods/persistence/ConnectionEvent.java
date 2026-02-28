package com.dosse.airpods.persistence;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "connection_events")
public class ConnectionEvent {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String deviceModel;
    public long startTime;
    public long duration;
    public int averageBattery;

    public ConnectionEvent(String deviceModel, long startTime, long duration, int averageBattery) {
        this.deviceModel = deviceModel;
        this.startTime = startTime;
        this.duration = duration;
        this.averageBattery = averageBattery;
    }
}
