package com.example.tukuwededed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.Locale;

public class RacewayActivity extends AppCompatActivity implements LocationListener {
    private TextView tvSpeed, tvDistance, tvAccuracy, tvLog;
    private LocationManager locationManager;
    private boolean isTracking = false;
    private float totalDistance = 0, topSpeedSession = 0;
    private Location lastLocation = null;
    private long startTime = 0;
    private SharedPreferences prefs;
    
    // Checkpoint flags
    private boolean cp100 = false, cp200 = false, cp500 = false, cp1km = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raceway);
        
        prefs = getSharedPreferences("RaceLog", MODE_PRIVATE);
        tvSpeed = findViewById(R.id.tvSpeed); 
        tvDistance = findViewById(R.id.tvDistance);
        tvAccuracy = findViewById(R.id.tvAccuracy); 
        tvLog = findViewById(R.id.tvLog);

        findViewById(R.id.btnStart).setOnClickListener(v -> startRecording());
        findViewById(R.id.btnStop).setOnClickListener(v -> finalizeRun());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    private void startRecording() {
        if (isTracking) return;
        isTracking = true; 
        totalDistance = 0; 
        topSpeedSession = 0; 
        lastLocation = null;
        cp100 = cp200 = cp500 = cp1km = false;
        startTime = System.currentTimeMillis(); 
        tvLog.setText("> TRACKING STARTED...");
        tvDistance.setText("0 m");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        tvAccuracy.setText(String.format(Locale.getDefault(), "GPS Acc: %.1fm", location.getAccuracy()));
        float speedKmh = (float) (location.getSpeed() * 3.6);
        tvSpeed.setText(String.format(Locale.getDefault(), "%.0f", speedKmh));
        
        // AUTO RECORD FEATURE
        if (!isTracking && speedKmh > 5.0f) {
            startRecording();
            tvLog.append("\n[AUTO-RECORD ACTIVATED]");
        }

        if (speedKmh > topSpeedSession) topSpeedSession = speedKmh;
        
        if (isTracking) {
            if (lastLocation != null) {
                float distanceIncrement = location.distanceTo(lastLocation);
                totalDistance += distanceIncrement;
                if (totalDistance >= 1000) {
                    tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000.0));
                } else {
                    tvDistance.setText(String.format(Locale.getDefault(), "%.0f m", totalDistance));
                }
                checkCheckpoint(totalDistance);
            }
            lastLocation = location;
        }
    }

    private void checkCheckpoint(float dist) {
        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        if (dist >= 100 && !cp100) { cp100 = true; appendLog("100m", time); }
        else if (dist >= 200 && !cp200) { cp200 = true; appendLog("200m", time); }
        else if (dist >= 500 && !cp500) { cp500 = true; appendLog("500m", time); }
        else if (dist >= 1000 && !cp1km) { cp1km = true; appendLog("1km", time); }
    }

    private void appendLog(String label, double time) {
        String entry = String.format(Locale.getDefault(), "\n🚩 %s | %.2fs | Top: %.1f kmh", label, time, topSpeedSession);
        tvLog.append(entry);
    }

    private void finalizeRun() {
        if (!isTracking) return;
        isTracking = false;
        double fTime = (System.currentTimeMillis() - startTime) / 1000.0;
        String finalDistance = totalDistance >= 1000 ? 
                String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000.0) : 
                String.format(Locale.getDefault(), "%.1f m", totalDistance);

        String result = "\n\n--- 🏁 FINAL RESULT ---" +
                "\nJARAK : " + finalDistance +
                "\nWAKTU : " + String.format(Locale.getDefault(), "%.2f s", fTime) +
                "\nTOP   : " + String.format(Locale.getDefault(), "%.1f km/h", topSpeedSession);
        
        tvLog.append(result);

        String history = prefs.getString("history", "");
        String newEntry = String.format(Locale.getDefault(), "\n• Dist: %s | Time: %.2fs | Top: %.1f km/h", finalDistance, fTime, topSpeedSession);
        prefs.edit().putString("history", history + newEntry).apply();
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history balap.");
        new AlertDialog.Builder(this).setTitle("Race History").setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("CLEAR", (d, w) -> {
                    prefs.edit().remove("history").apply();
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override public void onProviderDisabled(@NonNull String p) {}
    @Override public void onProviderEnabled(@NonNull String p) {}
}