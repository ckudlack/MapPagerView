# MapPagerView

A MapView combined with a ViewPager for ultimate search efficiency! Can be used in an Activity or Fragment

## How to compile AAR

- `./gradlew library:assemble`
- Get library-release.aar from build/outputs/aar
- Use [jetifier-standalone](https://developer.android.com/studio/command-line/jetifier) tool to convert dependencies to androidx

## Features
MapPagerView encapsulates a MapView, ViewPager, and map clustering so that you can present an uncluttered, easily searchable interface to your users

## How to use
1. Add MapPagerView into your layout xml. When you inflate it, remember to include the type

  ```
  <com.cdk.bettermapsearch.MapPagerView
        android:id="@+id/map_pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
  ```
  MapPagerView<MyCustomModelClass> mapPagerView = (MapPagerView<MyCustomModelClass>) findViewById(R.id.map_pager);
  mapPagerView.onCreate(savedInstanceState);
  mapPagerView.getMapAsync(this);
  ```
2. MapPagerView serves as a delegate for MapViews lifecycle methods, so you'll need to make sure to add all of the usual lifecycle methods that you use for MapView.
3. When the GoogleMap is ready, you'll receive a callback from MapPagerView (supplied to the `getMapAsync` method) that provides all of the fields needed to create your custom MarkerRenderer, which you give back to MapPagerView.

```
@Override
public MapPagerMarkerRenderer<MyCustomModelClass> onMapReady(GoogleMap googleMap, MapPagerClusterManager<MyCustomModelClass> clusterManager) {
    // You'll need to return your custom implmentation of MarkerRenderer and pass it back to the MapPagerView
    return new MyMarkerRenderer(getContext(), googleMap, clusterManager);
}
```

4. Use the `updateItems()` method to populate data on the map. This needs to be called _after_ the MarkerRenderer is returned. If you want to use nearest-neighbor processing on the list, pass `true` as the second argument in `updateItems()`

## Customizability
* The abstract class `MapPagerMarkerRenderer` controls the logic of when to show items & clusters as selected, you'll just need to implement the 4 abstract methods that handle the UI of the markers
* The abstract class `MapPagerAdapter` controls the ViewPager logic, it requires 2 types: your data model class and the ViewHolder type
* Obviously you'll also need to create your own ViewHolder, just like you normally would for an Adapter
* MapPager view has a method `setClusteringEnabled(boolean)` that allows you to turn marker clustering on and off

## Callbacks
You can set your own listeners for all these callbacks, if you so choose:
```
    GoogleMap.OnMapClickListener
    GoogleMap.OnInfoWindowClickListener
    GoogleMap.InfoWindowAdapter
    ClusterManager.OnClusterItemClickListener<MyCustomModelClass>
    ClusterManager.OnClusterClickListener<MyCustomModelClass>
    GoogleMap.OnCameraIdleListener
    RecyclerViewPager.OnPageChangedListener
```
Just make sure to call the default implementation of the callback in `MapPagerView` if you still want the default functionality:
```
@Override
public boolean onClusterClick(Cluster<MyCustomModelClass> cluster) {
    // Whatever logic you want to do goes in here
    
    return mapPagerView.onClusterClick(cluster); // add this for default functionality
}
```

## Related Links
* [Google MapsUtils](https://developers.google.com/maps/documentation/android-api/utility/)
* [RecyclerViewPager](https://github.com/lsjwzh/RecyclerViewPager)

### TODO: 
- Build out example more
- Add XML attributes to MapPagerView
- Replace Observable.create() with RxRelay
- Add to Maven
