package com.cdk.bettermapsearch.project.interfaces;

import com.cdk.bettermapsearch.project.clustering.CustomClusterItem;

public interface SelectedItemCallback<T extends CustomClusterItem> {
    T getSelectedItem();
}
