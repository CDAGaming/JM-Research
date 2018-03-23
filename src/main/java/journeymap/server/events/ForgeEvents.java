package journeymap.server.events;

import net.minecraftforge.event.entity.*;
import net.minecraft.entity.player.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.common.*;
import journeymap.common.network.*;
import journeymap.server.properties.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.*;
import journeymap.common.network.model.*;
import net.minecraftforge.fml.server.*;

public class ForgeEvents
{
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void on(final EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
            final Boolean hasForge = (Boolean)player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
            if (!hasForge) {
                Journeymap.getLogger().debug(player.getName() + " is connecting with a vanilla client, ignoring JoinWorldEvent");
                return;
            }
            Journeymap.getLogger().info(((EntityPlayerMP)event.getEntity()).getDisplayNameString() + " joining dimension " + event.getEntity().dimension);
            final DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(player.dimension);
            try {
                PermissionProperties prop;
                if (dimensionProperties.enabled.get()) {
                    prop = (DimensionProperties)dimensionProperties.clone();
                }
                else {
                    prop = (GlobalProperties)PropertiesManager.getInstance().getGlobalProperties().clone();
                }
                if (this.isOp(player)) {
                    prop.radarEnabled.set(prop.opRadarEnabled.get());
                    prop.caveMappingEnabled.set(prop.opCaveMappingEnabled.get());
                    prop.surfaceMappingEnabled.set(prop.opSurfaceMappingEnabled.get());
                    prop.topoMappingEnabled.set(prop.opTopoMappingEnabled.get());
                }
                PacketHandler.sendDimensionPacketToPlayer(player, prop);
            }
            catch (CloneNotSupportedException e) {
                Journeymap.getLogger().error("CloneNotSupportedException: ", (Throwable)e);
            }
        }
    }
    
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLoggedInEvent(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            if (PropertiesManager.getInstance().getGlobalProperties().useWorldId.get()) {
                PacketHandler.sendPlayerWorldID((EntityPlayerMP)event.player);
            }
            final InitLogin init = new InitLogin();
            if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get()) {
                init.setTeleportEnabled(true);
            }
            else if (this.isOp((EntityPlayerMP)event.player)) {
                init.setTeleportEnabled(true);
            }
            else {
                init.setTeleportEnabled(false);
            }
            PacketHandler.sendLoginPacket((EntityPlayerMP)event.player, init);
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
