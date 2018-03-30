package journeymap.client.command;

import journeymap.client.api.display.*;
import journeymap.common.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.client.entity.*;
import journeymap.client.*;
import journeymap.client.log.*;
import journeymap.common.log.*;
import net.minecraft.util.math.*;

public class CmdTeleportWaypoint implements Runnable
{
    private final Waypoint waypoint;
    private final int targetDimension;
    
    public CmdTeleportWaypoint(final Waypoint waypoint, final int targetDimension) {
        this.waypoint = waypoint;
        this.targetDimension = targetDimension;
    }
    
    public static boolean isPermitted() {
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && Journeymap.getClient().isServerEnabled()) {
            return ClientFeatures.instance().isAllowed(Feature.Action.Teleport, player.field_71093_bK);
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
                player.func_71165_d(String.format("/jtp %s %s %s %s", pos.field_72450_a, pos.field_72448_b, pos.field_72449_c, dim));
            }
            else if (player.field_71093_bK == dim) {
                final BlockPos pos2 = this.waypoint.getPosition(dim);
                player.func_71165_d(String.format("/tp %s %s %s %s", player.func_70005_c_(), pos2.func_177958_n(), pos2.func_177956_o(), pos2.func_177952_p()));
            }
            else {
                ChatLog.announceError(Constants.getString("jm.waypoint.teleport.dim_error"));
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error teleporting: " + LogFormatter.toPartialString(e));
        }
    }
}
