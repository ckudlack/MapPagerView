package com.cdk.bettermapsearch;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.cdk.bettermapsearch.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.clustering.CustomMarkerRenderer;
import com.cdk.bettermapsearch.interfaces.MapClusterItem;
import com.cdk.bettermapsearch.interfaces.MapReadyCallback;
import com.cdk.bettermapsearch.interfaces.SelectedItemCallback;
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
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MapPagerView<T extends MapClusterItem> extends FrameLayout implements
        OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener,
        RecyclerViewPager.OnPageChangedListener,
        ClusterManager.OnClusterClickListener<T>,
        ClusterManager.OnClusterInfoWindowClickListener<T>,
        ClusterManager.OnClusterItemClickListener<T>,
        ClusterManager.OnClusterItemInfoWindowClickListener<T>,
        SelectedItemCallback<T> {

    public static final double DEFAULT_VIEW_PAGER_HEIGHT_PERCENT = 0.25;
    private static final int DEFAULT_MAP_CAMERA_ANIMATION_SPEED = 200;

    public MapPagerView(Context context) {
        super(context);
        initialize();
    }

    public MapPagerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MapPagerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    //region variables
    private MapView mapView;
    private RecyclerViewPager viewPager;

    @Nullable private CachedClusterManager<T> clusterManager;
    @Nullable private GoogleMap googleMap;
    private T currentlySelectedItem;
    @Nullable private MapPagerAdapter pagerAdapter;
    private int phoneHeight;
    @Nullable private CustomMarkerRenderer<T> markerRenderer;
    private MapReadyCallback<T> mapReadyCallback;
    private Subscription viewSubscriber;
    private boolean clusteringEnabled = true;
    private int minClusterSize = 4;
    private int mapCameraAnimationSpeed = DEFAULT_MAP_CAMERA_ANIMATION_SPEED;
    private NonHierarchicalDistanceBasedAlgorithm<T> algorithm;

    @Nullable private GoogleMap.OnMapClickListener customMapClickListener;
    @Nullable private GoogleMap.OnInfoWindowClickListener customInfoWindowClickListener;
    @Nullable private GoogleMap.InfoWindowAdapter customInfoWindowAdapter;
    @Nullable private ClusterManager.OnClusterItemClickListener<T> customClusterItemClickListener;
    @Nullable private ClusterManager.OnClusterClickListener<T> customClusterClickListener;
    @Nullable private GoogleMap.OnCameraIdleListener customCameraIdleListener;
    //endregion

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.map_pager, this, true);
        mapView = (MapView) findViewById(R.id.map);
        viewPager = (RecyclerViewPager) findViewById(R.id.view_pager);

        viewPager.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        viewPager.addOnPageChangedListener(this);
        viewPager.setHasFixedSize(true);

        algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
    }

    //region Map and clustering callbacks

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // no-op
    }

    @Override
    public void onMapClick(LatLng latLng) {
        dismissViewPager();
        if (markerRenderer != null) {
            markerRenderer.unselectAllItems();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Do some map stuff
        clusterManager = new CachedClusterManager<>(getContext(), googleMap, customCameraIdleListener);
        clusterManager.setAlgorithm(algorithm);
        markerRenderer = mapReadyCallback.onMapReady(googleMap, clusterManager);
        markerRenderer.setItemCallback(this);
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
        googleMap.setInfoWindowAdapter(customInfoWindowAdapter == null ? this : customInfoWindowAdapter);
        googleMap.setOnInfoWindowClickListener(customInfoWindowClickListener == null ? this : customInfoWindowClickListener);

        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public boolean onClusterClick(Cluster<T> cluster) {
        if (markerRenderer == null) {
            return true;
        }

        if (currentlySelectedItem != null && markerRenderer.clusterContainsItem(cluster, currentlySelectedItem)) {
            markerRenderer.renderPreviousClusterAsUnselected();
        } else {
            currentlySelectedItem = null;
            dismissViewPager();
            markerRenderer.unselectAllItems();
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
    public void onClusterInfoWindowClick(Cluster<T> cluster) {
        // no-op
    }

    @Override
    public boolean onClusterItemClick(T clusterItem) {
        if (markerRenderer == null) {
            return false;
        }

        markerRenderer.renderClusterItemAsSelected(clusterItem);

        currentlySelectedItem = clusterItem;

        if (viewPager.getVisibility() != View.VISIBLE) {
            // This is to give the fragments some time to build their views
            showViewPager();
        } else {
            viewPager.scrollToPosition(currentlySelectedItem.getIndex());
        }
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(T t) {
        // no-op
    }

    //endregion

    @Override
    public void OnPageChanged(int size, int pos) {
        if (googleMap == null || clusterManager == null || markerRenderer == null || pagerAdapter == null) {
            return;
        }

        T clusterItem = clusterManager.getClusterItem(pos);

        if (!markerRenderer.renderClusterItemAsSelected(clusterItem)) {
            LatLng clusterPosition = markerRenderer.getClusterMarker(clusterManager.getClusterMarkerCollection().getMarkers(), clusterItem);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(clusterPosition, googleMap.getCameraPosition().zoom)), mapCameraAnimationSpeed, null);
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(pagerAdapter.getItemPositionOnMap(pos), googleMap.getCameraPosition().zoom)), mapCameraAnimationSpeed, null);
        }

        currentlySelectedItem = clusterItem;
        currentlySelectedItem.setIsViewed();
    }

    //region wrappers for MapView lifecycle

    public void onCreate(Bundle savedInstanceState, int phoneHeight) {
        mapView.onCreate(savedInstanceState);

        this.phoneHeight = phoneHeight;
    }

    public void getMapAsync(MapReadyCallback<T> callback) {
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

    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<T> clusterClickListener) {
        this.customClusterClickListener = clusterClickListener;
    }

    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> clusterItemClickListener) {
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

        int pos = currentlySelectedItem != null ? currentlySelectedItem.getIndex() : viewPager.getCurrentPosition();

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

        viewPager.scrollToPosition(currentlySelectedItem.getIndex());
    }

    public void dismissViewPager() {
        if (pagerAdapter == null) {
            return;
        }

        if (viewPager.getVisibility() == View.VISIBLE) {
            int pos = viewPager.getCurrentPosition();

            int k = 0;
            final int max = Math.min(pagerAdapter.getItemCount() - 1, pos + 1);

            for (int i = Math.max(pos - 1, 0); i <= max; i++) {
                RecyclerView.ViewHolder holder = viewPager.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    View view = holder.itemView;

                    TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, phoneHeight);
                    translateAnimation.setDuration(400);
                    translateAnimation.setInterpolator(new AccelerateInterpolator());
                    translateAnimation.setStartOffset(k * 50);

                    int finalI = i;
                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.setVisibility(View.GONE);
                            // if it's the final animation
                            if (finalI == max) {
                                viewPager.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    view.startAnimation(translateAnimation);
                    k++;
                }
            }
        }
        currentlySelectedItem = null;
    }

    private void animateViewPagerVisible() {
        if (pagerAdapter == null) {
            return;
        }

        viewPager.setVisibility(View.VISIBLE);

        int pos = currentlySelectedItem != null ? currentlySelectedItem.getIndex() : viewPager.getCurrentPosition();
        int k = 0;

        for (int i = Math.max(pos - 1, 0); i <= Math.min(pagerAdapter.getItemCount() - 1, pos + 1); i++) {
            RecyclerView.ViewHolder holder = viewPager.findViewHolderForAdapterPosition(i);
            final View itemView = holder.itemView;

            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, phoneHeight, 0);
            translateAnimation.setDuration(400);
            translateAnimation.setStartOffset(k * 50);
            translateAnimation.setInterpolator(new OvershootInterpolator(0.3f));
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    itemView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    itemView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            itemView.startAnimation(translateAnimation);
            k++;
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
    public void updateMapItems(List<T> clusterItems) {
        if (clusterManager == null || pagerAdapter == null) {
            return;
        }

        clusterManager.clearItems();

        for (int i = 0; i < clusterItems.size(); i++) {
            // set up each cluster item with the information it needs
            clusterItems.get(i).setIndex(i);
            clusterItems.get(i).buildPositionFromLatAndLon();
        }

        clusterManager.addItems(clusterItems);
        clusterManager.cluster();

        pagerAdapter.updateItems(clusterItems);
    }

    //region ViewPager customization
    public void setViewPagerPadding(int left, int top, int right, int bottom) {
        viewPager.setPadding(left, top, right, bottom);
    }

    public void setViewPagerHeightPercent(double percent) {
        viewPager.getLayoutParams().height = (int) (phoneHeight * percent);
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

    public void setAlgorithm(NonHierarchicalDistanceBasedAlgorithm<T> algorithm) {
        this.algorithm = algorithm;
    }

    public NonHierarchicalDistanceBasedAlgorithm<T> getAlgorithm() {
        return algorithm;
    }
    //endregion

    @Override
    @Nullable
    public T getSelectedItem() {
        return currentlySelectedItem;
    }
}
