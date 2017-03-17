package com.cdk.bettermapsearch.interfaces;

import com.cdk.bettermapsearch.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.clustering.CustomMarkerRenderer;
import com.google.android.gms.maps.GoogleMap;

/**
 * When the GoogleMap is ready, this will be called so that the activity / fragment can give the user's custom MarkerRenderer to the MapPagerView
 */
public interface MapReadyCallback<T extends MapClusterItem> {
    /**
     * @param googleMap      This is required for the MarkerRenderer
     * @param clusterManager Also required for the MarkerRenderer
     * @return the user's custom MarkerRenderer that extends CustomMarkerRenderer
     */
    CustomMarkerRenderer<T> onMapReady(GoogleMap googleMap, CachedClusterManager<T> clusterManager);
}
