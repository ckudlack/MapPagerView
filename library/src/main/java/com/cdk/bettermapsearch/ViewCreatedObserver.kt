package com.cdk.bettermapsearch

import android.view.View

import com.cdk.bettermapsearch.interfaces.ViewCreatedCallback
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager

import rx.Observable
import rx.Subscriber

/**
 * Custom Observer that will fire a subscriber when all views are drawn the in the ViewPager
 */
class ViewCreatedObserver(private val adapter: MapPagerAdapter<*, *>, private val viewPosition: Int, private val viewPager: RecyclerViewPager) : Observable.OnSubscribe<Void> {

    override fun call(subscriber: Subscriber<in Void>) {
        val callback = object : ViewCreatedCallback {
            override fun viewCreated(position: Int) {
                if (!subscriber.isUnsubscribed && position == viewPosition) {
                    subscriber.onNext(null)
                }
            }
        }

        adapter.setCallback(viewPosition, callback)
        viewPager.visibility = View.INVISIBLE
    }
}
