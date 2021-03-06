package journeymap.client.api.util;

import com.google.common.base.Strings;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public enum PluginHelper {
    INSTANCE;

    public static final Logger LOGGER;
    public static final String PLUGIN_ANNOTATION_NAME;
    public static final String PLUGIN_INTERFACE_NAME;

    static {
        LOGGER = LogManager.getLogger("journeymap");
        PLUGIN_ANNOTATION_NAME = ClientPlugin.class.getCanonicalName();
        PLUGIN_INTERFACE_NAME = IClientPlugin.class.getSimpleName();
    }

    protected Map<String, IClientPlugin> plugins;
    protected boolean initialized;

    private PluginHelper() {
        this.plugins = null;
    }

    public Map<String, IClientPlugin> preInitPlugins(final FMLPreInitializationEvent event) {
        if (this.plugins == null) {
            final ASMDataTable asmDataTable = event.getAsmData();
            final HashMap<String, IClientPlugin> discovered = new HashMap<>();
            final Set<ASMDataTable.ASMData> asmDataSet = asmDataTable.getAll(PluginHelper.PLUGIN_ANNOTATION_NAME);
            for (final ASMDataTable.ASMData asmData : asmDataSet) {
                final String className = asmData.getClassName();
                try {
                    final Class<?> pluginClass = Class.forName(className);
                    if (IClientPlugin.class.isAssignableFrom(pluginClass)) {
                        final Class<? extends IClientPlugin> interfaceImplClass = pluginClass.asSubclass(IClientPlugin.class);
                        final IClientPlugin instance = interfaceImplClass.newInstance();
                        final String modId = instance.getModId();
                        if (Strings.isNullOrEmpty(modId)) {
                            throw new Exception("IClientPlugin.getModId() must return a non-empty, non-null value");
                        }
                        if (discovered.containsKey(modId)) {
                            final Class otherPluginClass = discovered.get(modId).getClass();
                            throw new Exception(String.format("Multiple plugins trying to use the same modId: %s and %s", interfaceImplClass, otherPluginClass));
                        }
                        discovered.put(modId, instance);
                        PluginHelper.LOGGER.info(String.format("Found @%s: %s", PluginHelper.PLUGIN_ANNOTATION_NAME, className));
                    } else {
                        PluginHelper.LOGGER.error(String.format("Found @%s: %s, but it doesn't implement %s", PluginHelper.PLUGIN_ANNOTATION_NAME, className, PluginHelper.PLUGIN_INTERFACE_NAME));
                    }
                } catch (Exception e) {
                    PluginHelper.LOGGER.error(String.format("Found @%s: %s, but failed to instantiate it: %s", PluginHelper.PLUGIN_ANNOTATION_NAME, className, e.getMessage()), e);
                }
            }
            if (discovered.isEmpty()) {
                PluginHelper.LOGGER.info("No plugins for JourneyMap API discovered.");
            }
            this.plugins = Collections.unmodifiableMap(discovered);
        }
        return this.plugins;
    }

    public Map<String, IClientPlugin> initPlugins(final FMLInitializationEvent event, final IClientAPI clientAPI) {
        if (this.plugins == null) {
            PluginHelper.LOGGER.warn("Plugin discovery never occurred.", new IllegalStateException());
        } else if (!this.initialized) {
            PluginHelper.LOGGER.info(String.format("Initializing plugins with Client API: %s", clientAPI.getClass().getName()));
            final HashMap<String, IClientPlugin> discovered = new HashMap<>(this.plugins);
            final Iterator<IClientPlugin> iter = discovered.values().iterator();
            while (iter.hasNext()) {
                final IClientPlugin plugin = iter.next();
                try {
                    plugin.initialize(clientAPI);
                    PluginHelper.LOGGER.info(String.format("Initialized %s: %s", PluginHelper.PLUGIN_INTERFACE_NAME, plugin.getClass().getName()));
                } catch (Exception e) {
                    PluginHelper.LOGGER.error("Failed to initialize IClientPlugin: " + plugin.getClass().getName(), e);
                    iter.remove();
                }
            }
            this.plugins = Collections.unmodifiableMap(discovered);
            this.initialized = true;
        } else {
            PluginHelper.LOGGER.warn("Plugins already initialized!", new IllegalStateException());
        }
        return this.plugins;
    }

    public Map<String, IClientPlugin> getPlugins() {
        return this.plugins;
    }
}
