package com.cdk.bettermapsearch;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;

import java.util.Comparator;

public class MapClusterItemComparator implements Comparator<Cluster<MapClusterItem>> {

    private Cluster<MapClusterItem> currentLocation;

    public MapClusterItemComparator(Cluster<MapClusterItem> currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(Cluster<MapClusterItem> cluster1, Cluster<MapClusterItem> cluster2) {
        final double distance1 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster1.getPosition());
        final double distance2 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster2.getPosition());

        return (int) (distance1 - distance2);
    }
}