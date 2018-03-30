package journeymap.common.network;

import net.minecraftforge.fml.relauncher.*;
import journeymap.common.network.model.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.*;
import journeymap.common.feature.*;
import journeymap.server.nbt.*;
import journeymap.common.*;
import net.minecraftforge.fml.common.network.*;

public class PacketHandler
{
    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL;
    public static final SimpleNetworkWrapper DIMENSION_POLICIES_CHANNEL;
    public static final SimpleNetworkWrapper TELEPORT_CHANNEL;
    
    public static void init(final Side side) {
        PacketHandler.WORLD_INFO_CHANNEL.registerMessage((Class)WorldIDPacket.WorldIdListener.class, (Class)WorldIDPacket.class, 0, side);
        PacketHandler.TELEPORT_CHANNEL.registerMessage((Class)TeleportPacket.Listener.class, (Class)TeleportPacket.class, 0, Side.SERVER);
        if (Side.SERVER == side) {}
        if (Side.CLIENT == side) {
            PacketHandler.DIMENSION_POLICIES_CHANNEL.registerMessage((Class)DimensionPoliciesPacket.Listener.class, (Class)DimensionPoliciesPacket.class, 0, side);
        }
    }
    
    public static void teleportPlayer(final Location location) {
        PacketHandler.TELEPORT_CHANNEL.sendToServer((IMessage)new TeleportPacket(location));
    }
    
    public static void sendDimensionPolicyPacketToPlayer(final EntityPlayerMP player, final DimensionPolicies dimPolicies, final boolean compact) {
        if (player != null) {
            PacketHandler.DIMENSION_POLICIES_CHANNEL.sendTo((IMessage)new DimensionPoliciesPacket(dimPolicies, compact), player);
        }
    }
    
    public static void sendAllPlayersWorldID(final String worldID) {
        PacketHandler.WORLD_INFO_CHANNEL.sendToAll((IMessage)new WorldIDPacket(worldID));
    }
    
    public static void sendPlayerWorldID(final EntityPlayerMP player) {
        if (player instanceof EntityPlayerMP && player != null) {
            final WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            final String worldID = worldSaveHandler.getWorldID();
            final String playerName = player.func_70005_c_();
            try {
                PacketHandler.WORLD_INFO_CHANNEL.sendTo((IMessage)new WorldIDPacket(worldID), player);
            }
            catch (RuntimeException rte) {
                Journeymap.getLogger().error(playerName + " is not a real player. WorldID:" + worldID + " Error: " + rte);
            }
            catch (Exception e) {
                Journeymap.getLogger().error("Unknown Exception - PlayerName:" + playerName + " WorldID:" + worldID + " Exception " + e);
            }
        }
    }
    
    static {
        WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("world_info");
        DIMENSION_POLICIES_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("jm_dim_policy");
        TELEPORT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("jtp");
    }
}
