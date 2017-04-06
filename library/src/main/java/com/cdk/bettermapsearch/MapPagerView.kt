package com.cdk.bettermapsearch

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.AttrRes
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import com.cdk.bettermapsearch.clustering.MapPagerClusterManager
import com.cdk.bettermapsearch.clustering.MapPagerMarkerRenderer
import com.cdk.bettermapsearch.interfaces.MapClusterItem
import com.cdk.bettermapsearch.interfaces.MapReadyCallback
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.Algorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager
import kotlinx.android.synthetic.main.map_pager.view.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.combineLatest
import rx.lang.kotlin.subscribeBy
import java.util.*

@Suppress("unused")
class MapPagerView<T : MapClusterItem> : FrameLayout, OnMapReadyCallback, GoogleMap.OnMapClickListener, RecyclerViewPager.OnPageChangedListener, ClusterManager.OnClusterClickListener<T>, ClusterManager.OnClusterInfoWindowClickListener<T>, ClusterManager.OnClusterItemClickListener<T>, ClusterManager.OnClusterItemInfoWindowClickListener<T> {

    private val DEFAULT_VIEW_PAGER_HEIGHT_PERCENT = 0.25
    private val DEFAULT_MAP_CAMERA_ANIMATION_SPEED = 200
    private val DEFAULT_CLUSTER_SIZE = 4

    private var algorithm: Algorithm<T> = NonHierarchicalDistanceBasedAlgorithm<T>()
    private var googleMap: GoogleMap? = null

    private var clusterManager: MapPagerClusterManager<T>? = null
    private var markerRenderer: MapPagerMarkerRenderer<T>? = null
    private var pagerAdapter: MapPagerAdapter<T, *>? = null
    private var mapReadyCallback: MapReadyCallback<T>? = null

    private var viewSubscriber: Subscription? = null

    private var currentlySelectedItem: T? = null

    private var minClusterSize = DEFAULT_CLUSTER_SIZE
    private var mapCameraAnimationSpeed = DEFAULT_MAP_CAMERA_ANIMATION_SPEED
    private var fromClusterItemClick = false
    private var clusteringEnabled = true

    private var customMapClickListener: GoogleMap.OnMapClickListener? = null
    private var customInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener? = null
    private var customInfoWindowAdapter: GoogleMap.InfoWindowAdapter? = null
    private var customClusterItemClickListener: ClusterManager.OnClusterItemClickListener<T>? = null
    private var customClusterClickListener: ClusterManager.OnClusterClickListener<T>? = null
    private var customCameraIdleListener: GoogleMap.OnCameraIdleListener? = null
    private var customOnPageChangedListener: RecyclerViewPager.OnPageChangedListener? = null

    // region constructors
    constructor(context: Context) : super(context) {
        initialize(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(attrs)
    }

    //endregion

    private fun initialize(attrs: AttributeSet?) {
        // TODO: Handle attributes

        LayoutInflater.from(context).inflate(R.layout.map_pager, this, true)

        map_view_pager.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        map_view_pager.addOnPageChangedListener(this)
        map_view_pager.setHasFixedSize(true)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        val clusterManager = MapPagerClusterManager<T>(context, googleMap, customCameraIdleListener)
        clusterManager.algorithm = algorithm

        markerRenderer = mapReadyCallback?.onMapReady(googleMap, clusterManager)
        markerRenderer?.minClusterSize = minClusterSize
        setClusteringEnabled(clusteringEnabled)

        clusterManager.renderer = markerRenderer
        clusterManager.setOnClusterClickListener(customClusterClickListener ?: this)
        clusterManager.setOnClusterInfoWindowClickListener(this)
        clusterManager.setOnClusterItemClickListener(customClusterItemClickListener ?: this)
        clusterManager.setOnClusterItemInfoWindowClickListener(this)

        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMapClickListener(customMapClickListener ?: this)
        googleMap.setInfoWindowAdapter(customInfoWindowAdapter)
        googleMap.setOnInfoWindowClickListener(customInfoWindowClickListener)

        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isIndoorLevelPickerEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false

        this.clusterManager = clusterManager
    }

    override fun onClusterClick(cluster: Cluster<T>): Boolean {
        if (cluster.items.contains(currentlySelectedItem)) markerRenderer?.renderPreviousClusterAsUnselected() else dismissViewPager()

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        val builder = LatLngBounds.builder()
        cluster.items.map { builder.include(it.position) }

        val bounds = builder.build()
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapCameraAnimationSpeed))

        return true
    }

    override fun onClusterItemClick(clusterItem: T): Boolean {
        if (pagerAdapter != null) {
            fromClusterItemClick = true

            currentlySelectedItem?.isSelected = false
            currentlySelectedItem?.isViewed = true

            markerRenderer?.renderClusterItemAsSelected(clusterItem)

            clusterItem.isSelected = true
            currentlySelectedItem = clusterItem

            if (map_view_pager.visibility != View.VISIBLE) showViewPager() else map_view_pager.scrollToPosition(pagerAdapter!!.getPositionOfItem(clusterItem))
        }
        return false
    }

    override fun onMapClick(latLng: LatLng) = dismissViewPager()

    override fun OnPageChanged(previousPosition: Int, position: Int) {
        if (pagerAdapter != null && markerRenderer != null && clusterManager != null) {
            if (fromClusterItemClick) {
                fromClusterItemClick = false
                return
            }

            val clusterItem = pagerAdapter!!.getItemAtPosition(position)
            clusterItem.isSelected = true

            // the old item
            currentlySelectedItem?.isSelected = false
            currentlySelectedItem?.isViewed = true

            val itemPosition = if (!markerRenderer!!.renderClusterItemAsSelected(clusterItem)) markerRenderer!!.getClusterMarkerPosition(clusterManager!!.clusterMarkerCollection.markers, clusterItem) else pagerAdapter!!.getItemPositionOnMap(position)
            googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(itemPosition, googleMap?.cameraPosition?.zoom ?: 12.5f)), mapCameraAnimationSpeed, null)

            // update
            currentlySelectedItem = clusterItem

            customOnPageChangedListener?.OnPageChanged(previousPosition, position)
        }
    }

    override fun onClusterInfoWindowClick(cluster: Cluster<T>) {
    }


    override fun onClusterItemInfoWindowClick(item: T) {
    }

    //region wrappers for MapView lifecycle

    fun onCreate(savedInstanceState: Bundle?) {
        map_view.onCreate(savedInstanceState)
    }

    fun getMapAsync(callback: MapReadyCallback<T>) {
        this.mapReadyCallback = callback
        map_view.getMapAsync(this)
    }

    fun onResume() {
        map_view.onResume()
    }

    fun onStart() {
        map_view.onStart()
    }

    fun onPause() {
        map_view.onPause()
    }

    fun onStop() {
        map_view.onStop()
    }

    fun onDestroy() {
        map_view.onDestroy()
        map_view_pager.removeOnPageChangedListener(this)
    }

    fun onLowMemory() {
        map_view.onLowMemory()
    }

    //endregion

    fun getUISettings(): UiSettings? {
        return googleMap?.uiSettings
    }

    fun showViewPager() {
        pagerAdapter?.clearCallbacks()

        if (pagerAdapter != null) {
            val position = if (currentlySelectedItem != null) pagerAdapter!!.getPositionOfItem(currentlySelectedItem!!) else map_view_pager.currentPosition
            val observables = (Math.max(position - 1, 0)..Math.min(pagerAdapter!!.itemCount - 1, position + 1))
                    .filter { map_view_pager.findViewHolderForAdapterPosition(it) == null }
                    .map { Observable.create(ViewCreatedObserver(pagerAdapter!!, it, map_view_pager)) }

            if (observables.isNotEmpty()) {
                viewSubscriber?.unsubscribe()
                viewSubscriber = observables.combineLatest { it }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onNext = {
                                    pagerAdapter?.clearCallbacks()
                                    animateViewPagerVisible()
                                }
                        )
            } else {
                animateViewPagerVisible()
            }
            map_view_pager.scrollToPosition(pagerAdapter!!.getPositionOfItem(currentlySelectedItem))
        }
    }

    fun dismissViewPager() {
        if (map_view_pager.visibility == View.VISIBLE) {
            startViewPagerTranslateAnimation(map_view_pager.currentPosition, 0f, map_view.measuredHeight.toFloat(), AccelerateInterpolator(), false)
        }

        currentlySelectedItem?.isSelected = false
        currentlySelectedItem = null

        markerRenderer?.unselectAllItems()
    }

    fun animateViewPagerVisible() {
        if (pagerAdapter != null) {
            map_view_pager.visibility = View.VISIBLE

            val pos = if (currentlySelectedItem != null) pagerAdapter!!.getPositionOfItem(currentlySelectedItem) else map_view_pager.currentPosition
            startViewPagerTranslateAnimation(pos, map_view_pager.measuredHeight.toFloat(), 0f, OvershootInterpolator(0.3f), true)
        }
    }

    fun startViewPagerTranslateAnimation(position: Int, fromYDelta: Float, toYDelta: Float, interpolator: Interpolator, animateToVisible: Boolean) {
        var k = 0

        val max = Math.min(pagerAdapter!!.itemCount - 1, position + 1)

        for (i in Math.max(position - 1, 0)..max) {
            val holder = map_view_pager.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                val itemView = holder.itemView

                val translateAnimation = TranslateAnimation(0f, 0f, fromYDelta, toYDelta)
                translateAnimation.duration = 400
                translateAnimation.interpolator = interpolator
                translateAnimation.startOffset = (k * 50).toLong()

                val finalI = i
                translateAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        if (animateToVisible) {
                            itemView.visibility = View.VISIBLE
                        }
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        if (animateToVisible) {
                            itemView.visibility = View.VISIBLE
                        } else {
                            itemView.visibility = View.GONE
                            // if it's the final animation
                            if (finalI == max) {
                                map_view_pager.visibility = View.GONE
                            }
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                itemView.startAnimation(translateAnimation)
                k++
            }
        }
    }

    fun setAdapter(adapter: MapPagerAdapter<T, out RecyclerView.ViewHolder>) {
        if (pagerAdapter == null) {
            map_view_pager.adapter = adapter
        } else {
            map_view_pager.swapAdapter(adapter, true)
        }
        this.pagerAdapter = adapter
    }

    fun updateMapItems(clusterItems: List<T>, groupItemsByNearestNeighbor: Boolean) {
        clusterManager?.clearItems()
        clusterManager?.addItems(clusterItems)
        clusterManager?.cluster()

        pagerAdapter?.updateItems(if (groupItemsByNearestNeighbor) getClosestNeighborsList() else clusterItems)
    }

    fun getClosestNeighborsList(): List<T> {
        val processedList = mutableListOf<T>()

        val clusters = algorithm.getClusters(12.5)
        val closestClusterNeighbors = getClosestClusterNeighbors(clusters)

        closestClusterNeighbors.map { processedList.addAll(it.items) }
        return processedList
    }

    fun getClosestClusterNeighbors(clusters: MutableSet<out Cluster<T>>): List<Cluster<T>> {
        if (clusters.isEmpty()) {
            return listOf()
        }

        val startList = ArrayList<Cluster<T>>(clusters.size)
        startList.addAll(clusters)

        val processedList = ArrayList<Cluster<T>>(clusters.size)
        processedList.add(startList[0])

        // For each cluster, find the next closest cluster to it
        for (i in clusters.indices) {
            val subList = startList.subList(Math.min(i + 1, clusters.size), clusters.size)
            if (subList.size > 0) {
                Collections.sort(subList, MapClusterItemComparator(startList[i]))
                // add the next nearest neighbor to the list
                processedList.add(subList[0])
            }
        }

        return processedList
    }

    //region ViewPager customization
    fun setViewPagerPadding(left: Int, top: Int, right: Int, bottom: Int) {
        map_view_pager.setPadding(left, top, right, bottom)
    }

    fun setViewPagerHeightPercent(percent: Double) {
        map_view_pager.layoutParams.height = (map_view.measuredHeight * percent).toInt()
    }

    fun getCurrentViewPagerPosition(): Int {
        return map_view_pager.currentPosition
    }

    fun scrollViewPagerToPosition(position: Int, smoothScroll: Boolean) {
        if (smoothScroll) {
            map_view_pager.smoothScrollToPosition(position)
        } else {
            map_view_pager.scrollToPosition(position)
        }
    }

    fun setCustomOnPageChangedListener(customOnPageChangedListener: RecyclerViewPager.OnPageChangedListener?) {
        this.customOnPageChangedListener = customOnPageChangedListener
    }

    //endregion

    //region Google Map Customization

    fun getMapBounds(): LatLngBounds? {
        return googleMap?.projection?.visibleRegion?.latLngBounds
    }

    fun addMarker(markerOptions: MarkerOptions): Marker? {
        return googleMap?.addMarker(markerOptions)
    }

    fun moveCameraToBounds(bounds: LatLngBounds, padding: Int) {
        try {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, map_view.width, map_view.height, padding))
        } catch (e: Exception) {
            // In case the map initialization is not quite there yet
            Handler().postDelayed({ googleMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, map_view.width, map_view.height, padding)) }, 500)
        }
    }

    fun moveCamera(cameraUpdate: CameraUpdate) {
        try {
            googleMap?.moveCamera(cameraUpdate)
        } catch (e: Exception) {
            // In case the map initialization is not quite there yet
            Handler().postDelayed({ googleMap?.moveCamera(cameraUpdate) }, 500)
        }

    }

    fun setMapCameraAnimationSpeed(cameraAnimationSpeed: Int) {
        this.mapCameraAnimationSpeed = cameraAnimationSpeed
    }

    fun getCameraPosition(): CameraPosition? {
        return googleMap?.cameraPosition
    }
    //endregion

    //region cluster customization
    fun setClusteringEnabled(enabled: Boolean) {
        // 4 is the default that the MapsUtils library uses
        clusteringEnabled = enabled
        markerRenderer?.setClusteringEnabled(clusteringEnabled)
    }

    fun setMinClusterSize(size: Int) {
        this.minClusterSize = size
    }

    fun setAlgorithm(algorithm: Algorithm<T>) {
        this.algorithm = algorithm
    }

    fun getAlgorithm(): Algorithm<T> {
        return algorithm
    }
    //endregion
}