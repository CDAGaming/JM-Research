package journeymap.common.action;

import net.minecraft.entity.*;
import journeymap.common.network.model.*;
import net.minecraftforge.fml.common.*;
import journeymap.common.*;
import net.minecraft.util.text.*;
import journeymap.server.*;
import journeymap.server.api.impl.*;
import journeymap.common.api.feature.*;
import net.minecraft.server.*;
import journeymap.common.feature.*;
import net.minecraft.network.*;
import net.minecraft.world.*;
import net.minecraft.potion.*;
import net.minecraft.network.play.server.*;
import net.minecraft.entity.player.*;
import net.minecraft.server.management.*;
import java.util.*;
import net.minecraftforge.fml.server.*;

public class JourneyMapTeleport
{
    public static boolean attemptTeleport(final Entity entity, final Location location) {
        final MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (entity == null) {
            Journeymap.getLogger().error("Attempted to teleport null entity.");
            return false;
        }
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        if (mcServer == null) {
            entity.func_145747_a((ITextComponent)new TextComponentString("Cannot Find World"));
            return false;
        }
        final World destinationWorld = (World)mcServer.func_71218_a(location.getDim());
        if (!entity.func_70089_S()) {
            entity.func_145747_a((ITextComponent)new TextComponentString("Cannot teleport when dead."));
            return false;
        }
        if (destinationWorld == null) {
            entity.func_145747_a((ITextComponent)new TextComponentString("Could not get world for Dimension " + location.getDim()));
            return false;
        }
        final boolean isOp = JourneymapServer.isOp(entity.func_110124_au());
        final int fromDim = entity.func_130014_f_().field_73011_w.getDimension();
        final int toDim = location.getDim();
        final GameType gameType = ((EntityPlayerMP)entity).field_71134_c.func_73081_b();
        final DimensionPolicies fromDimPolicies = ServerAPI.INSTANCE.getFeatures(entity.func_110124_au()).get(fromDim);
        final boolean fromTpAllowed = fromDimPolicies.isAllowed(gameType, Feature.Action.Teleport);
        final DimensionPolicies toDimPolicies = ServerAPI.INSTANCE.getFeatures(entity.func_110124_au()).get(toDim);
        final boolean toTpAllowed = toDimPolicies.isAllowed(gameType, Feature.Action.Teleport);
        if (fromTpAllowed && toTpAllowed) {
            teleportEntity(mcServer, destinationWorld, entity, location, entity.field_70177_z);
            return true;
        }
        String origin = fromDimPolicies.getOrigin(gameType, Feature.Action.Teleport);
        final String role = isOp ? "Ops" : "Players";
        if (fromDim == toDim) {
            entity.func_145747_a((ITextComponent)new TextComponentString(String.format("%s has disabled JourneyMap teleporting by %s in this dimension in %s.", origin, role, gameType)));
            return false;
        }
        if (!fromTpAllowed) {
            entity.func_145747_a((ITextComponent)new TextComponentString(String.format("%s has disabled JourneyMap teleporting by %s from this dimension in %s.", origin, role, gameType)));
            return false;
        }
        origin = toDimPolicies.getOrigin(gameType, Feature.Action.Teleport);
        entity.func_145747_a((ITextComponent)new TextComponentString(String.format("%s has disabled JourneyMap teleporting by %s to that dimension in %s.", origin, role, gameType)));
        return false;
    }
    
    private static boolean teleportEntity(final MinecraftServer server, final World destinationWorld, final Entity entity, final Location location, final float yaw) {
        final World startWorld = Journeymap.clientWorld();
        final boolean changedWorld = startWorld != destinationWorld;
        final PlayerList playerList = server.func_184103_al();
        if (!(entity instanceof EntityPlayerMP)) {
            return false;
        }
        final EntityPlayerMP player = (EntityPlayerMP)entity;
        player.func_184210_p();
        if (changedWorld) {
            player.field_71093_bK = location.getDim();
            player.field_71135_a.func_147359_a((Packet)new SPacketRespawn(player.field_71093_bK, player.field_70170_p.func_175659_aa(), destinationWorld.func_72912_H().func_76067_t(), player.field_71134_c.func_73081_b()));
            playerList.func_187243_f(player);
            startWorld.func_72973_f((Entity)player);
            player.field_70128_L = false;
            transferPlayerToWorld((Entity)player, (WorldServer)destinationWorld);
            playerList.func_72375_a(player, (WorldServer)startWorld);
            player.field_71135_a.func_147364_a(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.field_70125_A);
            player.field_71134_c.func_73080_a((WorldServer)destinationWorld);
            player.field_71135_a.func_147359_a((Packet)new SPacketPlayerAbilities(player.field_71075_bZ));
            playerList.func_72354_b(player, (WorldServer)destinationWorld);
            playerList.func_72385_f(player);
            for (final PotionEffect potioneffect : player.func_70651_bq()) {
                player.field_71135_a.func_147359_a((Packet)new SPacketEntityEffect(player.func_145782_y(), potioneffect));
            }
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent((EntityPlayer)player, player.field_71093_bK, location.getDim());
            return true;
        }
        player.field_71135_a.func_147364_a(location.getX() + 0.5, location.getY(), location.getZ() + 0.5, yaw, entity.field_70125_A);
        ((WorldServer)destinationWorld).func_72863_F().func_186028_c((int)location.getX() >> 4, (int)location.getZ() >> 4);
        return true;
    }
    
    private static void transferPlayerToWorld(final Entity entity, final WorldServer toWorldIn) {
        entity.func_70012_b(entity.field_70165_t + 0.5, entity.field_70163_u, entity.field_70161_v + 0.5, entity.field_70177_z, entity.field_70125_A);
        toWorldIn.func_72838_d(entity);
        toWorldIn.func_72866_a(entity, false);
        entity.func_70029_a((World)toWorldIn);
    }
    
    public static boolean isOp(final EntityPlayerMP player) {
        return FMLServerHandler.instance().getServer().func_184103_al().func_152603_m().func_152683_b((Object)player.func_146103_bH()) != null;
    }
}
