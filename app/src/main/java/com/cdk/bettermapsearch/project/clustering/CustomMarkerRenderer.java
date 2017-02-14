package com.cdk.bettermapsearch.project.clustering;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.cdk.bettermapsearch.project.interfaces.SelectedItemCallback;
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

    private Context context;

    private IconGenerator iconGenerator;
    private IconGenerator clusterIconGenerator;
    private Cluster<T> previousCluster;
    private T previousClusterItem;

    private int colorNormal;
    private int colorActivated;

    private SelectedItemCallback<T> itemCallback;

    public CustomMarkerRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager, @ColorRes int colorNormal, @ColorRes int colorActivated) {
        super(context, map, clusterManager);
        this.context = context;
        this.colorNormal = colorNormal;
        this.colorActivated = colorActivated;

        // Application context is used in MapsUtils sample app
        iconGenerator = new IconGenerator(context.getApplicationContext());
        clusterIconGenerator = new IconGenerator(context.getApplicationContext());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        boolean clusterContainsSelectedItem = false;

        T selectedItem = itemCallback.getSelectedItem();
        if (selectedItem != null) {
            for (T clusterItem : cluster.getItems()) {
                if (clusterItem.equals(selectedItem)) {
                    clusterContainsSelectedItem = true;
                    break;
                }
            }
        }

        if (clusterContainsSelectedItem) {
            previousClusterItem = selectedItem;
            previousCluster = cluster;

            setupClusterView(cluster, colorActivated);
            clusterIconGenerator.setColor(ContextCompat.getColor(context, colorActivated));
        } else {
            setupClusterView(cluster, colorNormal);
            clusterIconGenerator.setColor(ContextCompat.getColor(context, colorNormal));
        }


        Bitmap icon = clusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
        if (itemCallback.getSelectedItem() != null && itemCallback.getSelectedItem().equals(item)) {
            previousClusterItem = item;
            setupClusterItemView(item, colorNormal);
            iconGenerator.setColor(ContextCompat.getColor(context, colorActivated));
        } else {
            setupClusterItemView(item, colorActivated);
            iconGenerator.setColor(ContextCompat.getColor(context, colorNormal));
        }

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        if (itemCallback.getSelectedItem() != null && itemCallback.getSelectedItem().equals(clusterItem)) {
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


    public LatLng getClusterMarker(Collection<Marker> markers, T item) {
        for (Marker m : markers) {
            Cluster<T> cluster = getCluster(m);
            for (T clusterItem : cluster.getItems()) {
                if (clusterItem.equals(item)) {
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
            if (clusterItem.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private void renderClusterAsSelected(Marker m, Cluster<T> cluster) {
        setupClusterView(cluster, colorNormal);

        clusterIconGenerator.setColor(ContextCompat.getColor(context, colorActivated));

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
            setupClusterItemView(item, colorNormal);

            iconGenerator.setColor(ContextCompat.getColor(context, colorActivated));

            Bitmap icon = iconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            marker.showInfoWindow();

            if (previousClusterItem != null && !previousClusterItem.equals(item)) {
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
            setupClusterView(previousCluster, colorActivated);

            clusterIconGenerator.setColor(ContextCompat.getColor(context, colorNormal));

            Bitmap icon = clusterIconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public void renderPreviousClusterItemAsUnselected() {
        Marker marker = getMarker(previousClusterItem);
        if (marker != null) {
            setupClusterItemView(previousClusterItem, colorActivated);

            iconGenerator.setColor(ContextCompat.getColor(context, colorNormal));

            Bitmap icon = iconGenerator.makeIcon();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    public void unselectAllItems() {
        renderPreviousClusterAsUnselected();
        renderPreviousClusterItemAsUnselected();
    }

    public void setColorActivated(int colorActivated) {
        this.colorActivated = colorActivated;
    }

    public void setColorNormal(int colorNormal) {
        this.colorNormal = colorNormal;
    }

    public void setItemCallback(SelectedItemCallback<T> itemCallback) {
        this.itemCallback = itemCallback;
    }

    // TODO: Might not need the colorRes argument
    public abstract void setupClusterView(Cluster<T> cluster, @ColorRes int colorRes);

    public abstract void setupClusterItemView(T item, @ColorRes int colorRes);
}
