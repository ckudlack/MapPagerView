package com.cdk.bettermapsearch;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.cdk.bettermapsearch.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.clustering.CustomMarkerRenderer;
import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.cdk.bettermapsearch.interfaces.MapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * This class encapsulates the MapView, ViewPager, and all the logic and callbacks related to linking the two
 */
public class MapPagerView extends FrameLayout implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        RecyclerViewPager.OnPageChangedListener,
        ClusterManager.OnClusterClickListener<MapClusterItem>,
        ClusterManager.OnClusterInfoWindowClickListener<MapClusterItem>,
        ClusterManager.OnClusterItemClickListener<MapClusterItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MapClusterItem> {

    private static final double DEFAULT_VIEW_PAGER_HEIGHT_PERCENT = 0.25;
    private static final int DEFAULT_MAP_CAMERA_ANIMATION_SPEED = 200;
    private static final int DEFAULT_CLUSTER_SIZE = 4;

    //region variables
    private MapView mapView;
    private RecyclerViewPager viewPager;

    @Nullable private GoogleMap googleMap;

    @Nullable private MapPagerAdapter pagerAdapter;

    private MapClusterItem currentlySelectedItem;

    private MapReadyCallback mapReadyCallback;

    private Subscription viewSubscriber;

    @Nullable private CustomMarkerRenderer markerRenderer;
    @Nullable private CachedClusterManager clusterManager;

    private boolean clusteringEnabled = true;

    private int minClusterSize = DEFAULT_CLUSTER_SIZE;
    private int mapCameraAnimationSpeed = DEFAULT_MAP_CAMERA_ANIMATION_SPEED;

    private Algorithm<MapClusterItem> algorithm;

    @Nullable private GoogleMap.OnMapClickListener customMapClickListener;
    @Nullable private GoogleMap.OnInfoWindowClickListener customInfoWindowClickListener;
    @Nullable private GoogleMap.InfoWindowAdapter customInfoWindowAdapter;
    @Nullable private ClusterManager.OnClusterItemClickListener<MapClusterItem> customClusterItemClickListener;
    @Nullable private ClusterManager.OnClusterClickListener<MapClusterItem> customClusterClickListener;
    @Nullable private GoogleMap.OnCameraIdleListener customCameraIdleListener;
    //endregion

    // region constructors
    public MapPagerView(Context context) {
        super(context);
        initialize(null);
    }

    public MapPagerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public MapPagerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MapPagerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(attrs);
    }

    //endregion

    private void initialize(@Nullable AttributeSet attrs) {
        // TODO: Handle attributes

        LayoutInflater.from(getContext()).inflate(R.layout.map_pager, this, true);
        mapView = (MapView) findViewById(R.id.map_view);
        viewPager = (RecyclerViewPager) findViewById(R.id.map_view_pager);

        viewPager.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        viewPager.addOnPageChangedListener(this);
        viewPager.setHasFixedSize(true);

        algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
    }

    //region Map and clustering callbacks
    @Override
    public void onMapClick(LatLng latLng) {
        dismissViewPager();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Do some map stuff
        clusterManager = new CachedClusterManager(getContext(), googleMap, customCameraIdleListener);
        clusterManager.setAlgorithm(algorithm);
        markerRenderer = mapReadyCallback.onMapReady(googleMap, clusterManager);
        markerRenderer.setMinClusterSize(minClusterSize);
        setClusteringEnabled(clusteringEnabled);
        clusterManager.setRenderer(markerRenderer);

        clusterManager.setOnClusterClickListener(customClusterItemClickListener == null ? this : customClusterClickListener);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(customClusterItemClickListener == null ? this : customClusterItemClickListener);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);

        googleMap.setOnMarkerClickListener(clusterManager);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMapClickListener(customMapClickListener == null ? this : customMapClickListener);
        googleMap.setInfoWindowAdapter(customInfoWindowAdapter);
        googleMap.setOnInfoWindowClickListener(customInfoWindowClickListener);

        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public boolean onClusterClick(Cluster<MapClusterItem> cluster) {
        if (markerRenderer == null) {
            return true;
        }

        if (markerRenderer.clusterContainsItem(cluster, currentlySelectedItem)) {
            markerRenderer.renderPreviousClusterAsUnselected();
        } else {
            dismissViewPager();
        }

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }

        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        if (googleMap != null) {
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapCameraAnimationSpeed));
            } catch (IllegalStateException e) {
                // Screen size is too small, get rid of padding
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            }
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MapClusterItem> cluster) {
        // no-op
    }

    @Override
    public boolean onClusterItemClick(MapClusterItem clusterItem) {
        if (markerRenderer == null || pagerAdapter == null) {
            return false;
        }

        markerRenderer.renderClusterItemAsSelected(clusterItem);

        currentlySelectedItem = clusterItem;
        currentlySelectedItem.setIsSelected(true);

        if (viewPager.getVisibility() != View.VISIBLE) {
            showViewPager();
        } else {
            viewPager.scrollToPosition(pagerAdapter.getPositionOfItem(currentlySelectedItem));
        }
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MapClusterItem t) {
        // no-op
    }

    //endregion

    @Override
    public void OnPageChanged(int size, int position) {
        if (googleMap == null || clusterManager == null || markerRenderer == null || pagerAdapter == null) {
            return;
        }

        //noinspection unchecked
        MapClusterItem clusterItem = (MapClusterItem) pagerAdapter.getItemAtPosition(position);
        clusterItem.setIsSelected(true);

        // the old item
        currentlySelectedItem.setIsSelected(false);
        currentlySelectedItem.setIsViewed(true);

        LatLng itemPosition = !markerRenderer.renderClusterItemAsSelected(clusterItem) ? markerRenderer.getClusterMarkerPosition(clusterManager.getClusterMarkerCollection().getMarkers(), clusterItem) : pagerAdapter.getItemPositionOnMap(position);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(itemPosition, googleMap.getCameraPosition().zoom)), mapCameraAnimationSpeed, null);

        // update
        currentlySelectedItem = clusterItem;
    }

    //region wrappers for MapView lifecycle

    public void onCreate(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
    }

    public void getMapAsync(MapReadyCallback callback) {
        this.mapReadyCallback = callback;
        mapView.getMapAsync(this);
    }

    public void onResume() {
        mapView.onResume();
    }

    public void onStart() {
        mapView.onStart();
    }

    public void onPause() {
        mapView.onPause();
    }

    public void onStop() {
        mapView.onStop();
    }

    public void onDestroy() {
        mapView.onDestroy();
        viewPager.removeOnPageChangedListener(this);
    }

    public void onLowMemory() {
        mapView.onLowMemory();
    }

    //endregion

    //region override callbacks

    public void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener listener) {
        customInfoWindowClickListener = listener;
    }

    public void setInfoWindowAdapter(GoogleMap.InfoWindowAdapter windowAdapter) {
        customInfoWindowAdapter = windowAdapter;
    }

    public void setOnMapClickListener(GoogleMap.OnMapClickListener mapClickListener) {
        this.customMapClickListener = mapClickListener;
    }

    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<MapClusterItem> clusterClickListener) {
        this.customClusterClickListener = clusterClickListener;
    }

    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<MapClusterItem> clusterItemClickListener) {
        this.customClusterItemClickListener = clusterItemClickListener;
    }

    public void setCameraIdleListener(GoogleMap.OnCameraIdleListener cameraIdleListener) {
        this.customCameraIdleListener = cameraIdleListener;
    }
    //endregion

    @Nullable
    public UiSettings getUiSettings() {
        if (googleMap != null) {
            return googleMap.getUiSettings();
        }
        return null;
    }

    public void showViewPager() {
        if (pagerAdapter == null) {
            return;
        }

        pagerAdapter.clearCallbacks();

        int pos = currentlySelectedItem != null ? pagerAdapter.getPositionOfItem(currentlySelectedItem) : viewPager.getCurrentPosition();

        List<Observable<Void>> observables = new ArrayList<>();

        for (int i = Math.max(pos - 1, 0); i <= Math.min(pagerAdapter.getItemCount() - 1, pos + 1); i++) {
            if (viewPager.findViewHolderForAdapterPosition(i) == null) {
                observables.add(Observable.create(new ViewCreatedObserver(pagerAdapter, i, viewPager)));
            }
        }

        if (observables.size() > 0) {
            if (viewSubscriber != null) {
                viewSubscriber.unsubscribe();
            }
            viewSubscriber = Observable.combineLatest(observables, args -> null)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Object ignored) {
                            pagerAdapter.clearCallbacks();
                            animateViewPagerVisible();
                        }
                    });
        } else {
            animateViewPagerVisible();
        }

        viewPager.scrollToPosition(pagerAdapter.getPositionOfItem(currentlySelectedItem));
    }

    public void dismissViewPager() {
        if (pagerAdapter == null) {
            return;
        }

        if (viewPager.getVisibility() == View.VISIBLE) {
            startViewPagerTranslateAnimation(viewPager.getCurrentPosition(), 0, mapView.getMeasuredHeight(), new AccelerateInterpolator(), false);
        }

        if (currentlySelectedItem != null) {
            currentlySelectedItem.setIsSelected(false);
            currentlySelectedItem = null;
        }

        if (markerRenderer != null) {
            markerRenderer.unselectAllItems();
        }
    }

    private void animateViewPagerVisible() {
        if (pagerAdapter == null) {
            return;
        }

        viewPager.setVisibility(View.VISIBLE);

        int pos = currentlySelectedItem != null ? pagerAdapter.getPositionOfItem(currentlySelectedItem) : viewPager.getCurrentPosition();
        startViewPagerTranslateAnimation(pos, mapView.getMeasuredHeight(), 0, new OvershootInterpolator(0.3f), true);
    }

    private void startViewPagerTranslateAnimation(int pos, float fromYDelta, float toYDelta, Interpolator interpolator, boolean animateToVisible) {
        int k = 0;
        //noinspection ConstantConditions
        final int max = Math.min(pagerAdapter.getItemCount() - 1, pos + 1);

        for (int i = Math.max(pos - 1, 0); i <= max; i++) {
            RecyclerView.ViewHolder holder = viewPager.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                View itemView = holder.itemView;

                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, fromYDelta, toYDelta);
                translateAnimation.setDuration(400);
                translateAnimation.setInterpolator(interpolator);
                translateAnimation.setStartOffset(k * 50);

                int finalI = i;
                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        if (animateToVisible) {
                            itemView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (animateToVisible) {
                            itemView.setVisibility(View.VISIBLE);
                        } else {
                            itemView.setVisibility(View.GONE);
                            // if it's the final animation
                            if (finalI == max) {
                                viewPager.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                itemView.startAnimation(translateAnimation);
                k++;
            }
        }
    }

    // This is called every time data is refreshed
    public void setAdapter(MapPagerAdapter adapter) {
        if (pagerAdapter == null) {
            viewPager.setAdapter(adapter);
        } else {
            viewPager.swapAdapter(adapter, true);
        }
        this.pagerAdapter = adapter;
    }

    @SuppressWarnings("unchecked")
    public void updateMapItems(List<? extends MapClusterItem> clusterItems) {
        if (clusterManager == null || pagerAdapter == null) {
            return;
        }

        clusterManager.clearItems();

        // Can't use .addAll() with wildcard
        for (MapClusterItem clusterItem : clusterItems) {
            clusterManager.addItem(clusterItem);
        }

        clusterManager.cluster();

        pagerAdapter.updateItems(clusterItems);
    }

    //region ViewPager customization
    public void setViewPagerPadding(int left, int top, int right, int bottom) {
        viewPager.setPadding(left, top, right, bottom);
    }

    public void setViewPagerHeightPercent(double percent) {
        viewPager.getLayoutParams().height = (int) (mapView.getMeasuredHeight() * percent);
    }

    public int getCurrentViewPagerPosition() {
        return viewPager.getCurrentPosition();
    }

    public void scrollViewPagerToPosition(int position, boolean smoothScroll) {
        if (smoothScroll) {
            viewPager.smoothScrollToPosition(position);
        } else {
            viewPager.scrollToPosition(position);
        }
    }
    //endregion

    //region Google Map Customization

    @Nullable
    public LatLngBounds getMapBounds() {
        if (googleMap != null) {
            return googleMap.getProjection().getVisibleRegion().latLngBounds;
        }
        return null;
    }

    @Nullable
    public Marker addMarker(MarkerOptions markerOptions) {
        if (googleMap != null) {
            return googleMap.addMarker(markerOptions);
        }
        return null;
    }

    public void moveCameraToBounds(LatLngBounds bounds, int padding) {
        try {
            if (googleMap != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.getWidth(), mapView.getHeight(), padding));
            }
        } catch (Exception e) {
            // In case the map initialization is not quite there yet
            new Handler().postDelayed(() -> googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapView.getWidth(), mapView.getHeight(), padding)), 500);
        }
    }

    public void setMapCameraAnimationSpeed(int cameraAnimationSpeed) {
        this.mapCameraAnimationSpeed = cameraAnimationSpeed;
    }

    @Nullable
    public CameraPosition getCameraPosition() {
        if (googleMap != null) {
            return googleMap.getCameraPosition();
        }
        return null;
    }
    //endregion

    //region cluster customization
    public void setClusteringEnabled(boolean enabled) {
        // 4 is the default that the MapsUtils library uses
        clusteringEnabled = enabled;
        if (markerRenderer != null) {
            markerRenderer.setClusteringEnabled(clusteringEnabled);
        }
    }

    public void setMinClusterSize(int size) {
        this.minClusterSize = size;
    }

    public void setAlgorithm(Algorithm<MapClusterItem> algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm<MapClusterItem> getAlgorithm() {
        return algorithm;
    }
    //endregion

}
