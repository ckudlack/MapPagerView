package com.cdk.bettermapsearch;

import android.view.View;

import com.cdk.bettermapsearch.interfaces.ViewCreatedCallback;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import rx.Observable;
import rx.Subscriber;

/**
 * Custom Observer that will fire a subscriber when all views are drawn the in the ViewPager
 */
public class ViewCreatedObserver implements Observable.OnSubscribe<Void> {
    private MapPagerAdapter adapter;
    private int viewPosition;
    private RecyclerViewPager viewPager;

    public ViewCreatedObserver(MapPagerAdapter adapter, int position, RecyclerViewPager viewPager) {
        this.adapter = adapter;
        this.viewPosition = position;
        this.viewPager = viewPager;
    }

    @Override
    public void call(Subscriber<? super Void> subscriber) {
        ViewCreatedCallback callback = position -> {
            if (!subscriber.isUnsubscribed() && position == viewPosition) {
                subscriber.onNext(null);
            }
        };

        adapter.setCallback(viewPosition, callback);
        viewPager.setVisibility(View.INVISIBLE);
    }
}
