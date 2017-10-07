package io.sharedstreets.tools.builder.model;


import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polyline;
import io.sharedstreets.data.osm.model.NodePosition;
import io.sharedstreets.data.osm.model.SpatialEntity;
import io.sharedstreets.data.osm.model.Way;
import org.apache.commons.lang3.ArrayUtils;

import java.util.UUID;

public class BaseSegment extends SpatialEntity {

    public WaySection[] waySections;
    public boolean oneWay;
    public boolean link;
    public boolean roundabout;

    public BaseSegment(WaySection section) {

        // TODO switch to UniqueID to avoid collisions
        id = UUID.randomUUID().getLeastSignificantBits();
        waySections = new WaySection[1];
        waySections[0] = section;
        oneWay = section.oneWay;
        link = section.link;
        roundabout = section.roundabout;

    }

    public static boolean canMerge(BaseSegment baseSegment1, BaseSegment baseSegment2) {

        // can't merge oneway with non-onway
        if(baseSegment1.oneWay != baseSegment2.oneWay)
            return false;

        // can't merge link roads with non-link roads
        if(baseSegment1.link != baseSegment2.link)
            return false;

        // can't merge roundabouts  with non-roundabouts
        if(baseSegment1.roundabout != baseSegment2.roundabout)
            return false;

        // check for duplicates-- need to catch circular segments (they appear mergable)
        if(baseSegment1.waySections.length == baseSegment2.waySections.length) {
            boolean duplicate = true;

            for(int i = 0; i < baseSegment1.waySections.length; i++) {
                if(     !baseSegment1.waySections[i].wayId.equals(baseSegment2.waySections[i].wayId) &&
                        !baseSegment1.waySections[i].nodes[0].nodeId.equals(baseSegment2.waySections[i].nodes[0].nodeId) &&
                        !baseSegment1.waySections[i].nodes[baseSegment1.waySections[i].nodes.length - 1].nodeId.equals(baseSegment2.waySections[i].nodes[baseSegment2.waySections[i].nodes.length - 1].nodeId)) {
                    duplicate = false;
                }
            }
            //can't merge duplicates
            if(duplicate)
                 return false;
        }

        return true;
    }

    public static BaseSegment merge(BaseSegment baseSegment1, BaseSegment baseSegment2) {

        if(canMerge(baseSegment1, baseSegment2)) {
            if (baseSegment2.getFirstNode().equals(baseSegment1.getLastNode())) {
                baseSegment1.append(baseSegment2);
                baseSegment1.id = UUID.randomUUID().getLeastSignificantBits();
                return baseSegment1;
            } else if (baseSegment1.getFirstNode().equals(baseSegment2.getLastNode())) {
                baseSegment2.append(baseSegment1);
                baseSegment2.id = UUID.randomUUID().getLeastSignificantBits();
                return baseSegment2;
            }
        }

        return null;
    }

    public void append(BaseSegment baseSegment) {
        this.waySections = ArrayUtils.addAll(this.waySections, baseSegment.waySections);
    }

    public Long getFirstNode() {
        return waySections[0].nodes[0].nodeId;
    }

    public Long getLastNode() {
        return waySections[waySections.length - 1].nodes[waySections[waySections.length - 1].nodes.length -1].nodeId;
    }

    public String getWayIds() {
        String id[] = new String[this.waySections.length];

        int i = 0;
        for(WaySection waySection : this.waySections) {
            id[i] = waySection.wayId + "";
            i++;
        }
        return String.join( ",", id);
    }

    @Override
    public Geometry constructGeometry() {

        Polyline line = new Polyline();

        boolean firstPosition = true;

        for(WaySection section : this.waySections) {
            for(NodePosition node : section.nodes) {
                if(firstPosition == true) {
                    line.startPath(node.lon, node.lat);
                    firstPosition = false;
                }
                else
                    line.lineTo(node.lon, node.lat);
            }
        }



        return line;
    }
}