package journeymap.server.properties;

import journeymap.common.properties.*;
import java.util.*;

public class ServerCategory
{
    private static int order;
    public static final Category General;
    public static final Category FeatureAction;
    public static final Category FeatureDisplay;
    public static final Category FeatureMaptype;
    public static final Category FeatureRadar;
    public static final List<Category> values;
    
    private static Category create(final String name, final String key) {
        return new Category(name, ServerCategory.order++, key, key + ".tooltip");
    }
    
    public static Category valueOf(final String name) {
        for (final Category category : ServerCategory.values) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
    
    static {
        ServerCategory.order = 1;
        General = create("General", "jm.config.category.general");
        FeatureAction = create("Action", "jm.common.feature.action");
        FeatureDisplay = create("Display", "jm.common.feature.display");
        FeatureMaptype = create("MapType", "jm.common.feature.maptype");
        FeatureRadar = create("Radar", "jm.common.feature.radar");
        values = Arrays.asList(Category.Inherit, Category.Hidden, ServerCategory.General, ServerCategory.FeatureAction, ServerCategory.FeatureDisplay, ServerCategory.FeatureMaptype, ServerCategory.FeatureRadar);
    }
}
