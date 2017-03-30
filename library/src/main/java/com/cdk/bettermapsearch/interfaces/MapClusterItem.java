package com.cdk.bettermapsearch.interfaces;

import com.google.maps.android.clustering.ClusterItem;

@SuppressWarnings("unused")
public interface MapClusterItem extends ClusterItem {

    /**
     * Call this method if you want the UI for a previously viewed item on the map to be
     * different than the selected or unselected states
     *
     * @return has the item been viewed previously
     */
    boolean isViewed();

    /**
     * Call this once an item has been viewed
     *
     * @param isViewed
     */
    void setIsViewed(boolean isViewed);

    boolean isSelected();

    void setIsSelected(boolean isSelected);
}
