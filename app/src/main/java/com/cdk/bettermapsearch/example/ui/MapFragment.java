package com.cdk.bettermapsearch.example.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdk.bettermapsearch.R;
import com.cdk.bettermapsearch.example.models.ItemModel;
import com.cdk.bettermapsearch.example.models.LatLngModel;
import com.cdk.bettermapsearch.project.CustomPagerAdapter;
import com.cdk.bettermapsearch.project.MapPagerView;
import com.cdk.bettermapsearch.project.clustering.CachedClusterManager;
import com.cdk.bettermapsearch.project.clustering.CustomMarkerRenderer;
import com.cdk.bettermapsearch.project.interfaces.MapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import butterknife.ButterKnife;

public class MapFragment extends Fragment implements MapReadyCallback<LatLngModel> {

    private static final String items = "{\n" +
            "  \"items\": [\n" +
            "    {\n" +
            "      \"latitude\": 37.78837800000000157751856022514402866363525390625,\n" +
            "      \"longitude\": -122.4754750000000029785951483063399791717529296875\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.78863199999999977762854541651904582977294921875,\n" +
            "      \"longitude\": -122.4747550000000018144419300369918346405029296875\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.78774200000000149657353176735341548919677734375,\n" +
            "      \"longitude\": -122.4743959999999987076080287806689739227294921875\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.78620399999999790452420711517333984375,\n" +
            "      \"longitude\": -122.4811040000000019745129975490272045135498046875\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.78854100000000215686668525449931621551513671875,\n" +
            "      \"longitude\": -122.4758340000000060854290495626628398895263671875\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.78846800000000172303771250881254673004150390625,\n" +
            "      \"longitude\": -122.47525799999999662759364582598209381103515625\n" +
            "    },\n" +
            "    {\n" +
            "      \"latitude\": 37.7872210000000023910615709610283374786376953125,\n" +
            "      \"longitude\": -122.474703000000005204128683544695377349853515625\n" +
            "    }\n" +
            "  ]\n" +
            "}";

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

        // Add backing list
        mapPagerView.onCreate(null); // savedInstanceState crashes this sometimes
        mapPagerView.initialize(getPhoneHeight(getActivity()), this);
        mapPagerView.getMapAsync();
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
        final ItemModel itemModel = new Gson().fromJson(items, ItemModel.class);
        final List<LatLngModel> items = itemModel.getItems();

        mapPagerView.updateMapItems(items);

        return new MyMarkerRenderer(getContext(), googleMap, clusterManager, android.R.color.white, android.R.color.black);
    }

    public static class ViewCreatedEvent {
        // TODO: Put some stuff in here?
    }

    //region test classes

    public static class MyMarkerRenderer extends CustomMarkerRenderer<LatLngModel> {

        public MyMarkerRenderer(Context context, GoogleMap map, ClusterManager<LatLngModel> clusterManager, @ColorRes int colorNormal, @ColorRes int colorActivated) {
            super(context, map, clusterManager, colorNormal, colorActivated);
        }

        @Override
        public void setupClusterView(Cluster<LatLngModel> cluster, @ColorRes int colorRes) {

        }

        @Override
        public void setupClusterItemView(LatLngModel item, @ColorRes int colorRes) {

        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public ItemViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public static class MyViewPagerAdapter extends CustomPagerAdapter<LatLngModel, ItemViewHolder> {

        public MyViewPagerAdapter() {
        }

        public MyViewPagerAdapter(List<LatLngModel> backingList) {
            super(backingList);
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
