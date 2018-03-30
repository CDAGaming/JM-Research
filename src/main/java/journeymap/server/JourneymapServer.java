package journeymap.server;

import org.apache.logging.log4j.*;
import journeymap.common.version.*;
import journeymap.common.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.discovery.*;
import journeymap.server.properties.*;
import journeymap.server.api.util.*;
import net.minecraftforge.common.*;
import journeymap.server.events.*;
import journeymap.common.network.*;
import journeymap.common.migrate.*;
import journeymap.server.api.impl.*;
import journeymap.common.api.*;
import net.minecraftforge.fml.common.event.*;
import journeymap.common.command.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.*;
import java.util.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.server.*;
import net.minecraft.server.management.*;

public class JourneymapServer implements CommonProxy
{
    private Logger logger;
    public static boolean DEV_MODE;
    private static final Version MINIMUM_ACCEPTABLE_VERSION;
    
    public JourneymapServer() {
        this.logger = Journeymap.getLogger();
    }
    
    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    @Override
    public void preInitialize(final FMLPreInitializationEvent event) {
        preInitialize(event.getAsmData());
    }
    
    public static void preInitialize(final ASMDataTable asmDataTable) {
        try {
            PropertiesManager.getInstance();
            ServerPluginHelper.instance().preInitPlugins(asmDataTable);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    @Override
    public void initialize(final FMLInitializationEvent event) {
        initialize();
    }
    
    public static void initialize() {
        MinecraftForge.EVENT_BUS.register((Object)ForgeEvents.INSTANCE);
        PacketHandler.init(Side.SERVER);
        final boolean migrationOk = new Migration("journeymap.server.task.migrate").performTasks();
        PropertiesManager.getInstance();
        ServerPluginHelper.instance().initPlugins(ServerAPI.INSTANCE);
    }
    
    @SideOnly(Side.SERVER)
    @Override
    public void postInitialize(final FMLPostInitializationEvent event) {
    }
    
    @Override
    public void serverStartingEvent(final FMLServerStartingEvent event) throws Throwable {
        event.registerServerCommand((ICommand)new CommandJTP());
    }
    
    @Override
    public boolean checkModLists(final Map<String, String> modList, final Side side) {
        for (final String s : modList.keySet()) {
            if (s.toLowerCase().startsWith("journeymap")) {
                if (modList.get(s).contains("@")) {
                    this.logger.info("Mod check = dev environment");
                    return JourneymapServer.DEV_MODE = true;
                }
                final String version = modList.get(s).split("-")[1];
                final Version userLoggedInVersion = Version.from(version, null);
                if (JourneymapServer.MINIMUM_ACCEPTABLE_VERSION.isNewerThan(userLoggedInVersion)) {
                    this.logger.info("Version Mismatch need " + JourneymapServer.MINIMUM_ACCEPTABLE_VERSION.toString() + " or higher. Current version attempt -> " + modList.get(s));
                    return false;
                }
                return true;
            }
        }
        return true;
    }
    
    @Override
    public boolean isUpdateCheckEnabled() {
        return false;
    }
    
    @Override
    public void handleWorldIdMessage(final String message, final EntityPlayerMP playerEntity) {
        final boolean isOp = isOp(playerEntity.func_110124_au());
        if (PropertiesManager.getInstance().getGlobalProperties(isOp).useWorldId.get()) {
            PacketHandler.sendPlayerWorldID(playerEntity);
        }
    }
    
    public static boolean isOp(final UUID playerID) {
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server.func_71262_S()) {
            final PlayerList playerList = server.func_184103_al();
            final EntityPlayerMP player = playerList.func_177451_a(playerID);
            return player != null && playerList.func_152603_m().func_152683_b((Object)player.func_146103_bH()) != null;
        }
        return false;
    }
    
    static {
        JourneymapServer.DEV_MODE = false;
        MINIMUM_ACCEPTABLE_VERSION = Journeymap.JM_VERSION;
    }
}
