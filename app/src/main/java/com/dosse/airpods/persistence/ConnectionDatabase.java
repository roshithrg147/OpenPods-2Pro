package com.dosse.airpods.persistence;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ConnectionEvent.class}, version = 1, exportSchema = false)
public abstract class ConnectionDatabase extends RoomDatabase {
    private static volatile ConnectionDatabase INSTANCE;

    public abstract ConnectionDao connectionDao();

    public static ConnectionDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ConnectionDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ConnectionDatabase.class, "connection_database")
                            .fallbackToDestructiveMigration() // Added for development safety
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
