package com.dosse.airpods.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ConnectionDao {
    @Insert
    void insert(ConnectionEvent event);

    @Query("SELECT * FROM connection_events ORDER BY startTime DESC")
    List<ConnectionEvent> getAllEvents();

    @Query("SELECT * FROM connection_events WHERE startTime >= :since ORDER BY startTime DESC")
    List<ConnectionEvent> getEventsSince(long since);
}
