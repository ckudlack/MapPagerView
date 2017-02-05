package com.cdk.bettermapsearch.example.models;

import com.cdk.bettermapsearch.project.interfaces.ViewPagerItem;
import com.google.android.gms.maps.model.LatLng;

public class LatLngModel implements ViewPagerItem {

    private double latitude;
    private double longitude;
    private LatLng position;
    private int index;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public LatLng getPosition() {
        if (position == null) {
            position = new LatLng(latitude, longitude);
        }
        return position;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
