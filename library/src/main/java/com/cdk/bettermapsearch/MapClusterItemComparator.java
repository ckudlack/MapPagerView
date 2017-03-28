package com.cdk.bettermapsearch;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;

import java.util.Comparator;

public class MapClusterItemComparator implements Comparator<Cluster<? extends MapClusterItem>> {

    private Cluster<? extends MapClusterItem> currentLocation;

    public MapClusterItemComparator(Cluster<? extends MapClusterItem> currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(Cluster<? extends MapClusterItem> cluster1, Cluster<? extends MapClusterItem> cluster2) {
        final double distance1 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster1.getPosition());
        final double distance2 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster2.getPosition());

        return (int) (distance1 - distance2);
    }
}