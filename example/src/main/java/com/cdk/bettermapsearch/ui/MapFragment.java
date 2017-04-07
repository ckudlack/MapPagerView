package com.cdk.bettermapsearch.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdk.bettermapsearch.MapPagerView;
import com.cdk.bettermapsearch.MapPagerAdapter;
import com.cdk.bettermapsearch.clustering.MapPagerClusterManager;
import com.cdk.bettermapsearch.clustering.MapPagerMarkerRenderer;
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

        //noinspection unchecked
        mapPagerView = (MapPagerView<LatLngModel>) view.findViewById(R.id.map_pager);
        mapPagerView.setAdapter(new MyViewPagerAdapter());

        mapPagerView.onCreate(null); // savedInstanceState crashes this sometimes
        mapPagerView.getMapAsync(this);

        simulateNetworkCall();
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

    private LatLngBounds createBoundsFromList(List<LatLngModel> items) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLngModel item : items) {
            boundsBuilder.include(item.getPosition());
        }
        return boundsBuilder.build();
    }

    @NonNull
    @Override
    public MapPagerMarkerRenderer<LatLngModel> onMapReady(@NonNull GoogleMap googleMap, @NonNull MapPagerClusterManager<LatLngModel> clusterManager) {
        return new MyMarkerRenderer(getContext(), googleMap, clusterManager);
    }

    private void simulateNetworkCall() {
        mapPagerView.postDelayed(() -> {
            String json = null;
            try {
                json = FileUtils.getStringFromFile(getContext(), "sample_locations.json");
            } catch (Exception e) {
                e.printStackTrace();
            }

            final ItemModel itemModel = new Gson().fromJson(json, ItemModel.class);

            final List<LatLngModel> items = itemModel.getItems();

            mapPagerView.updateMapItems(items, false);
            mapPagerView.moveCameraToBounds(createBoundsFromList(items), 100);
        }, 1000);
    }

    //region example classes

    public static class MyMarkerRenderer extends MapPagerMarkerRenderer<LatLngModel> {

        private TextView clusterText;
        private TextView clusterItemText;

        public MyMarkerRenderer(Context context, GoogleMap map, ClusterManager<LatLngModel> clusterManager) {
            super(context, map, clusterManager);

            View clusterView = View.inflate(context, R.layout.cluster_view, null);
            clusterText = (TextView) clusterView.findViewById(R.id.text_primary);
            clusterItemText = (TextView) View.inflate(context, R.layout.cluster_item_view, null);

            getClusterIconGenerator().setContentView(clusterView);
            getClusterItemIconGenerator().setContentView(clusterItemText);
        }

        @Override
        protected void setupClusterView(Cluster<LatLngModel> cluster, boolean isSelected) {
            clusterText.setText(getContext().getResources().getString(R.string.cluster_text, cluster.getItems().size()));
            clusterText.setTextColor(ContextCompat.getColor(getContext(), isSelected ? android.R.color.white : android.R.color.black));
        }

        @Override
        protected void setupClusterItemView(LatLngModel item, boolean isSelected) {
            clusterItemText.setText(item.getName());
            clusterItemText.setTextColor(ContextCompat.getColor(getContext(), isSelected ? android.R.color.white : android.R.color.black));
        }

        @Override
        protected void setClusterViewBackground(boolean isSelected) {
            getClusterIconGenerator().setColor(ContextCompat.getColor(getContext(), isSelected ? android.R.color.black : android.R.color.white));
        }

        @Override
        protected void setClusterItemViewBackground(boolean isSelected) {
            getClusterItemIconGenerator().setColor(ContextCompat.getColor(getContext(), isSelected ? android.R.color.black : android.R.color.white));
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public ItemViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class MyViewPagerAdapter extends MapPagerAdapter<LatLngModel, ItemViewHolder> {

        public MyViewPagerAdapter() {
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            holder.title.setText(getItemAtPosition(position).getName());
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pager_item_view, parent, false));
        }
    }

    //endregion
}
