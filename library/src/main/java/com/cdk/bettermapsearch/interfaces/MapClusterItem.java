package com.cdk.bettermapsearch.interfaces;

import com.google.maps.android.clustering.ClusterItem;

public interface MapClusterItem extends ClusterItem {

    /**
     * @return index of the item in the list
     */
    int getIndex();

    /**
     * @param index index of the item in the list
     */
    void setIndex(int index);

    /**
     * This will create a LatLng object from latitude and longitude
     */
    void buildPositionFromLatAndLng();

    /**
     * Call this method if you want the UI for a previously viewed item on the map to be
     * different than the selected or unselected states
     *
     * @return has the item been viewed previously
     */
    boolean isViewed();

    /**
     * Call this once an item has been viewed
     */
    void setIsViewed();
}
