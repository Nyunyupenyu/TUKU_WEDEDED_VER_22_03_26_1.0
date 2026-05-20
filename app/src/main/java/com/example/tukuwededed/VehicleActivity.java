package com.example.tukuwededed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class VehicleActivity extends AppCompatActivity {
    private ListView lvVehicles;
    private TextView tvEmpty;
    private List<JSONObject> vehicleList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<String> displayList = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        prefs = getSharedPreferences("VehiclePrefs", MODE_PRIVATE);
        lvVehicles = findViewById(R.id.lvVehicles);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        lvVehicles.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnBackNav).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddVehicle).setOnClickListener(v -> {
            Intent intent = new Intent(this, VehicleFormActivity.class);
            startActivity(intent);
        });

        lvVehicles.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, VehicleFormActivity.class);
            intent.putExtra("vehicle_index", position);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles();
    }

    private void loadVehicles() {
        vehicleList.clear();
        displayList.clear();
        String json = prefs.getString("vehicles", "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                vehicleList.add(obj);
                displayList.add(obj.optString("name", "Unknown") + " (" + obj.optString("plate", "-") + ")");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (vehicleList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}