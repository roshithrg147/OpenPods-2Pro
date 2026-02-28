package com.dosse.airpods.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.dosse.airpods.R;
import com.dosse.airpods.pods.Pod;
import com.dosse.airpods.pods.PodsService;
import com.dosse.airpods.pods.PodsStatus;
import com.dosse.airpods.pods.models.IPods;
import com.dosse.airpods.pods.models.RegularPods;

public class DashboardActivity extends AppCompatActivity {

    private TextView mTitle, mModelText;
    private TextView mLeftText, mRightText, mCaseText;
    private ImageView mLeftImg, mRightImg, mCaseImg;
    private ImageView mLeftInEar, mRightInEar;
    private ImageView mLeftCharge, mRightCharge, mCaseCharge;
    private Switch mSwitchAutoPause, mSwitchLowLatency;
    private SharedPreferences mPrefs;
    private boolean mIsStatusBound = false;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusHex = intent.getStringExtra(PodsService.EXTRA_HEX_STATUS);
            if (statusHex != null) {
                updateDashboard(new PodsStatus(statusHex));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mTitle = findViewById(R.id.dash_title);
        mModelText = findViewById(R.id.dash_model);

        mLeftText = findViewById(R.id.dash_left_text);
        mRightText = findViewById(R.id.dash_right_text);
        mCaseText = findViewById(R.id.dash_case_text);

        mLeftImg = findViewById(R.id.dash_left_img);
        mRightImg = findViewById(R.id.dash_right_img);
        mCaseImg = findViewById(R.id.dash_case_img);

        mLeftInEar = findViewById(R.id.dash_left_inear);
        mRightInEar = findViewById(R.id.dash_right_inear);

        mLeftCharge = findViewById(R.id.dash_left_charging);
        mRightCharge = findViewById(R.id.dash_right_charging);
        mCaseCharge = findViewById(R.id.dash_case_charging);

        mSwitchAutoPause = findViewById(R.id.dash_switch_autopause);
        mSwitchLowLatency = findViewById(R.id.dash_switch_lowlatency);

        mSwitchAutoPause.setChecked(mPrefs.getBoolean("autoPause", false));
        mSwitchLowLatency.setChecked(mPrefs.getBoolean("lowLatency", false));

        mSwitchAutoPause.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mPrefs.edit().putBoolean("autoPause", isChecked).apply());

        mSwitchLowLatency.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mPrefs.edit().putBoolean("lowLatency", isChecked).apply());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsStatusBound) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mStatusReceiver, new IntentFilter(PodsService.ACTION_STATUS_UPDATE));
            mIsStatusBound = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsStatusBound) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
            mIsStatusBound = false;
        }
    }

    private void updateDashboard(PodsStatus status) {
        if (status == null || status.isAllDisconnected() || status.getAirpods() == null) {
            mTitle.setText("No Device Connected");
            mModelText.setText("Please connect your earbuds to view details.");

            mLeftText.setText("--");
            mRightText.setText("--");
            mCaseText.setText("--");

            mLeftCharge.setVisibility(View.GONE);
            mRightCharge.setVisibility(View.GONE);
            mCaseCharge.setVisibility(View.GONE);

            mLeftInEar.setVisibility(View.INVISIBLE);
            mRightInEar.setVisibility(View.INVISIBLE);
            return;
        }

        IPods pods = status.getAirpods();
        mTitle.setText("Connected Device");
        mModelText.setText("Model: " + pods.getModel());

        if (pods instanceof RegularPods) {
            RegularPods rp = (RegularPods) pods;

            Pod left = rp.getPod(RegularPods.LEFT);
            Pod right = rp.getPod(RegularPods.RIGHT);
            Pod kase = rp.getPod(RegularPods.CASE);

            // Left
            mLeftImg.setImageResource((left.isConnected() || left.isCharging() || left.isInEar()) ? R.drawable.pod
                    : R.drawable.pod_disconnected);
            mLeftText.setText(left.isConnected() ? left.parseStatus() : "--");
            mLeftCharge.setVisibility(left.isCharging() ? View.VISIBLE : View.GONE);
            mLeftInEar.setVisibility(left.isInEar() ? View.VISIBLE : View.INVISIBLE);

            // Right
            mRightImg.setImageResource((right.isConnected() || right.isCharging() || right.isInEar()) ? R.drawable.pod
                    : R.drawable.pod_disconnected);
            mRightText.setText(right.isConnected() ? right.parseStatus() : "--");
            mRightCharge.setVisibility(right.isCharging() ? View.VISIBLE : View.GONE);
            mRightInEar.setVisibility(right.isInEar() ? View.VISIBLE : View.INVISIBLE);

            // Case
            mCaseImg.setImageResource(
                    (kase.isConnected() || kase.isCharging()) ? R.drawable.pod_case : R.drawable.pod_case_disconnected);
            mCaseText.setText(kase.isConnected() ? kase.parseStatus() : "--");
            mCaseCharge.setVisibility(kase.isCharging() && kase.isConnected() ? View.VISIBLE : View.GONE);
        } else {
            // For SinglePods like Airpods Max, Beats
            mLeftText.setText("--");
            mRightText.setText("--");
            mCaseText.setText("--");
        }
    }
}
