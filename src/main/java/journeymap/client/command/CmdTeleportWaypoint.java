package journeymap.client.command;

import journeymap.client.Constants;
import journeymap.client.api.display.Waypoint;
import journeymap.client.feature.ClientFeatures;
import journeymap.client.log.ChatLog;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;

public class CmdTeleportWaypoint implements Runnable {
    private final Waypoint waypoint;
    private final int targetDimension;

    public CmdTeleportWaypoint(final Waypoint waypoint, final int targetDimension) {
        this.waypoint = waypoint;
        this.targetDimension = targetDimension;
    }

    public static boolean isPermitted() {
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && Journeymap.getClient().isServerEnabled()) {
            return ClientFeatures.instance().isAllowed(Feature.Action.Teleport, player.dimension);
        }
        return ClientCommandInvoker.commandsAllowed(FMLClientHandler.instance().getClient());
    }

    public static boolean isPermitted(final int fromDim, final int toDim) {
        if (Journeymap.getClient().isServerEnabled()) {
            final ClientFeatures features = ClientFeatures.instance();
            boolean allowed = features.isAllowed(Feature.Action.Teleport, fromDim);
            if (allowed && fromDim != toDim) {
                allowed = features.isAllowed(Feature.Action.Teleport, toDim);
            }
            return allowed;
        }
        return fromDim == toDim && ClientCommandInvoker.commandsAllowed(FMLClientHandler.instance().getClient());
    }

    @Override
    public void run() {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                return;
            }
            final int dim = this.targetDimension;
            if (Journeymap.getClient().isServerEnabled()) {
                final Vec3d pos = this.waypoint.getCenteredVec(dim);
                player.sendChatMessage(String.format("/jtp %s %s %s %s", pos.x, pos.y, pos.z, dim));
            } else if (player.dimension == dim) {
                final BlockPos pos2 = this.waypoint.getPosition(dim);
                player.sendChatMessage(String.format("/tp %s %s %s %s", player.getName(), pos2.getX(), pos2.getY(), pos2.getZ()));
            } else {
                ChatLog.announceError(Constants.getString("jm.waypoint.teleport.dim_error"));
            }
        } catch (Exception e) {
            Journeymap.getLogger().error("Error teleporting: " + LogFormatter.toPartialString(e));
        }
    }
}
