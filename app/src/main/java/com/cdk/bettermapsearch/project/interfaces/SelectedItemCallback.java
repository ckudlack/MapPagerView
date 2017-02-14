package com.cdk.bettermapsearch.project.interfaces;

import com.cdk.bettermapsearch.project.clustering.MapClusterItem;

public interface SelectedItemCallback<T extends MapClusterItem> {
    T getSelectedItem();
}
