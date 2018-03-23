package journeymap.common.feature;

import net.minecraft.entity.*;
import journeymap.common.network.model.*;
import net.minecraftforge.fml.common.*;
import journeymap.common.*;
import com.mojang.authlib.*;
import net.minecraft.util.text.*;
import journeymap.server.properties.*;
import net.minecraft.server.*;
import net.minecraft.network.*;
import net.minecraft.world.*;
import net.minecraft.potion.*;
import net.minecraft.network.play.server.*;
import net.minecraft.entity.player.*;
import net.minecraft.server.management.*;
import java.util.*;
import journeymap.server.*;

public class JourneyMapTeleport
{
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
        creative = ((EntityPlayerMP)entity).capabilities.isCreativeMode;
        cheatMode = mcServer.getPlayerList().canSendCommands(new GameProfile(entity.getUniqueID(), entity.getName()));
        if (mcServer == null) {
            entity.sendMessage((ITextComponent)new TextComponentString("Cannot Find World"));
            return false;
        }
        final World destinationWorld = (World)mcServer.getWorld(location.getDim());
        if (!entity.isEntityAlive()) {
            entity.sendMessage((ITextComponent)new TextComponentString("Cannot teleport when dead."));
            return false;
        }
        if (destinationWorld == null) {
            entity.sendMessage((ITextComponent)new TextComponentString("Could not get world for Dimension " + location.getDim()));
            return false;
        }
        if (PropertiesManager.getInstance().getGlobalProperties().teleportEnabled.get() || debugOverride(entity) || creative || cheatMode || isOp((EntityPlayerMP)entity)) {
            teleportEntity(mcServer, destinationWorld, entity, location, entity.rotationYaw);
            return true;
        }
        entity.sendMessage((ITextComponent)new TextComponentString("Server has disabled JourneyMap teleporting."));
        return false;
    }
    
    private static boolean teleportEntity(final MinecraftServer server, final World destinationWorld, final Entity entity, final Location location, final float yaw) {
        final World startWorld = entity.world;
        final boolean changedWorld = startWorld != destinationWorld;
        final PlayerList playerList = server.getPlayerList();
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        final EntityPlayerMP player = (EntityPlayerMP)entity;
        player.dismountRidingEntity();
        if (changedWorld) {
            player.dimension = location.getDim();
            player.connection.sendPacket((Packet)new SPacketRespawn(player.dimension, player.world.getDifficulty(), destinationWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
            playerList.updatePermissionLevel(player);
            startWorld.removeEntityDangerously((Entity)player);
            player.isDead = false;
            transferPlayerToWorld((Entity)player, (WorldServer)destinationWorld);
            playerList.preparePlayer(player, (WorldServer)startWorld);
            player.connection.setPlayerLocation(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.rotationPitch);
            player.interactionManager.setWorld((WorldServer)destinationWorld);
            player.connection.sendPacket((Packet)new SPacketPlayerAbilities(player.capabilities));
            playerList.updateTimeAndWeatherForPlayer(player, (WorldServer)destinationWorld);
            playerList.syncPlayerInventory(player);
            for (final PotionEffect potioneffect : player.getActivePotionEffects()) {
                player.connection.sendPacket((Packet)new SPacketEntityEffect(player.getEntityId(), potioneffect));
            }
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent((EntityPlayer)player, player.dimension, location.getDim());
            return true;
        }
        player.connection.setPlayerLocation(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.rotationPitch);
        ((WorldServer)destinationWorld).getChunkProvider().loadChunk((int)location.getX() >> 4, (int)location.getZ() >> 4);
        return true;
    }
    
    private static void transferPlayerToWorld(final Entity entity, final WorldServer toWorldIn) {
        entity.setLocationAndAngles(entity.posX + 0.5, entity.posY, entity.posZ + 0.5, entity.rotationYaw, entity.rotationPitch);
        toWorldIn.spawnEntity(entity);
        toWorldIn.updateEntityWithOptionalForce(entity, false);
        entity.setWorld((World)toWorldIn);
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
