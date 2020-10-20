package com.cdk.bettermapsearch;


import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.cdk.bettermapsearch.interfaces.ViewCreatedCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the handling of the list that backs both the ViewPager and the marker clustering
 * It also handles the callbacks that are required for the ViewPager item translation animations
 *
 * @param <VH> The class type of your custom ViewHolder
 */
@SuppressWarnings("WeakerAccess")
public abstract class MapPagerAdapter<T extends MapClusterItem, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> backingList = new ArrayList<>();
    private SparseArray<ViewCreatedCallback> callbackMap = new SparseArray<>();

    public MapPagerAdapter() {
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @CallSuper
    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.itemView.setY(0);
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return backingList.size();
    }

    @CallSuper
    @Override
    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);

        final int position = holder.getAdapterPosition();
        holder.itemView.setVisibility(View.VISIBLE);

        final ViewCreatedCallback viewCreatedCallback = callbackMap.get(position);
        if (viewCreatedCallback != null) {
            viewCreatedCallback.viewCreated(position);
        }
    }

    public void setCallback(int position, ViewCreatedCallback callback) {
        callbackMap.put(position, callback);
    }

    public void clearCallbacks() {
        callbackMap.clear();
    }

    public LatLng getItemPositionOnMap(int index) {
        return backingList.get(index).getPosition();
    }

    public final void updateItems(List<T> items) {
        this.backingList.clear();
        this.backingList.addAll(items);
    }

    public int getPositionOfItem(T item) {
        return backingList.indexOf(item);
    }

    public T getItemAtPosition(int position) {
        //noinspection unchecked
        return backingList.get(position);
    }
}
