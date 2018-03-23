package journeymap.client.properties;

import journeymap.common.properties.config.*;

public class WaypointProperties extends ClientPropertiesBase implements Comparable<WaypointProperties>
{
    public final BooleanField managerEnabled;
    public final BooleanField beaconEnabled;
    public final BooleanField showTexture;
    public final BooleanField showStaticBeam;
    public final BooleanField showRotatingBeam;
    public final BooleanField showName;
    public final BooleanField showDistance;
    public final BooleanField autoHideLabel;
    public final BooleanField boldLabel;
    public final IntegerField fontScale;
    public final BooleanField textureSmall;
    public final IntegerField maxDistance;
    public final IntegerField minDistance;
    public final BooleanField createDeathpoints;
    
    public WaypointProperties() {
        this.managerEnabled = new BooleanField(ClientCategory.Waypoint, "jm.waypoint.enable_manager", true, true);
        this.beaconEnabled = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.enable_beacons", true, true);
        this.showTexture = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.show_texture", true);
        this.showStaticBeam = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.show_static_beam", true);
        this.showRotatingBeam = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.show_rotating_beam", true);
        this.showName = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.show_name", true);
        this.showDistance = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.show_distance", true);
        this.autoHideLabel = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.auto_hide_label", true);
        this.boldLabel = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.bold_label", false);
        this.fontScale = new IntegerField(ClientCategory.WaypointBeacon, "jm.waypoint.font_scale", 1, 3, 2);
        this.textureSmall = new BooleanField(ClientCategory.WaypointBeacon, "jm.waypoint.texture_size", true);
        this.maxDistance = new IntegerField(ClientCategory.Waypoint, "jm.waypoint.max_distance", 0, 10000, 0);
        this.minDistance = new IntegerField(ClientCategory.WaypointBeacon, "jm.waypoint.min_distance", 0, 64, 4);
        this.createDeathpoints = new BooleanField(ClientCategory.Waypoint, "jm.waypoint.create_deathpoints", true);
    }
    
    @Override
    public String getName() {
        return "waypoint";
    }
    
    @Override
    public int compareTo(final WaypointProperties other) {
        return Integer.valueOf(this.hashCode()).compareTo(Integer.valueOf(other.hashCode()));
    }
}
