package journeymap.common.command;

import net.minecraft.server.*;
import net.minecraft.command.*;
import journeymap.common.network.model.*;
import journeymap.common.feature.*;
import net.minecraft.entity.*;

public class CommandJTP extends CommandBase
{
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }
    
    public String getName() {
        return "jtp";
    }
    
    public String getUsage(final ICommandSender sender) {
        return "/jtp <x y z dim>";
    }

    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 4) {
            throw new CommandException(this.getUsage(sender), new Object[0]);
        }
        final Entity player = (Entity)getCommandSenderAsPlayer(sender);
        try {
            final double x = Double.parseDouble(args[0]);
            final double y = Double.parseDouble(args[1]);
            final double z = Double.parseDouble(args[2]);
            final int dim = Integer.parseInt(args[3]);
            final Location location = new Location(x, y, z, dim);
            JourneyMapTeleport.attemptTeleport(player, location);
        }
        catch (NumberFormatException nfe) {
            throw new CommandException("Numbers only! Usage: " + this.getUsage(sender) + nfe, new Object[0]);
        }
        catch (Exception e) {
            throw new CommandException("/jtp failed Usage: " + this.getUsage(sender), new Object[0]);
        }
    }
}
