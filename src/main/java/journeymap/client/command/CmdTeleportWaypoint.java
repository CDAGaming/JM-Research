package journeymap.client.command;

import net.minecraft.client.*;
import journeymap.client.model.*;
import net.minecraftforge.fml.client.*;
import com.mojang.authlib.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraft.server.integrated.*;
import net.minecraft.server.management.*;
import java.util.*;

public class CmdTeleportWaypoint
{
    final Minecraft mc;
    final Waypoint waypoint;
    
    public CmdTeleportWaypoint(final Waypoint waypoint) {
        this.mc = FMLClientHandler.instance().getClient();
        this.waypoint = waypoint;
    }
    
    public static boolean isPermitted(final Minecraft mc) {
        if (mc.getIntegratedServer() != null) {
            final IntegratedServer mcServer = mc.getIntegratedServer();
            PlayerList configurationManager = null;
            GameProfile profile = null;
            try {
                profile = new GameProfile(mc.player.getUniqueID(), mc.player.getName());
                configurationManager = mcServer.getPlayerList();
                return configurationManager.canSendCommands(profile);
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    if (profile != null && configurationManager != null) {
                        return mcServer.isSinglePlayer() && mcServer.worlds[0].getWorldInfo().areCommandsAllowed() && mcServer.getServerOwner().equalsIgnoreCase(profile.getName());
                    }
                    Journeymap.getLogger().warn("Failed to check teleport permission both ways: " + LogFormatter.toString(e) + ", and profile or configManager were null.");
                    return true;
                }
                catch (Exception e2) {
                    Journeymap.getLogger().warn("Failed to check teleport permission. Both ways failed: " + LogFormatter.toString(e) + ", and " + LogFormatter.toString(e2));
                }
            }
        }
        return true;
    }
    
    public void run() {
        double x = this.waypoint.getBlockCenteredX();
        double z = this.waypoint.getBlockCenteredZ();
        final TreeSet<Integer> dim = (TreeSet<Integer>)(TreeSet)this.waypoint.getDimensions();
        if (dim.first() == -1 && this.mc.player.dimension != -1) {
            x /= 8.0;
            z /= 8.0;
        }
        else if (dim.first() != -1 && this.mc.player.dimension == -1) {
            x *= 8.0;
            z *= 8.0;
        }
        if (Journeymap.getClient().isServerEnabled() || FMLClientHandler.instance().getClient().isSingleplayer()) {
            this.mc.player.sendChatMessage(String.format("/jtp %s %s %s %s", x, this.waypoint.getY(), z, dim.first()));
        }
        else {
            this.mc.player.sendChatMessage(String.format("/tp %s %s %s %s", this.mc.player.getName(), this.waypoint.getX(), this.waypoint.getY(), this.waypoint.getZ()));
        }
    }
}
