# MapPagerView

A MapView combined with a ViewPager for ultimate search effiency! Can be used in an Activity or Fragment

## Features
MapPagerView encapsulates a MapView, ViewPager, and map clustering so that you can present an uncluttered, searchable interface to your users

## How to use
1. Add MapPagerView into your layout xml. It requires a type to do its job correctly:

  ```
  <com.cdk.bettermapsearch.MapPagerView
        android:id="@+id/map_pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
  ```
  MapPagerView<MyCustomModelClass> mapPagerView = findViewById(R.id.map_pager);
  ```
2. The class type that is given to MapPagerView will need to implement the `MapClusterItem` interface.
3. MapPagerView serves as a delegate for MapViews lifecycle methods, so you'll need to make sure to add all of the usual lifecycle methods that you use for MapView.
4. When the GoogleMap is ready, you'll receive a callback from MapPagerView (supplied to the `getMapAsync` method) that provides all of the fields needed to create your custom MarkerRenderer, which you give back to MapPagerView.
5. Use the `updateItems()` method to populate data on the map.

## Customizability
* The abstract class `CustomMarkerRenderer` controls the logic of when to show items & clusters as selected, you'll just need to implement the 4 abstract methods that handle the look of the markers
* The abstract class `MapPagerAdapter` controls the ViewPager logic, it requires 2 types: your data model class and the ViewHolder type
* Obviously you'll also need to create your own ViewHolder

TODO: 
- Build out example more
- Make clustering optional
- Replace Observable.create() with RxRelay
- Add to Maven
