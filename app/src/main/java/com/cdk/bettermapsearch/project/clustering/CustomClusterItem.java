package com.cdk.bettermapsearch.project.clustering;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public abstract class CustomClusterItem implements ClusterItem {

    protected final LatLng position;
    private final int index;

    public CustomClusterItem(LatLng position, int index) {
        this.position = position;
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
