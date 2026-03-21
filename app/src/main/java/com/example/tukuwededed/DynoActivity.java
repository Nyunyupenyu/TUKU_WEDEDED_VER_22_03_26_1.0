package com.example.tukuwededed;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.*;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DynoActivity extends AppCompatActivity implements SensorEventListener, LocationListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private TextView tvLiveG, tvLiveKmh, tvMaxPower, tvGpsStatus;
    private DynoView dynoGraph;
    
    private boolean isRunning = false;
    private List<Float> powerData = new ArrayList<>();
    private float maxPowerKw = 0;
    private float currentSpeedKmh = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dyno);

        prefs = getSharedPreferences("DynoLog", MODE_PRIVATE);
        tvLiveG = findViewById(R.id.tvLiveG);
        tvLiveKmh = findViewById(R.id.tvLiveKmh);
        tvMaxPower = findViewById(R.id.tvMaxPower);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        dynoGraph = findViewById(R.id.dynoGraph);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        findViewById(R.id.btnStartDyno).setOnClickListener(v -> startDyno());
        findViewById(R.id.btnStopDyno).setOnClickListener(v -> stopDyno());
        findViewById(R.id.btnSaveDyno).setOnClickListener(v -> saveDynoData());
        findViewById(R.id.btnHistoryDyno).setOnClickListener(v -> showHistory());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        }
    }

    private void startDyno() {
        isRunning = true;
        powerData.clear();
        maxPowerKw = 0;
        tvMaxPower.setText("MAX ESTIMATED POWER: 0 KW / 0 HP");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        Toast.makeText(this, "DYNO RUN STARTED!", Toast.LENGTH_SHORT).show();
    }

    private void stopDyno() {
        isRunning = false;
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "DYNO RUN STOPPED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRunning) return;
        
        float accelY = event.values[1]; 
        float gForce = accelY / 9.81f;
        tvLiveG.setText(String.format(Locale.getDefault(), "G-Force: %.2f", gForce));

        float estimatedPowerKw = (float) (200 * (accelY) * (currentSpeedKmh / 3.6) / 1000.0);
        if (estimatedPowerKw < 0) estimatedPowerKw = 0;
        
        powerData.add(estimatedPowerKw);
        if (estimatedPowerKw > maxPowerKw) {
            maxPowerKw = estimatedPowerKw;
            float maxPowerHp = maxPowerKw * 1.34102f;
            tvMaxPower.setText(String.format(Locale.getDefault(), "MAX ESTIMATED POWER: %.1f KW / %.1f HP", maxPowerKw, maxPowerHp));
        }
        
        dynoGraph.setData(new ArrayList<>(powerData));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentSpeedKmh = (float) (location.getSpeed() * 3.6);
        tvLiveKmh.setText(String.format(Locale.getDefault(), "%.0f KM/H", currentSpeedKmh));
        tvGpsStatus.setText(String.format(Locale.getDefault(), "GPS Active (Acc: %.1fm)", location.getAccuracy()));
    }

    private void saveDynoData() {
        String history = prefs.getString("history", "");
        float maxPowerHp = maxPowerKw * 1.34102f;
        String entry = String.format(Locale.getDefault(), "\n• Dyno Run: %.1f KW / %.1f HP | Top Speed: %.0f KM/H", maxPowerKw, maxPowerHp, currentSpeedKmh);
        prefs.edit().putString("history", history + entry).apply();
        Toast.makeText(this, "Dyno Data Saved!", Toast.LENGTH_SHORT).show();
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history dyno.");
        new AlertDialog.Builder(this).setTitle("Dyno History").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history").apply())
                .show();
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override public void onProviderDisabled(@NonNull String p) { tvGpsStatus.setText("GPS Disabled"); }
    @Override public void onProviderEnabled(@NonNull String p) { tvGpsStatus.setText("GPS Enabled"); }
}