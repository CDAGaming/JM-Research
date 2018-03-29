package journeymap.server.events;

import journeymap.common.Journeymap;
import journeymap.common.network.PacketHandler;
import journeymap.common.network.model.InitLogin;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

public class ForgeEvents {
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void on(final EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            final Boolean hasForge = player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
            if (!hasForge) {
                Journeymap.getLogger().debug(player.getName() + " is connecting with a vanilla client, ignoring JoinWorldEvent");
                return;
            }
            Journeymap.getLogger().info(((EntityPlayerMP) event.getEntity()).getDisplayNameString() + " joining dimension " + event.getEntity().dimension);
            final DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(player.dimension);
            try {
                PermissionProperties prop;
                if (dimensionProperties.enabled.get()) {
                    prop = (DimensionProperties) dimensionProperties.clone();
                } else {
                    prop = (GlobalProperties) PropertiesManager.getInstance().getGlobalProperties().clone();
                }
                if (this.isOp(player)) {
                    prop.radarEnabled.set(prop.opRadarEnabled.get());
                    prop.caveMappingEnabled.set(prop.opCaveMappingEnabled.get());
                    prop.surfaceMappingEnabled.set(prop.opSurfaceMappingEnabled.get());
                    prop.topoMappingEnabled.set(prop.opTopoMappingEnabled.get());
                }
                PacketHandler.sendDimensionPacketToPlayer(player, prop);
            } catch (CloneNotSupportedException e) {
                Journeymap.getLogger().error("CloneNotSupportedException: ", e);
            }
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLoggedInEvent(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get()) {
                PacketHandler.sendPlayerWorldID((EntityPlayerMP) event.player);
            }
            final InitLogin init = new InitLogin();
            if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get()) {
                init.setTeleportEnabled(true);
            } else if (this.isOp((EntityPlayerMP) event.player)) {
                init.setTeleportEnabled(true);
            } else {
                init.setTeleportEnabled(false);
            }
            PacketHandler.sendLoginPacket((EntityPlayerMP) event.player, init);
        }
    }

    private boolean isOp(final EntityPlayerMP player) {
        final String[] getOppedPlayerNames;
        final String[] ops = getOppedPlayerNames = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayerNames();
        for (final String opName : getOppedPlayerNames) {
            if (player.getDisplayNameString().equalsIgnoreCase(opName)) {
                return true;
            }
        }
        return false;
    }
}
