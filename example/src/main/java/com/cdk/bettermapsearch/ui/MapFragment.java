package com.cdk.bettermapsearch.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdk.bettermapsearch.MapPagerAdapter;
import com.cdk.bettermapsearch.MapPagerView;
import com.cdk.bettermapsearch.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.clustering.CustomMarkerRenderer;
import com.cdk.bettermapsearch.example.R;
import com.cdk.bettermapsearch.interfaces.MapReadyCallback;
import com.cdk.bettermapsearch.models.ItemModel;
import com.cdk.bettermapsearch.models.LatLngModel;
import com.cdk.bettermapsearch.util.FileUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import butterknife.ButterKnife;

public class MapFragment extends Fragment implements MapReadyCallback<LatLngModel> {

    public MapFragment() {
        // Required empty public constructor
    }

    private MapPagerView<LatLngModel> mapPagerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mapPagerView = ButterKnife.findById(view, R.id.map_pager);
        mapPagerView.setAdapter(new MyViewPagerAdapter());

        mapPagerView.onCreate(null, getPhoneHeight(getActivity())); // savedInstanceState crashes this sometimes
        mapPagerView.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapPagerView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapPagerView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapPagerView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapPagerView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapPagerView.onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapPagerView.onLowMemory();
    }

    public int getPhoneHeight(@NonNull Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public CustomMarkerRenderer<LatLngModel> onMapReady(GoogleMap googleMap, CachedClusterManager<LatLngModel> clusterManager) {
        String json = null;
        try {
            json = FileUtils.getStringFromFile(getContext(), "sample_locations.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ItemModel itemModel = new Gson().fromJson(json, ItemModel.class);

        final List<LatLngModel> items = itemModel.getItems();

        mapPagerView.updateMapItems(items);
        mapPagerView.moveCameraToBounds(createBoundsFromList(items), 100);

        return new MyMarkerRenderer(getContext(), googleMap, clusterManager);
    }

    private LatLngBounds createBoundsFromList(List<LatLngModel> items) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLngModel item : items) {
            boundsBuilder.include(item.getPosition());
        }
        return boundsBuilder.build();
    }

    //region example classes

    public static class MyMarkerRenderer extends CustomMarkerRenderer<LatLngModel> {

        private TextView clusterText;
        private TextView clusterItemText;

        public MyMarkerRenderer(Context context, GoogleMap map, ClusterManager<LatLngModel> clusterManager) {
            super(context, map, clusterManager);

            View clusterView = View.inflate(context, R.layout.cluster_view, null);
            clusterText = (TextView) clusterView.findViewById(R.id.text_primary);
            clusterItemText = (TextView) View.inflate(context, R.layout.cluster_item_view, null);

            clusterIconGenerator.setContentView(clusterView);
            clusterItemIconGenerator.setContentView(clusterItemText);
        }

        @Override
        public void setupClusterView(Cluster<LatLngModel> cluster, boolean isSelected) {
            clusterText.setText(context.getResources().getString(R.string.cluster_text, cluster.getItems().size()));
            clusterText.setTextColor(ContextCompat.getColor(context, isSelected ? android.R.color.white : android.R.color.black));
        }

        @Override
        public void setupClusterItemView(LatLngModel item, boolean isSelected) {
            clusterItemText.setText(context.getResources().getString(R.string.cluster_item_text, item.getIndex()));
            clusterItemText.setTextColor(ContextCompat.getColor(context, isSelected ? android.R.color.white : android.R.color.black));
        }

        @Override
        public void setClusterViewBackground(boolean isSelected) {
            clusterIconGenerator.setColor(ContextCompat.getColor(context, isSelected ? android.R.color.black : android.R.color.white));
        }

        @Override
        public void setClusterItemViewBackground(boolean isSelected) {
            clusterItemIconGenerator.setColor(ContextCompat.getColor(context, isSelected ? android.R.color.black : android.R.color.white));
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public ItemViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public static class MyViewPagerAdapter extends MapPagerAdapter<LatLngModel, ItemViewHolder> {

        public MyViewPagerAdapter() {
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.itemView.setY(0);
            holder.itemView.setVisibility(View.VISIBLE);

            final LatLngModel latLngModel = backingList.get(position);

            holder.title.setText("Item " + latLngModel.getIndex());
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_item_view, parent, false));
        }
    }

    //endregion
}
