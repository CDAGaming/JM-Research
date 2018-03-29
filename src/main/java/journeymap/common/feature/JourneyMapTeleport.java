package journeymap.common.feature;

import com.mojang.authlib.GameProfile;
import journeymap.common.Journeymap;
import journeymap.common.network.model.Location;
import journeymap.server.JourneymapServer;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class JourneyMapTeleport {
    public static boolean attemptTeleport(final Entity entity, final Location location) {
        final MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        boolean creative = false;
        boolean cheatMode = false;
        if (entity == null) {
            Journeymap.getLogger().error("Attempted to teleport null entity.");
            return false;
        }
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        creative = ((EntityPlayerMP) entity).capabilities.isCreativeMode;
        cheatMode = mcServer.getPlayerList().canSendCommands(new GameProfile(entity.getUniqueID(), entity.getName()));
        if (mcServer == null) {
            entity.sendMessage(new TextComponentString("Cannot Find World"));
            return false;
        }
        final World destinationWorld = mcServer.getWorld(location.getDim());
        if (!entity.isEntityAlive()) {
            entity.sendMessage(new TextComponentString("Cannot teleport when dead."));
            return false;
        }
        if (destinationWorld == null) {
            entity.sendMessage(new TextComponentString("Could not get world for Dimension " + location.getDim()));
            return false;
        }
        if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get() || debugOverride(entity) || creative || cheatMode || isOp((EntityPlayerMP) entity)) {
            teleportEntity(mcServer, destinationWorld, entity, location, entity.rotationYaw);
            return true;
        }
        entity.sendMessage(new TextComponentString("Server has disabled JourneyMap teleporting."));
        return false;
    }

    private static boolean teleportEntity(final MinecraftServer server, final World destinationWorld, final Entity entity, final Location location, final float yaw) {
        final World startWorld = entity.world;
        final boolean changedWorld = startWorld != destinationWorld;
        final PlayerList playerList = server.getPlayerList();
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        final EntityPlayerMP player = (EntityPlayerMP) entity;
        player.dismountRidingEntity();
        if (changedWorld) {
            player.dimension = location.getDim();
            player.connection.sendPacket(new SPacketRespawn(player.dimension, player.world.getDifficulty(), destinationWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
            playerList.updatePermissionLevel(player);
            startWorld.removeEntityDangerously(player);
            player.isDead = false;
            transferPlayerToWorld(player, (WorldServer) destinationWorld);
            playerList.preparePlayer(player, (WorldServer) startWorld);
            player.connection.setPlayerLocation(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.rotationPitch);
            player.interactionManager.setWorld((WorldServer) destinationWorld);
            player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
            playerList.updateTimeAndWeatherForPlayer(player, (WorldServer) destinationWorld);
            playerList.syncPlayerInventory(player);
            for (final PotionEffect potioneffect : player.getActivePotionEffects()) {
                player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
            }
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, player.dimension, location.getDim());
            return true;
        }
        player.connection.setPlayerLocation(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.rotationPitch);
        ((WorldServer) destinationWorld).getChunkProvider().loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);
        return true;
    }

    private static void transferPlayerToWorld(final Entity entity, final WorldServer toWorldIn) {
        entity.setLocationAndAngles(entity.posX + 0.5, entity.posY, entity.posZ + 0.5, entity.rotationYaw, entity.rotationPitch);
        toWorldIn.spawnEntity(entity);
        toWorldIn.updateEntityWithOptionalForce(entity, false);
        entity.setWorld(toWorldIn);
    }

    private static boolean debugOverride(final Entity sender) {
        return JourneymapServer.DEV_MODE && ("mysticdrew".equalsIgnoreCase(sender.getName()) || "techbrew".equalsIgnoreCase(sender.getName()));
    }

    public static boolean isOp(final EntityPlayerMP player) {
        final String[] getOppedPlayerNames;
        final String[] ops = getOppedPlayerNames = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayerNames();
        for (final String opName : getOppedPlayerNames) {
            if (player.getDisplayNameString().equalsIgnoreCase(opName)) {
                return true;
            }
        }
        return false;
    }
}
