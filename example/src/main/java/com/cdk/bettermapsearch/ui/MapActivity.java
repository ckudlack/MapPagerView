package com.cdk.bettermapsearch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cdk.bettermapsearch.example.R;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = new MapFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).commit();

    }
}
