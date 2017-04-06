package com.cdk.bettermapsearch

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.cdk.bettermapsearch.interfaces.MapClusterItem
import com.cdk.bettermapsearch.interfaces.ViewCreatedCallback
import com.google.android.gms.maps.model.LatLng

/**
 * This class encapsulates the handling of the list that backs both the ViewPager and the marker clustering
 * It also handles the callbacks that are required for the ViewPager item translation animations
 *
 * @param <VH> The class type of your custom ViewHolder
 */
@Suppress("unused")
abstract class MapPagerAdapter<T : MapClusterItem, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var backingList: List<T> = listOf()
    var callbackMap: SparseArray<ViewCreatedCallback> = SparseArray(3)

    abstract override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH

    @CallSuper
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.y = 0f
        holder.itemView.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return backingList.size
    }

    @CallSuper
    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)

        holder.itemView.visibility = View.VISIBLE

        val position = holder.adapterPosition
        val viewCreatedCallback = callbackMap.get(position)
        viewCreatedCallback?.viewCreated(position)
    }

    fun setCallback(position: Int, callback: ViewCreatedCallback) {
        callbackMap.put(position, callback)
    }

    fun clearCallbacks() = callbackMap.clear()

    fun getItemPositionOnMap(index: Int): LatLng {
        return backingList[index].position
    }

    fun updateItems(items: List<T>) {
        backingList = items
    }

    fun getPositionOfItem(item: T?): Int = backingList.indexOf(item)

    fun getItemAtPosition(position: Int): T = backingList[position]
}
