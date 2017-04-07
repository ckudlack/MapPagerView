package com.cdk.bettermapsearch.models;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.android.gms.maps.model.LatLng;

/*
    Test model representing what you might get from the server
 */
public class LatLngModel implements MapClusterItem {

    private String name;
    private int price;
    private double latitude;
    private double longitude;
    private LatLng position;
    private boolean isViewed = false;
    private boolean isSelected = false;

    @Override
    public LatLng getPosition() {
        if (position == null) {
            position = new LatLng(latitude, longitude);
        }
        return position;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public boolean isViewed() {
        return isViewed;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }

    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
