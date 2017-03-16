package com.cdk.bettermapsearch.clustering;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.cdk.bettermapsearch.interfaces.SelectedItemCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Collection;

public abstract class CustomMarkerRenderer<T extends MapClusterItem> extends DefaultClusterRenderer<T> {

    protected Context context;
    protected IconGenerator clusterItemIconGenerator;
    protected IconGenerator clusterIconGenerator;
    @Nullable protected Cluster<T> previousCluster;
    @Nullable protected T previousClusterItem;

    private boolean clusteringEnabled = true;
    private SelectedItemCallback<T> itemCallback;

    public CustomMarkerRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        // Application context is used in MapsUtils sample app
        clusterItemIconGenerator = new IconGenerator(context.getApplicationContext());
        clusterIconGenerator = new IconGenerator(context.getApplicationContext());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        boolean clusterContainsSelectedItem = false;

        T selectedItem = itemCallback.getSelectedItem();
        if (selectedItem != null) {
            for (T clusterItem : cluster.getItems()) {
                if (itemsAreEqual(clusterItem, selectedItem)) {
                    clusterContainsSelectedItem = true;
                    break;
                }
            }
        }

        if (clusterContainsSelectedItem) {
            previousClusterItem = selectedItem;
            previousCluster = cluster;

            setupClusterView(cluster, true);
            setClusterViewBackground(true);
        } else {
            setupClusterView(cluster, false);
            setClusterViewBackground(false);
        }


        Bitmap icon = clusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
        if (itemCallback.getSelectedItem() != null && itemsAreEqual(itemCallback.getSelectedItem(), item)) {
            previousClusterItem = item;
            setupClusterItemView(item, true);
            setClusterItemViewBackground(true);
        } else {
            setupClusterItemView(item, false);
            setClusterItemViewBackground(false);
        }

        Bitmap icon = clusterItemIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        if (itemCallback.getSelectedItem() != null && itemsAreEqual(itemCallback.getSelectedItem(), clusterItem)) {
            marker.showInfoWindow();
        }
    }

    @Override
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);

        if (previousCluster == cluster) {
            marker.showInfoWindow();
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return clusteringEnabled && super.shouldRenderAsCluster(cluster);
    }

    public void setClusteringEnabled(boolean clusteringEnabled) {
        this.clusteringEnabled = clusteringEnabled;
    }

    public LatLng getClusterMarker(Collection<Marker> markers, T item) {
        for (Marker m : markers) {
            Cluster<T> cluster = getCluster(m);
            for (T clusterItem : cluster.getItems()) {
                if (itemsAreEqual(clusterItem, item)) {
                    // we have a live one
                    if (previousCluster == null || !previousCluster.equals(cluster)) {
                        renderClusterAsSelected(m, cluster);
                    }
                    return m.getPosition();
                }
            }
        }
        return null;
    }

    public boolean clusterContainsItem(Cluster<T> cluster, T item) {
        for (T clusterItem : cluster.getItems()) {
            if (itemsAreEqual(clusterItem, item)) {
                return true;
            }
        }
        return false;
    }

    private void renderClusterAsSelected(Marker m, Cluster<T> cluster) {
        setupClusterView(cluster, true);
        setClusterViewBackground(true);

        Bitmap icon = clusterIconGenerator.makeIcon();
        m.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        m.showInfoWindow();

        if (previousCluster != null) {
            renderPreviousClusterAsUnselected();
        }
        if (previousClusterItem != null) {
            renderPreviousClusterItemAsUnselected();
            previousClusterItem = null;
        }
        previousCluster = cluster;
    }

    public boolean renderClusterItemAsSelected(T item) {
        Marker marker = getMarker(item);
        if (marker != null) {
            setupClusterItemView(item, true);
            setClusterItemViewBackground(true);

            Bitmap icon = clusterItemIconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            marker.showInfoWindow();

            if (previousClusterItem != null && !itemsAreEqual(previousClusterItem, item)) {
                renderPreviousClusterItemAsUnselected();
            }
            if (previousCluster != null) {
                renderPreviousClusterAsUnselected();
                previousCluster = null;
            }

            previousClusterItem = item;
            return true;
        } else {
            renderPreviousClusterItemAsUnselected();
            return false;
        }
    }

    public void renderPreviousClusterAsUnselected() {
        Marker marker = getMarker(previousCluster);
        if (marker != null) {
            setupClusterView(previousCluster, false);
            setClusterViewBackground(false);

            Bitmap icon = clusterIconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public void renderPreviousClusterItemAsUnselected() {
        Marker marker = getMarker(previousClusterItem);
        if (previousClusterItem != null) {
            previousClusterItem.setIsViewed();
        }
        if (marker != null) {
            setupClusterItemView(previousClusterItem, false);
            setClusterItemViewBackground(false);

            Bitmap icon = clusterItemIconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public void unselectAllItems() {
        renderPreviousClusterAsUnselected();
        renderPreviousClusterItemAsUnselected();
    }

    public void setItemCallback(SelectedItemCallback<T> itemCallback) {
        this.itemCallback = itemCallback;
    }

    private boolean itemsAreEqual(MapClusterItem item1, MapClusterItem item2) {
        if (item1 == null && item2 == null) {
            return true;
        }

        if (item1 == null || item2 == null) {
            return false;
        }

        return item1.getPosition().equals(item2.getPosition());
    }

    public abstract void setupClusterView(Cluster<T> cluster, boolean isSelected);

    public abstract void setupClusterItemView(T item, boolean isSelected);

    public abstract void setClusterViewBackground(boolean isSelected);

    public abstract void setClusterItemViewBackground(boolean isSelected);
}
