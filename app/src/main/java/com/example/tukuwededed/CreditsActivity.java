package com.example.tukuwededed;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CreditsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }
}