package com.cdk.bettermapsearch.clustering;

import android.content.Context;
import android.support.annotation.Nullable;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;


/**
 * Custom implementation of ClusterManager that adds a custom OnCameraIdleListener
 * This allows the user to do some custom task after the ClusterManager re-renders the markers
 */
public class CachedClusterManager<T extends MapClusterItem> extends ClusterManager<T> {

    private @Nullable GoogleMap.OnCameraIdleListener cameraIdleListener;

    public CachedClusterManager(Context context, GoogleMap map, @Nullable GoogleMap.OnCameraIdleListener cameraIdleListener) {
        super(context, map);
        this.cameraIdleListener = cameraIdleListener;
    }

    /**
     * The addition of showInfoWindow allows the selected marker to show on top of all others
     * This happens automatically when you click on a marker, but not when you scroll to it
     * via the carousel
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return super.onMarkerClick(marker);
    }

    /**
     * The ClusterManager needs to handle the IdleListener internally so it can
     * re-cluster the markers
     */
    @Override
    public void onCameraIdle() {
        super.onCameraIdle();
        if (cameraIdleListener != null) {
            cameraIdleListener.onCameraIdle();
        }
    }
}
