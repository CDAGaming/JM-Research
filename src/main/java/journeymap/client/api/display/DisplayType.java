package journeymap.client.api.display;

import java.util.HashMap;

public enum DisplayType {
    Image(ImageOverlay.class),
    Marker(MarkerOverlay.class),
    Polygon(PolygonOverlay.class),
    Waypoint(Waypoint.class),
    WaypointGroup(WaypointGroup.class);

    private static HashMap<Class<? extends Displayable>, DisplayType> reverseLookup;
    private final Class<? extends Displayable> implClass;

    private DisplayType(final Class<? extends Displayable> implClass) {
        this.implClass = implClass;
    }

    public static DisplayType of(final Class<? extends Displayable> implClass) {
        if (DisplayType.reverseLookup == null) {
            DisplayType.reverseLookup = new HashMap<>();
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
