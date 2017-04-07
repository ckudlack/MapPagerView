package com.cdk.bettermapsearch.clustering

import android.content.Context
import com.cdk.bettermapsearch.interfaces.MapClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager

/**
 * Custom implementation of ClusterManager that adds a custom OnCameraIdleListener
 * This allows the user to do some custom task after the ClusterManager re-renders the markers
 */
class MapPagerClusterManager<T : MapClusterItem>(context: Context, map: GoogleMap, private var cameraIdleListener: GoogleMap.OnCameraIdleListener?) : ClusterManager<T>(context, map) {

    /**
     * The addition of showInfoWindow allows the selected marker to show on top of all others
     * This happens automatically when you click on a marker, but not when you scroll to it
     * via the carousel
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        marker.showInfoWindow()
        return super.onMarkerClick(marker)
    }

    /**
     * The ClusterManager needs to handle the IdleListener internally so it can
     * re-cluster the markers
     */
    override fun onCameraIdle() {
        super.onCameraIdle()
        cameraIdleListener?.onCameraIdle()
    }
}