package journeymap.server;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.common.*;
import journeymap.server.events.*;
import journeymap.common.network.*;
import net.minecraftforge.fml.common.event.*;
import java.util.*;
import net.minecraft.entity.player.*;
import journeymap.server.properties.*;

public class JourneymapServer implements CommonProxy
{
    private Logger logger;
    public static boolean DEV_MODE;
    
    public JourneymapServer() {
        this.logger = Journeymap.getLogger();
    }
    
    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    @Override
    public void preInitialize(final FMLPreInitializationEvent event) {
    }
    
    @Override
    public void initialize(final FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register((Object)new ForgeEvents());
        PacketHandler.init(Side.SERVER);
    }
    
    @SideOnly(Side.SERVER)
    @Override
    public void postInitialize(final FMLPostInitializationEvent event) {
    }
    
    @Override
    public boolean checkModLists(final Map<String, String> modList, final Side side) {
        this.logger.info(side.toString());
        for (final String s : modList.keySet()) {
            if ("journeymap".equalsIgnoreCase(s)) {
                if (modList.get(s).contains("@")) {
                    this.logger.info("Mod check = dev environment");
                    return JourneymapServer.DEV_MODE = true;
                }
                final String[] version = modList.get(s).split("-")[1].split("\\.");
                final int major = Integer.parseInt(version[0]);
                final int minor = Integer.parseInt(version[1]);
                if (major >= 5 && minor >= 3) {
                    return true;
                }
                this.logger.info("Version Mismatch need 5.3.0 or higher. Current version attempt -> " + modList.get(s));
                return false;
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
        if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get()) {
            PacketHandler.sendPlayerWorldID(playerEntity);
        }
    }
    
    static {
        JourneymapServer.DEV_MODE = false;
    }
}
