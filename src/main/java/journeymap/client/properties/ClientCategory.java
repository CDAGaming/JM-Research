package journeymap.client.properties;

import journeymap.client.Constants;
import journeymap.common.properties.Category;

import java.util.Arrays;
import java.util.List;

public class ClientCategory {
    public static final Category MiniMap1;
    public static final Category MiniMap2;
    public static final Category FullMap;
    public static final Category WebMap;
    public static final Category Waypoint;
    public static final Category WaypointBeacon;
    public static final Category Cartography;
    public static final Category Advanced;
    public static final List<Category> values;
    private static int order;

    static {
        ClientCategory.order = 1;
        MiniMap1 = create("MiniMap1", "jm.config.category.minimap");
        MiniMap2 = create("MiniMap2", "jm.config.category.minimap2");
        FullMap = create("FullMap", "jm.config.category.fullmap");
        WebMap = create("WebMap", "jm.config.category.webmap");
        Waypoint = create("Waypoint", "jm.config.category.waypoint");
        WaypointBeacon = create("WaypointBeacon", "jm.config.category.waypoint_beacons");
        Cartography = create("Cartography", "jm.config.category.cartography");
        Advanced = create("Advanced", "jm.config.category.advanced");
        values = Arrays.asList(Category.Inherit, Category.Hidden, ClientCategory.MiniMap1, ClientCategory.MiniMap2, ClientCategory.FullMap, ClientCategory.WebMap, ClientCategory.Waypoint, ClientCategory.WaypointBeacon, ClientCategory.Cartography, ClientCategory.Advanced);
    }

    private static Category create(final String name, final String key) {
        return new Category(name, ClientCategory.order++, Constants.getString(key), Constants.getString(key + ".tooltip"));
    }

    public static Category valueOf(final String name) {
        for (final Category category : ClientCategory.values) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
}
