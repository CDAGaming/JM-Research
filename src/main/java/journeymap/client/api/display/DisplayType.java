package journeymap.client.api.display;

import java.util.*;

public enum DisplayType
{
    Image((Class<? extends Displayable>)ImageOverlay.class), 
    Marker((Class<? extends Displayable>)MarkerOverlay.class), 
    Polygon((Class<? extends Displayable>)PolygonOverlay.class), 
    Waypoint((Class<? extends Displayable>)Waypoint.class), 
    WaypointGroup((Class<? extends Displayable>)WaypointGroup.class);
    
    private static HashMap<Class<? extends Displayable>, DisplayType> reverseLookup;
    private final Class<? extends Displayable> implClass;
    
    private DisplayType(final Class<? extends Displayable> implClass) {
        this.implClass = implClass;
    }
    
    public static DisplayType of(final Class<? extends Displayable> implClass) {
        if (DisplayType.reverseLookup == null) {
            DisplayType.reverseLookup = new HashMap<Class<? extends Displayable>, DisplayType>();
            for (final DisplayType type : values()) {
                DisplayType.reverseLookup.put(type.getImplClass(), type);
            }
        }
        final DisplayType displayType = DisplayType.reverseLookup.get(implClass);
        if (displayType == null) {
            throw new IllegalArgumentException("Not a valid Displayable implementation: " + implClass);
        }
        return displayType;
    }
    
    public Class<? extends Displayable> getImplClass() {
        return this.implClass;
    }
}
