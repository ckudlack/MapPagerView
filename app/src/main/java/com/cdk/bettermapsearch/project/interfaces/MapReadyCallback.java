package com.cdk.bettermapsearch.project.interfaces;

import com.cdk.bettermapsearch.project.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.project.clustering.CustomClusterItem;
import com.cdk.bettermapsearch.project.clustering.CustomMarkerRenderer;
import com.google.android.gms.maps.GoogleMap;

public interface MapReadyCallback<T extends CustomClusterItem> {
    CustomMarkerRenderer<T> onMapReady(GoogleMap googleMap, CachedClusterManager<T> clusterManager);
}
