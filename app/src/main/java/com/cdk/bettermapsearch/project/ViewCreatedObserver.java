package com.cdk.bettermapsearch.project;

import android.view.View;

import com.cdk.bettermapsearch.example.ui.MapFragment;
import com.cdk.bettermapsearch.project.interfaces.ViewCreatedCallback;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import rx.Observable;
import rx.Subscriber;

public class ViewCreatedObserver implements Observable.OnSubscribe<MapFragment.ViewCreatedEvent> {
    private CustomPagerAdapter adapter;
    private int viewPosition;
    private RecyclerViewPager viewPager;

    public ViewCreatedObserver(CustomPagerAdapter adapter, int position, RecyclerViewPager viewPager) {
        this.adapter = adapter;
        this.viewPosition = position;
        this.viewPager = viewPager;
    }

    @Override
    public void call(Subscriber<? super MapFragment.ViewCreatedEvent> subscriber) {
        ViewCreatedCallback callback = position -> {
            if (!subscriber.isUnsubscribed() && position == viewPosition) {
                subscriber.onNext(new MapFragment.ViewCreatedEvent());
            }
        };

        adapter.setCallback(viewPosition, callback);
        viewPager.setVisibility(View.INVISIBLE);
    }
}
