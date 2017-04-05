package com.cdk.bettermapsearch.clustering;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.cdk.bettermapsearch.interfaces.MapClusterItem;
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

/**
 * This class handles the underlying logic of selecting and unselecting markers and clusters,
 * but leaves the creation of UI to the user
 */
public abstract class MapPagerMarkerRenderer<T extends MapClusterItem> extends DefaultClusterRenderer<T> {

    protected Context context;

    protected IconGenerator clusterItemIconGenerator;
    protected IconGenerator clusterIconGenerator;

    @Nullable private Cluster<T> previousCluster;
    @Nullable private T previousClusterItem;

    private boolean clusteringEnabled = true;

    public MapPagerMarkerRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        // Application context is used in MapsUtils sample app
        clusterItemIconGenerator = new IconGenerator(context.getApplicationContext());
        clusterIconGenerator = new IconGenerator(context.getApplicationContext());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        T selectedItem = getSelectedItemFromCluster(cluster);

        final boolean clusterContainsSelectedItem = selectedItem != null;

        if (clusterContainsSelectedItem) {
            previousClusterItem = selectedItem;
            previousCluster = cluster;
        }

        setupClusterView(cluster, clusterContainsSelectedItem);
        setClusterViewBackground(clusterContainsSelectedItem);

        Bitmap icon = clusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
        final boolean selected = item.isSelected();

        if (selected) {
            previousClusterItem = item;
        }

        setupClusterItemView(item, selected);
        setClusterItemViewBackground(selected);

        Bitmap icon = clusterItemIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
        if (clusterItem.isSelected()) {
            marker.showInfoWindow();
        }
    }

    @Override
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
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

    public LatLng getClusterMarkerPosition(Collection<Marker> markers, T item) {
        for (Marker m : markers) {
            Cluster<T> cluster = getCluster(m);
            if (clusterContainsItem(cluster, item)) {
                if (previousCluster == null || !previousCluster.equals(cluster)) {
                    renderClusterAsSelected(m, cluster);
                }
                return m.getPosition();
            }
        }
        return null;
    }

    public boolean clusterContainsItem(Cluster<T> cluster, @Nullable T item) {
        return item != null && cluster.getItems().contains(item);
    }

    private T getSelectedItemFromCluster(Cluster<T> cluster) {
        for (T clusterItem : cluster.getItems()) {
            if (clusterItem.isSelected()) {
                return clusterItem;
            }
        }
        return null;
    }

    private void renderClusterAsSelected(Marker m, Cluster<T> cluster) {
        setupClusterView(cluster, true);
        setClusterViewBackground(true);

        Bitmap icon = clusterIconGenerator.makeIcon();
        m.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        m.showInfoWindow();

        renderPreviousClusterAsUnselected();
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

            if (!itemsAreEqual(previousClusterItem, item)) {
                renderPreviousClusterItemAsUnselected();
            }

            renderPreviousClusterAsUnselected();
            previousCluster = null;

            previousClusterItem = item;
            return true;
        } else {
            renderPreviousClusterItemAsUnselected();
            return false;
        }
    }

    public void renderPreviousClusterAsUnselected() {
        if (previousCluster != null) {
            Marker marker = getMarker(previousCluster);
            if (marker != null) {
                setupClusterView(previousCluster, false);
                setClusterViewBackground(false);

                Bitmap icon = clusterIconGenerator.makeIcon();
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            }
        }
    }

    private void renderPreviousClusterItemAsUnselected() {
        Marker marker = getMarker(previousClusterItem);
        if (previousClusterItem != null) {
            previousClusterItem.setIsViewed(true);
            previousClusterItem.setIsSelected(false);
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

    private boolean itemsAreEqual(T item1, T item2) {
        if (item1 == null && item2 == null) {
            return true;
        }

        if (item1 == null || item2 == null) {
            return false;
        }

        return item1.getPosition().equals(item2.getPosition());
    }

    /**
     * This method sets the content of the marker for a cluster
     *
     * @param cluster    the cluster being rendered
     * @param isSelected lets the user know the state of the marker so the UI can be updated in
     *                   whatever way they want
     */
    protected abstract void setupClusterView(Cluster<T> cluster, boolean isSelected);

    /**
     * This method set the content of the marker for a singular item
     *
     * @param item       the marker item (singular, not in a cluster) being rendered
     * @param isSelected lets the user know the state of the marker so the UI can be updated in
     *                   whatever way they want
     */
    protected abstract void setupClusterItemView(T item, boolean isSelected);

    /**
     * This sets the background of the marker for the cluster
     */
    protected abstract void setClusterViewBackground(boolean isSelected);

    /**
     * This sets the background of the marker for a singular marker item
     */
    protected abstract void setClusterItemViewBackground(boolean isSelected);
}
