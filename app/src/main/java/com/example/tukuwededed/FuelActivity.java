package com.example.tukuwededed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class FuelActivity extends AppCompatActivity {
    private EditText etDistance, etFuelAmount;
    private TextView tvResult;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);

        prefs = getSharedPreferences("FuelLog", MODE_PRIVATE);
        etDistance = findViewById(R.id.etDistance);
        etFuelAmount = findViewById(R.id.etFuelAmount);
        tvResult = findViewById(R.id.tvResult);

        findViewById(R.id.btnSave).setOnClickListener(v -> saveFuelLog());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void saveFuelLog() {
        try {
            double dist = Double.parseDouble(etDistance.getText().toString());
            double fuel = Double.parseDouble(etFuelAmount.getText().toString());
            double res = dist / fuel;
            
            tvResult.setText(String.format(Locale.getDefault(), "%.1f Km/L", res));

            String history = prefs.getString("history", "");
            String newEntry = String.format(Locale.getDefault(), "\n• %.1f km / %.1f L = %.1f Km/L", dist, fuel, res);
            prefs.edit().putString("history", history + newEntry).apply();
            
            Toast.makeText(this, "Log Saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Masukkan data yang valid!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        String history = prefs.getString("history", "Belum ada history.");
        new AlertDialog.Builder(this)
                .setTitle("History Konsumsi BBM")
                .setMessage(history)
                .setPositiveButton("OK", null)
                .setNeutralButton("HAPUS", (dialog, which) -> {
                    prefs.edit().remove("history").apply();
                    Toast.makeText(this, "History dihapus", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}