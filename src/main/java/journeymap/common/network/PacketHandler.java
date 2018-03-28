package journeymap.common.network;

import journeymap.common.Journeymap;
import journeymap.common.network.model.InitLogin;
import journeymap.common.network.model.Location;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.PermissionProperties;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL;
    public static final SimpleNetworkWrapper DIMENSION_PERMISSIONS_CHANNEL;
    public static final SimpleNetworkWrapper TELEPORT_CHANNEL;
    public static final SimpleNetworkWrapper INIT_LOGIN_CHANNEL;

    static {
        WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("world_info");
        DIMENSION_PERMISSIONS_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("jm_dim_permission");
        TELEPORT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("jtp");
        INIT_LOGIN_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("jm_init_login");
    }

    public static void init(final Side side) {
        PacketHandler.WORLD_INFO_CHANNEL.registerMessage((Class) WorldIDPacket.WorldIdListener.class, (Class) WorldIDPacket.class, 0, side);
        PacketHandler.INIT_LOGIN_CHANNEL.registerMessage((Class) LoginPacket.Listener.class, (Class) LoginPacket.class, 0, side);
        PacketHandler.TELEPORT_CHANNEL.registerMessage((Class) TeleportPacket.Listener.class, (Class) TeleportPacket.class, 0, Side.SERVER);
        if (Side.SERVER == side) {
        }
        if (Side.CLIENT == side) {
            PacketHandler.DIMENSION_PERMISSIONS_CHANNEL.registerMessage((Class) DimensionPermissionPacket.Listener.class, (Class) DimensionPermissionPacket.class, 0, side);
        }
    }

    public static void teleportPlayer(final Location location) {
        PacketHandler.TELEPORT_CHANNEL.sendToServer((IMessage) new TeleportPacket(location));
    }

    public static void sendDimensionPacketToPlayer(final EntityPlayerMP player, final PermissionProperties property) {
        final DimensionPermissionPacket prop = new DimensionPermissionPacket(property);
        PacketHandler.DIMENSION_PERMISSIONS_CHANNEL.sendTo((IMessage) prop, player);
    }

    public static void sendAllPlayersWorldID(final String worldID) {
        PacketHandler.WORLD_INFO_CHANNEL.sendToAll((IMessage) new WorldIDPacket(worldID));
    }

    public static void sendPlayerWorldID(final EntityPlayerMP player) {
        if (player != null && player instanceof EntityPlayerMP) {
            final WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            final String worldID = worldSaveHandler.getWorldID();
            final String playerName = player.getName();
            try {
                PacketHandler.WORLD_INFO_CHANNEL.sendTo((IMessage) new WorldIDPacket(worldID), player);
            } catch (RuntimeException rte) {
                Journeymap.getLogger().error(playerName + " is not a real player. WorldID:" + worldID + " Error: " + rte);
            } catch (Exception e) {
                Journeymap.getLogger().error("Unknown Exception - PlayerName:" + playerName + " WorldID:" + worldID + " Exception " + e);
            }
        }
    }

    public static void sendLoginPacket(final EntityPlayerMP player, final InitLogin packetData) {
        if (player != null && player instanceof EntityPlayerMP) {
            Journeymap.getLogger().info("Sending log in packet.");
            final String playerName = player.getName();
            try {
                PacketHandler.INIT_LOGIN_CHANNEL.sendTo((IMessage) new LoginPacket(packetData), player);
            } catch (RuntimeException rte) {
                Journeymap.getLogger().error(playerName + " is not a real player. Error: " + rte);
            } catch (Exception e) {
                Journeymap.getLogger().error("Unknown Exception - PlayerName:" + playerName + " Exception " + e);
            }
        }
    }
}
