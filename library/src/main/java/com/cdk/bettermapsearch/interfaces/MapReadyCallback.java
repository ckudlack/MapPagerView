package com.cdk.bettermapsearch.interfaces;

import com.cdk.bettermapsearch.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.clustering.CustomMarkerRenderer;
import com.cdk.bettermapsearch.clustering.MapClusterItem;
import com.google.android.gms.maps.GoogleMap;

public interface MapReadyCallback<T extends MapClusterItem> {
    CustomMarkerRenderer<T> onMapReady(GoogleMap googleMap, CachedClusterManager<T> clusterManager);
}
