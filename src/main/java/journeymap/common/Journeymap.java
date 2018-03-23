package journeymap.common;

import journeymap.common.version.*;
import net.minecraftforge.fml.common.*;
import org.apache.logging.log4j.*;
import java.util.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.server.properties.*;
import journeymap.common.command.*;
import net.minecraft.command.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.*;
import journeymap.client.*;
import journeymap.server.*;

@Mod(modid = "journeymap", name = "JourneyMap", version = "1.12.2-5.5.2", canBeDeactivated = true, guiFactory = "journeymap.client.ui.dialog.OptionsGuiFactory", dependencies = "required-after:Forge@[${14.23.0.2491},)", acceptedMinecraftVersions = "[1.12.2]")
public class Journeymap
{
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";
    public static final Version JM_VERSION;
    public static final String FORGE_VERSION = "14.23.0.2491";
    public static final String MC_VERSION = "1.12.2";
    public static final String WEBSITE_URL = "http://journeymap.info/";
    public static final String PATREON_URL = "http://patreon.com/techbrew";
    public static final String DOWNLOAD_URL = "http://minecraft.curseforge.com/projects/journeymap/files/";
    public static final String VERSION_URL = "http://widget.mcf.li/mc-mods/minecraft/journeymap.json";
    @Mod.Instance("journeymap")
    public static Journeymap instance;
    @SidedProxy(clientSide = "journeymap.client.JourneymapClient", serverSide = "journeymap.server.JourneymapServer")
    public static CommonProxy proxy;
    
    public static Logger getLogger() {
        return LogManager.getLogger("journeymap");
    }
    
    @NetworkCheckHandler
    public boolean checkModLists(final Map<String, String> modList, final Side side) {
        return Journeymap.proxy == null || Journeymap.proxy.checkModLists(modList, side);
    }
    
    @Mod.EventHandler
    public void preInitialize(final FMLPreInitializationEvent event) throws Throwable {
        Journeymap.proxy.preInitialize(event);
    }
    
    @Mod.EventHandler
    public void initialize(final FMLInitializationEvent event) throws Throwable {
        Journeymap.proxy.initialize(event);
    }
    
    @Mod.EventHandler
    public void postInitialize(final FMLPostInitializationEvent event) throws Throwable {
        Journeymap.proxy.postInitialize(event);
    }
    
    @Mod.EventHandler
    public void serverStartingEvent(final FMLServerStartingEvent event) {
        PropertiesManager.getInstance();
        event.registerServerCommand((ICommand)new CommandJTP());
    }
    
    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverStartedEvent(final FMLServerStartedEvent event) {
    }
    
    @SideOnly(Side.CLIENT)
    public static JourneymapClient getClient() {
        return (JourneymapClient)Journeymap.proxy;
    }
    
    @SideOnly(Side.SERVER)
    public static JourneymapServer getServer() {
        return (JourneymapServer)Journeymap.proxy;
    }
    
    static {
        JM_VERSION = Version.from("5", "5", "2", "", new Version(5, 5, 0, "dev"));
    }
}
