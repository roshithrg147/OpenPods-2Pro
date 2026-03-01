package com.dosse.airpods.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dosse.airpods.R;
import com.dosse.airpods.persistence.ConnectionDatabase;
import com.dosse.airpods.persistence.ConnectionEvent;
import com.dosse.airpods.receivers.StartupReceiver;
import com.dosse.airpods.utils.MIUIWarning;
import com.dosse.airpods.utils.PermissionUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ConnectionAdapter adapter;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ShimmerFrameLayout shimmerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if Bluetooth LE is available on this device. If not, show an error
        BluetoothAdapter btAdapter = ((BluetoothManager) Objects
                .requireNonNull(getSystemService(Context.BLUETOOTH_SERVICE))).getAdapter();
        if (btAdapter == null || (btAdapter.isEnabled() && btAdapter.getBluetoothLeScanner() == null)
                || (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            startActivity(new Intent(MainActivity.this, NoBTActivity.class));
            finish();

            return;
        }

        if (!PermissionUtils.checkAllPermissions(this)) {
            startActivity(new Intent(MainActivity.this, IntroActivity.class));
            finish();
        } else {
            StartupReceiver.startPodsService(getApplicationContext());
            // Warn MIUI users that their rom has known issues
            MIUIWarning.show(this);
        }

        Button btnDashboard = findViewById(R.id.btn_open_dashboard);
        btnDashboard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        });

        shimmerContainer = findViewById(R.id.shimmer_view_container);
        setupChips();
        setupHistory();
    }

    private void setupChips() {
        ChipGroup chipGroup = findViewById(R.id.supported_devices_chipgroup);
        String[] supportedDevices = getResources().getStringArray(R.array.supported_devices);

        for (String deviceName : supportedDevices) {
            Chip chip = new Chip(this);
            chip.setText(deviceName);
            chip.setClickable(false);
            chipGroup.addView(chip);
        }
    }

    private void setupHistory() {
        RecyclerView recyclerView = findViewById(R.id.connection_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConnectionAdapter();
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        // Start perceived latency proxy
        shimmerContainer.startShimmer();
        shimmerContainer.setVisibility(android.view.View.VISIBLE);

        executor.execute(() -> {
            long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            List<ConnectionEvent> events = ConnectionDatabase.getDatabase(this).connectionDao()
                    .getEventsSince(oneWeekAgo);

            long totalMinutes = 0;
            for (ConnectionEvent event : events) {
                totalMinutes += (event.duration / 60000);
            }

            final String summary = String.format(Locale.getDefault(), "Weekly Usage: %dh %dm", totalMinutes / 60,
                    totalMinutes % 60);

            runOnUiThread(() -> {
                adapter.setEvents(events);
                TextView summaryView = findViewById(R.id.usage_summary);
                summaryView.setText(summary);

                // Binding finished, kill proxy
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(android.view.View.GONE);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_ab_settings) {
            startActivity(new Intent(this, SettingsActivity.class)); // Settings icon clicked
            return true;
        }

        return false;
    }
}
