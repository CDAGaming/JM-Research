package journeymap.common.command;

import net.minecraft.server.*;
import net.minecraft.command.*;
import journeymap.common.network.model.*;
import journeymap.common.action.*;
import net.minecraft.entity.*;
import journeymap.server.api.impl.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraft.entity.player.*;

public class CommandJTP extends CommandBase
{
    public boolean func_184882_a(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }
    
    public String func_71517_b() {
        return "jtp";
    }
    
    public String func_71518_a(final ICommandSender sender) {
        return "/jtp <x y z dim>";
    }
    
    public void func_184881_a(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 4) {
            throw new CommandException(this.func_71518_a(sender), new Object[0]);
        }
        final EntityPlayerMP player = func_71521_c(sender);
        try {
            final double x = Double.parseDouble(args[0]);
            final double y = Double.parseDouble(args[1]);
            final double z = Double.parseDouble(args[2]);
            final int dim = Integer.parseInt(args[3]);
            final Location location = new Location(x, y, z, dim);
            final boolean ok = JourneyMapTeleport.attemptTeleport((Entity)player, location);
            if (!ok) {
                ServerAPI.INSTANCE.sendDimensionPolicies(player, player.field_71093_bK);
                if (dim != player.field_71093_bK) {
                    ServerAPI.INSTANCE.sendDimensionPolicies(player, dim);
                }
            }
        }
        catch (NumberFormatException nfe) {
            throw new CommandException("Numbers only! Usage: " + this.func_71518_a(sender) + nfe, new Object[0]);
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error with CommandJTP: " + LogFormatter.toPartialString(e));
            throw new CommandException("/jtp failed Usage: " + this.func_71518_a(sender), new Object[0]);
        }
    }
}
