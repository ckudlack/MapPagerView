package com.cdk.bettermapsearch.project.clustering;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CachedClusterManager<T extends ClusterItem> extends ClusterManager<T> {

    private List<T> items = new ArrayList<>();

    public CachedClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return super.onMarkerClick(marker);
    }

    @Override
    public void addItem(T myItem) {
        super.addItem(myItem);
        items.add(myItem);
    }

    @Override
    public void clearItems() {
        super.clearItems();
        items.clear();
    }

    @Override
    public void addItems(Collection<T> items) {
        super.addItems(items);
        this.items.clear();
        this.items.addAll(items);
    }

    public T getClusterItem(int position) {
        return items.get(position);
    }
}
