package com.cdk.bettermapsearch

import com.cdk.bettermapsearch.interfaces.MapClusterItem
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.Cluster
import java.util.*

class MapClusterItemComparator<T : MapClusterItem>(private val currentLocation: Cluster<T>) : Comparator<Cluster<T>> {

    override fun compare(cluster1: Cluster<T>, cluster2: Cluster<T>): Int {
        val distance1 = SphericalUtil.computeDistanceBetween(currentLocation.position, cluster1.position)
        val distance2 = SphericalUtil.computeDistanceBetween(currentLocation.position, cluster2.position)

        return (distance1 - distance2).toInt()
    }
}