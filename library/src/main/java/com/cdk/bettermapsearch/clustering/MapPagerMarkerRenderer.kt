package com.cdk.bettermapsearch.clustering

import android.content.Context
import com.cdk.bettermapsearch.interfaces.MapClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator



/**
 * This class handles the underlying logic of selecting and unselecting markers and clusters,
 * but leaves the creation of UI to the user
 */
abstract class MapPagerMarkerRenderer<T : MapClusterItem>(protected var context: Context, map: GoogleMap?, clusterManager: ClusterManager<T>?) : DefaultClusterRenderer<T>(context, map, clusterManager) {

    protected var clusterItemIconGenerator: IconGenerator = IconGenerator(context.applicationContext)
    protected var clusterIconGenerator: IconGenerator = IconGenerator(context.applicationContext)

    private var previousCluster: Cluster<T>? = null
    private var previousClusterItem: T? = null

    private var clusteringEnabled = true

    override fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        val selectedItemFromCluster = getSelectedItemFromCluster(cluster)

        val clusterContainsSelectedItem = selectedItemFromCluster != null

        if (clusterContainsSelectedItem) {
            previousClusterItem = selectedItemFromCluster
            previousCluster = cluster
        }

        setupClusterView(cluster, clusterContainsSelectedItem)
        setClusterViewBackground(clusterContainsSelectedItem)

        val icon = clusterIconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions) {
        val selected = item.isSelected

        if (selected) {
            previousClusterItem = item
        }

        setupClusterItemView(item, selected)
        setClusterItemViewBackground(selected)

        val icon = clusterItemIconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterItemRendered(clusterItem: T, marker: Marker) {
        if (clusterItem.isSelected) {
            marker.showInfoWindow()
        }
    }

    override fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {
        if (previousCluster == cluster) {
            marker.showInfoWindow()
        }
    }

    override fun shouldRenderAsCluster(cluster: Cluster<T>): Boolean {
        return clusteringEnabled && super.shouldRenderAsCluster(cluster)
    }

    // we no longer need this when MapPagerView is converted to Kotlin
    fun setClusteringEnabled(clusteringEnabled: Boolean) {
        this.clusteringEnabled = clusteringEnabled
    }

    fun getClusterMarkerPosition(markers: Collection<Marker>, item: T?): LatLng? {
        for (m in markers) {
            val cluster = getCluster(m)
            if (cluster.items.contains(item)) {
                if (previousCluster == null || previousCluster != cluster) {
                    renderClusterAsSelected(m, cluster)
                }
                return m.position
            }
        }
        return null
    }

    fun getSelectedItemFromCluster(cluster: Cluster<T>): T? {
        return cluster.items.firstOrNull { it.isSelected }
    }

    fun renderClusterAsSelected(marker: Marker, cluster: Cluster<T>) {
        setupClusterView(cluster, true)
        setClusterViewBackground(true)

        val icon = clusterIconGenerator.makeIcon()
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
        marker.showInfoWindow()

        renderPreviousClusterAsUnselected()

        if (previousClusterItem != null) {
            renderPreviousClusterItemAsUnselected()
            previousClusterItem = null
        }

        previousCluster = cluster
    }

    fun renderClusterItemAsSelected(item: T): Boolean {
        val marker: Marker? = getMarker(item)

        if (marker != null) {
            setupClusterItemView(item, true)
            setClusterItemViewBackground(true)

            val icon = clusterItemIconGenerator.makeIcon()
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            marker.showInfoWindow()

            if (!itemsAreEqual(previousClusterItem, item)) {
                renderPreviousClusterItemAsUnselected()
            }

            renderPreviousClusterAsUnselected()
            previousCluster = null

            previousClusterItem = item
            return true
        } else {
            renderPreviousClusterItemAsUnselected()
            return false
        }
    }

    fun renderPreviousClusterAsUnselected() {
        if (previousCluster != null) {
            val marker: Marker? = getMarker(previousCluster)

            if (marker != null) {
                setupClusterView(previousCluster, false)
                setClusterViewBackground(false)

                val icon = clusterIconGenerator.makeIcon()
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
            }
        }
    }

    fun renderPreviousClusterItemAsUnselected() {
        val marker: Marker? = getMarker(previousClusterItem)

        if (previousClusterItem != null) {
            (previousClusterItem as T).isViewed = true
            (previousClusterItem as T).isSelected = false
        }

        if (marker != null) {
            setupClusterItemView(previousClusterItem, false)
            setClusterItemViewBackground(false)

            val icon = clusterItemIconGenerator.makeIcon()
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
        }
    }

    fun unselectAllItems() {
        renderPreviousClusterAsUnselected()
        renderPreviousClusterItemAsUnselected()
    }

    fun itemsAreEqual(item1: T?, item2: T?): Boolean {
        return item1?.position == item2?.position
    }

    /**
     * This method sets the content of the marker for a cluster
     *
     * @param cluster    the cluster being rendered
     * @param isSelected lets the user know the state of the marker so the UI can be updated in
     *                   whatever way they want
     */
    protected abstract fun setupClusterView(cluster: Cluster<T>?, isSelected: Boolean)

    /**
     * This method set the content of the marker for a singular item
     *
     * @param item       the marker item (singular, not in a cluster) being rendered
     * @param isSelected lets the user know the state of the marker so the UI can be updated in
     *                   whatever way they want
     */
    protected abstract fun setupClusterItemView(item: T?, isSelected: Boolean)

    /**
     * This sets the background of the marker for the cluster
     */
    protected abstract fun setClusterViewBackground(isSelected: Boolean)

    /**
     * This sets the background of the marker for a singular marker item
     */
    protected abstract fun setClusterItemViewBackground(isSelected: Boolean)
}