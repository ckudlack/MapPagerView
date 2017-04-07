package com.cdk.bettermapsearch.interfaces

import com.google.maps.android.clustering.ClusterItem

interface MapClusterItem : ClusterItem {

    /**
     * Call this method if you want the UI for a previously viewed item on the map to be
     * different than the selected or unselected states

     * @return has the item been viewed previously
     */
    var isViewed: Boolean

    var isSelected: Boolean
}
