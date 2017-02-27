package com.cdk.bettermapsearch.clustering;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public abstract class MapClusterItem implements ClusterItem {

    private double latitude;
    private double longitude;
    protected LatLng position;
    private int index;

    public void initialize(int index) {
        position = new LatLng(latitude, longitude);
        this.index = index;
    }

    @Override
    public LatLng getPosition() {
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

    public int getIndex() {
        return index;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
