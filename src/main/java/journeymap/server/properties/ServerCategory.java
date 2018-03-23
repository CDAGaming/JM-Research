package journeymap.server.properties;

import journeymap.common.properties.*;
import java.util.*;

public class ServerCategory
{
    private static int order;
    public static final Category General;
    public static final Category Radar;
    public static final Category Cave;
    public static final Category Surface;
    public static final Category Topo;
    public static final List<Category> values;
    
    public static Category valueOf(final String name) {
        for (final Category category : ServerCategory.values) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
    
    private static Category create(final String name, final String label) {
        return create(name, label, null);
    }
    
    private static Category create(final String name, final String label, final String tooltip) {
        return new Category(name, ServerCategory.order++, label, tooltip);
    }
    
    static {
        ServerCategory.order = 1;
        General = create("General", "General Configuration");
        Radar = create("Radar", "Radar Features");
        Cave = create("Cave", "Cave Mapping");
        Surface = create("Surface", "Surface Mapping");
        Topo = create("Topo", "Topo Mapping");
        values = Arrays.asList(Category.Inherit, Category.Hidden, ServerCategory.General, ServerCategory.Radar, ServerCategory.Cave, ServerCategory.Surface, ServerCategory.Topo);
    }
}
