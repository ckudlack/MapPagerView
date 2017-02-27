package com.cdk.bettermapsearch;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.cdk.bettermapsearch.clustering.MapClusterItem;
import com.cdk.bettermapsearch.interfaces.ViewCreatedCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public abstract class MapPagerAdapter<LT extends MapClusterItem, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<LT> backingList = new ArrayList<>();
    private SparseArray<ViewCreatedCallback> callbackMap = new SparseArray<>();

    public MapPagerAdapter() {
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return backingList.size();
    }

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

    public final void updateItems(List<LT> items) {
        this.backingList.clear();
        this.backingList.addAll(items);
    }
}
