package com.cdk.bettermapsearch;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;

import java.util.Comparator;

@SuppressWarnings("WeakerAccess")
public class MapClusterItemComparator<T extends MapClusterItem> implements Comparator<Cluster<T>> {

    private Cluster<T> currentLocation;

    public MapClusterItemComparator(Cluster<T> currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public int compare(Cluster<T> cluster1, Cluster<T> cluster2) {
        final double distance1 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster1.getPosition());
        final double distance2 = SphericalUtil.computeDistanceBetween(currentLocation.getPosition(), cluster2.getPosition());

        return (int) (distance1 - distance2);
    }
}