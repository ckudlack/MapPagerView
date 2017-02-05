package com.cdk.bettermapsearch.project.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface ViewPagerItem {

    LatLng getPosition();

    void setIndex(int index);

    int getIndex();
}
