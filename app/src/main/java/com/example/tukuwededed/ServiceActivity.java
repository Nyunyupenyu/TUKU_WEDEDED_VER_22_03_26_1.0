package com.example.tukuwededed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ServiceActivity extends AppCompatActivity {
    private EditText etLastOli, etDateOli, etLastBusi, etLastFilter, etLastCleaner;
    private TextView tvEstimasiOli, tvEstimasiBusi, tvEstimasiFilter, tvEstimasiCleaner;
    private SharedPreferences prefs;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        prefs = getSharedPreferences("ServicePrefs", MODE_PRIVATE);
        initViews();
        loadHistory();

        TextWatcher serviceWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                updateEstimasi();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etLastOli.addTextChangedListener(serviceWatcher);
        etDateOli.addTextChangedListener(serviceWatcher);
        etLastBusi.addTextChangedListener(serviceWatcher);
        etLastFilter.addTextChangedListener(serviceWatcher);
        etLastCleaner.addTextChangedListener(serviceWatcher);
        
        findViewById(R.id.btnSave).setOnClickListener(v -> saveToHistory());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etLastOli = findViewById(R.id.etLastOli);
        etDateOli = findViewById(R.id.etDateOli);
        etLastBusi = findViewById(R.id.etLastBusi);
        etLastFilter = findViewById(R.id.etLastFilter);
        etLastCleaner = findViewById(R.id.etLastCleaner);
        tvEstimasiOli = findViewById(R.id.tvEstimasiOli);
        tvEstimasiBusi = findViewById(R.id.tvEstimasiBusi);
        tvEstimasiFilter = findViewById(R.id.tvEstimasiFilter);
        tvEstimasiCleaner = findViewById(R.id.tvEstimasiCleaner);
    }

    private void updateEstimasi() {
        // Estimasi Oli (KM)
        try {
            int km = Integer.parseInt(etLastOli.getText().toString());
            tvEstimasiOli.setText("Estimasi Ganti: KM " + (km + 3000));
        } catch (Exception e) {
            tvEstimasiOli.setText("Estimasi Ganti: +3000km");
        }

        // Estimasi Tanggal (Oli, Busi, Filter, Cleaner)
        tvEstimasiOli.append(" atau " + getFutureDate(etDateOli.getText().toString(), 3));
        tvEstimasiBusi.setText("Estimasi Ganti Busi: " + getFutureDate(etLastBusi.getText().toString(), 6));
        tvEstimasiFilter.setText("Estimasi Ganti Filter: " + getFutureDate(etLastFilter.getText().toString(), 6));
        tvEstimasiCleaner.setText("Estimasi Cleaner: " + getFutureDate(etLastCleaner.getText().toString(), 6));
    }

    private String getFutureDate(String inputDate, int months) {
        try {
            Date date = sdf.parse(inputDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MONTH, months);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return "+" + months + " Bulan";
        }
    }

    private void saveToHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            JSONObject entry = new JSONObject();
            entry.put("last_oli", etLastOli.getText().toString());
            entry.put("date_oli", etDateOli.getText().toString());
            entry.put("last_busi", etLastBusi.getText().toString());
            entry.put("last_filter", etLastFilter.getText().toString());
            entry.put("last_cleaner", etLastCleaner.getText().toString());
            entry.put("timestamp", new java.util.Date().toString());

            historyArray.put(entry);
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_oli", etLastOli.getText().toString());
            editor.putString("date_oli", etDateOli.getText().toString());
            editor.putString("last_busi", etLastBusi.getText().toString());
            editor.putString("last_filter", etLastFilter.getText().toString());
            editor.putString("last_cleaner", etLastCleaner.getText().toString());
            editor.putString("history_json", historyArray.toString());
            editor.apply();
            
            Toast.makeText(this, "Service Log Saved to History!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving history", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        try {
            JSONArray historyArray = new JSONArray(prefs.getString("history_json", "[]"));
            if (historyArray.length() == 0) {
                Toast.makeText(this, "Belum ada history service.", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] items = new String[historyArray.length()];
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject obj = historyArray.getJSONObject(i);
                items[i] = "Service Tgl: " + obj.optString("date_oli", "-") + " | Oli: " + obj.optString("last_oli", "0") + "km";
            }

            new AlertDialog.Builder(this)
                    .setTitle("History Service (Klik untuk load)")
                    .setItems(items, (dialog, which) -> {
                        try {
                            loadFromJSONObject(historyArray.getJSONObject(which));
                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading history item", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("OK", null)
                    .setNeutralButton("CLEAR", (d, w) -> prefs.edit().remove("history_json").apply())
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromJSONObject(JSONObject obj) {
        etLastOli.setText(obj.optString("last_oli", ""));
        etDateOli.setText(obj.optString("date_oli", ""));
        etLastBusi.setText(obj.optString("last_busi", ""));
        etLastFilter.setText(obj.optString("last_filter", ""));
        etLastCleaner.setText(obj.optString("last_cleaner", ""));
        updateEstimasi();
        Toast.makeText(this, "Data Service Loaded!", Toast.LENGTH_SHORT).show();
    }

    private void loadHistory() {
        etLastOli.setText(prefs.getString("last_oli", ""));
        etDateOli.setText(prefs.getString("date_oli", ""));
        etLastBusi.setText(prefs.getString("last_busi", ""));
        etLastFilter.setText(prefs.getString("last_filter", ""));
        etLastCleaner.setText(prefs.getString("last_cleaner", ""));
        updateEstimasi();
    }
}