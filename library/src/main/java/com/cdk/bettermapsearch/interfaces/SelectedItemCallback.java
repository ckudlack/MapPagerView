package com.cdk.bettermapsearch.interfaces;

import com.cdk.bettermapsearch.clustering.MapClusterItem;

public interface SelectedItemCallback<T extends MapClusterItem> {
    T getSelectedItem();
}
