package io.sharedstreets.tools.builder.osm.model;

import com.esri.core.geometry.Geometry;

public abstract class SpatialEntity extends AttributedEntity {

    public abstract Geometry constructGeometry();

}