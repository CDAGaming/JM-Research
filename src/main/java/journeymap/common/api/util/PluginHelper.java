package journeymap.common.api.util;

import javax.annotation.*;
import net.minecraftforge.fml.common.discovery.*;
import com.google.common.base.*;
import java.util.*;
import journeymap.common.api.*;
import org.apache.logging.log4j.*;

@ParametersAreNonnullByDefault
public abstract class PluginHelper<A, I extends IJmPlugin>
{
    public static final Logger LOGGER;
    private final Class<A> pluginAnnotationClass;
    private final Class<I> pluginInterfaceClass;
    protected Map<String, I> plugins;
    protected boolean initialized;
    
    protected PluginHelper(final Class<A> pluginAnnotationClass, final Class<I> pluginInterfaceClass) {
        this.plugins = null;
        this.pluginAnnotationClass = pluginAnnotationClass;
        this.pluginInterfaceClass = pluginInterfaceClass;
    }
    
    public Map<String, I> preInitPlugins(final ASMDataTable asmDataTable) {
        if (this.plugins == null) {
            final HashMap<String, I> discovered = new HashMap<String, I>();
            final Set<ASMDataTable.ASMData> asmDataSet = (Set<ASMDataTable.ASMData>)asmDataTable.getAll(this.pluginAnnotationClass.getCanonicalName());
            final String pluginAnnotationName = this.pluginAnnotationClass.getCanonicalName();
            final String pluginInterfaceName = this.pluginInterfaceClass.getSimpleName();
            for (final ASMDataTable.ASMData asmData : asmDataSet) {
                final String className = asmData.getClassName();
                try {
                    final Class<?> pluginClass = Class.forName(className);
                    if (this.pluginInterfaceClass.isAssignableFrom(pluginClass)) {
                        final Class<I> interfaceImplClass = (Class<I>)pluginClass.asSubclass(this.pluginInterfaceClass);
                        final I instance = interfaceImplClass.newInstance();
                        final String modId = instance.getModId();
                        if (Strings.isNullOrEmpty(modId)) {
                            throw new Exception("IPlugin.getModId() must return a non-empty, non-null value");
                        }
                        if (discovered.containsKey(modId)) {
                            final Class otherPluginClass = discovered.get(modId).getClass();
                            throw new Exception(String.format("Multiple plugins trying to use the same modId: %s and %s", interfaceImplClass, otherPluginClass));
                        }
                        discovered.put(modId, instance);
                        PluginHelper.LOGGER.info(String.format("Found @%s: %s", pluginAnnotationName, className));
                    }
                    else {
                        PluginHelper.LOGGER.error(String.format("Found @%s: %s, but it doesn't implement %s", pluginAnnotationName, className, pluginInterfaceName));
                    }
                }
                catch (Exception e) {
                    PluginHelper.LOGGER.error(String.format("Found @%s: %s, but failed to instantiate it: %s", pluginAnnotationName, className, e.getMessage()), (Throwable)e);
                }
            }
            if (discovered.isEmpty()) {
                PluginHelper.LOGGER.info("No plugins for JourneyMap API discovered.");
            }
            this.plugins = Collections.unmodifiableMap((Map<? extends String, ? extends I>)discovered);
        }
        return this.plugins;
    }
    
    public Map<String, I> initPlugins(final IJmAPI jmApi) {
        if (this.plugins == null) {
            PluginHelper.LOGGER.warn("Plugin discovery never occurred.", (Throwable)new IllegalStateException());
        }
        else if (!this.initialized) {
            PluginHelper.LOGGER.info(String.format("Initializing plugins with %s", jmApi.getClass().getName()));
            final HashMap<String, I> discovered = new HashMap<String, I>((Map<? extends String, ? extends I>)this.plugins);
            final Iterator<I> iter = discovered.values().iterator();
            while (iter.hasNext()) {
                final I plugin = iter.next();
                try {
                    plugin.initialize(jmApi);
                    PluginHelper.LOGGER.info(String.format("Initialized %s: %s", this.pluginInterfaceClass.getSimpleName(), plugin.getClass().getName()));
                }
                catch (Exception e) {
                    PluginHelper.LOGGER.error("Failed to initialize I: " + plugin.getClass().getName(), (Throwable)e);
                    iter.remove();
                }
            }
            this.plugins = Collections.unmodifiableMap((Map<? extends String, ? extends I>)discovered);
            this.initialized = true;
        }
        else {
            PluginHelper.LOGGER.info("Plugins already initialized.");
        }
        return this.plugins;
    }
    
    public Map<String, I> getPlugins() {
        return this.plugins;
    }
    
    static {
        LOGGER = LogManager.getLogger("journeymap");
    }
}
