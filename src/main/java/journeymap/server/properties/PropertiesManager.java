package journeymap.server.properties;

import java.util.*;
import net.minecraftforge.common.*;

public class PropertiesManager
{
    private static PropertiesManager INSTANCE;
    private Map<Integer, DimensionProperties> dimensionProperties;
    private GlobalProperties globalProperties;
    
    public static PropertiesManager getInstance() {
        if (PropertiesManager.INSTANCE == null) {
            (PropertiesManager.INSTANCE = new PropertiesManager()).loadConfigs();
        }
        return PropertiesManager.INSTANCE;
    }
    
    private void loadConfigs() {
        this.dimensionProperties = new HashMap<Integer, DimensionProperties>();
        (this.globalProperties = new GlobalProperties()).load();
        for (final Integer dim : DimensionManager.getIDs()) {
            this.genConfig(dim);
        }
    }
    
    public DimensionProperties getDimProperties(final int dim) {
        if (this.dimensionProperties.get(dim) == null) {
            this.genConfig(dim);
        }
        return this.dimensionProperties.get(dim);
    }
    
    public GlobalProperties getGlobalProperties() {
        return this.globalProperties;
    }
    
    private void genConfig(final int dim) {
        final DimensionProperties prop = new DimensionProperties(dim);
        this.dimensionProperties.put(dim, prop);
        if (!prop.getFile().exists()) {
            prop.build();
        }
        prop.load();
    }
}
