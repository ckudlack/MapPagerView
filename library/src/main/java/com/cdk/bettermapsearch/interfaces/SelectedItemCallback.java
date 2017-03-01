package com.cdk.bettermapsearch.interfaces;

import android.support.annotation.Nullable;

public interface SelectedItemCallback<T extends MapClusterItem> {
    @Nullable T getSelectedItem();
}
