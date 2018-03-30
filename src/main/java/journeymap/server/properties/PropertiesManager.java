package journeymap.server.properties;

import java.util.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.server.*;
import com.google.common.base.*;

public class PropertiesManager
{
    private static final Supplier<PropertiesManager> LAZYINIT;
    private final Map<Integer, DimensionProperties> dimensionProperties;
    private final Map<Integer, DimensionProperties> dimensionOpProperties;
    private final GlobalProperties globalProperties;
    private final GlobalProperties globalOpProperties;
    
    public static PropertiesManager getInstance() {
        return (PropertiesManager)PropertiesManager.LAZYINIT.get();
    }
    
    private PropertiesManager() {
        this.dimensionProperties = new HashMap<Integer, DimensionProperties>();
        this.dimensionOpProperties = new HashMap<Integer, DimensionProperties>();
        this.globalOpProperties = new GlobalProperties(true);
        (this.globalProperties = new GlobalProperties(false)).load();
        for (final Integer dim : DimensionManager.getIDs()) {
            this.loadConfig(dim, false);
        }
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server.func_71262_S()) {
            this.globalOpProperties.load();
            for (final Integer dim2 : DimensionManager.getIDs()) {
                this.loadConfig(dim2, true);
            }
        }
    }
    
    public DimensionProperties getDimProperties(final int dim, final boolean isOp) {
        final DimensionProperties dimProps = this.getDimPropertiesMap(isOp).get(dim);
        if (dimProps == null) {
            return this.loadConfig(dim, isOp);
        }
        return dimProps;
    }
    
    public GlobalProperties getGlobalProperties(final boolean isOp) {
        return isOp ? this.globalOpProperties : this.globalProperties;
    }
    
    private Map<Integer, DimensionProperties> getDimPropertiesMap(final boolean isOp) {
        return isOp ? this.dimensionOpProperties : this.dimensionProperties;
    }
    
    private DimensionProperties loadConfig(final int dim, final boolean isOp) {
        final DimensionProperties prop = new DimensionProperties(dim, isOp);
        this.getDimPropertiesMap(isOp).put(dim, prop);
        if (!prop.getFile().exists()) {
            prop.build(this.getGlobalProperties(isOp));
        }
        prop.load();
        return prop;
    }
    
    static {
        LAZYINIT = Suppliers.memoize(PropertiesManager::new);
    }
}
