package journeymap.server;

import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.network.PacketHandler;
import journeymap.server.events.ForgeEvents;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class JourneymapServer implements CommonProxy {
    public static boolean DEV_MODE;

    static {
        JourneymapServer.DEV_MODE = false;
    }

    private Logger logger;

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
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
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
}
