package journeymap.client.properties;

import journeymap.common.properties.config.*;
import journeymap.common.properties.*;

public abstract class MapProperties extends ClientPropertiesBase implements Comparable<MapProperties>
{
    public final BooleanField showWaypoints;
    public final BooleanField showSelf;
    public final BooleanField showGrid;
    public final BooleanField showCaves;
    public final BooleanField showEntityNames;
    public final IntegerField zoomLevel;
    
    public MapProperties() {
        this.showWaypoints = new BooleanField(Category.Inherit, "jm.common.show_waypoints", true);
        this.showSelf = new BooleanField(Category.Inherit, "jm.common.show_self", true);
        this.showGrid = new BooleanField(Category.Inherit, "jm.common.show_grid", true);
        this.showCaves = new BooleanField(Category.Inherit, "jm.common.show_caves", true);
        this.showEntityNames = new BooleanField(Category.Inherit, "jm.common.show_entity_names", true);
        this.zoomLevel = new IntegerField(Category.Hidden, "", 0, 8, 0);
    }
    
    @Override
    public int compareTo(final MapProperties other) {
        return Integer.valueOf(this.hashCode()).compareTo(Integer.valueOf(other.hashCode()));
    }
}
