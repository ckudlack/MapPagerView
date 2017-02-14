package com.cdk.bettermapsearch.example.models;

import com.cdk.bettermapsearch.project.clustering.MapClusterItem;

/*
    Test model representing what you might get from the server
 */
public class LatLngModel extends MapClusterItem {

    private String title;
    private int price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapClusterItem that = (MapClusterItem) o;
        return position.equals(that.getPosition());

    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
