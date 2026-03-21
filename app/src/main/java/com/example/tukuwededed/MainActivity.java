package com.example.tukuwededed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnGoSpec).setOnClickListener(v -> startActivity(new Intent(this, SpecActivity.class)));
        findViewById(R.id.btnGoRaceway).setOnClickListener(v -> startActivity(new Intent(this, RacewayActivity.class)));
        findViewById(R.id.btnGoDyno).setOnClickListener(v -> startActivity(new Intent(this, DynoActivity.class)));
        findViewById(R.id.btnGoService).setOnClickListener(v -> startActivity(new Intent(this, ServiceActivity.class)));
        findViewById(R.id.btnGoFuel).setOnClickListener(v -> startActivity(new Intent(this, FuelActivity.class)));
        findViewById(R.id.btnGoJJS).setOnClickListener(v -> startActivity(new Intent(this, JjsActivity.class)));
        findViewById(R.id.btnGoCredits).setOnClickListener(v -> startActivity(new Intent(this, CreditsActivity.class)));
    }
}