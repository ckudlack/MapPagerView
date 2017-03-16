package com.cdk.bettermapsearch.models;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.android.gms.maps.model.LatLng;

/*
    Test model representing what you might get from the server
 */
public class LatLngModel implements MapClusterItem {

    private String title;
    private int price;
    private double latitude;
    private double longitude;
    private LatLng position;
    private int index;
    private boolean isViewed = false;

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void buildPositionFromLatAndLng() {
        position = new LatLng(latitude, longitude);
    }

    @Override
    public boolean isViewed() {
        return isViewed;
    }

    @Override
    public void setIsViewed() {
        isViewed = true;
    }
}
