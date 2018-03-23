package journeymap.client.command;

import net.minecraft.util.text.*;
import java.util.*;
import net.minecraft.server.*;
import com.google.common.base.*;
import journeymap.common.*;
import journeymap.client.task.main.*;
import net.minecraft.client.*;
import journeymap.client.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.ui.waypoint.*;
import net.minecraft.client.gui.*;
import net.minecraft.util.math.*;
import net.minecraft.command.*;

public class CmdChatPosition implements ICommand
{
    public String getName() {
        return "~";
    }
    
    public String getUsage(final ICommandSender sender) {
        return TextFormatting.AQUA + "~" + TextFormatting.RESET + " : Copy your location into Text";
    }
    
    public List<String> getAliases() {
        return null;
    }
    
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        String text;
        if (args.length > 1) {
            text = Joiner.on("").skipNulls().join((Object[])args);
        }
        else {
            final BlockPos pos = sender.getPosition();
            text = String.format("[x:%s, y:%s, z:%s]", pos.getX(), pos.getY(), pos.getZ());
        }
        final String pos2 = text;
        Journeymap.getClient().queueMainThreadTask(new IMainThreadTask() {
            @Override
            public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
                FMLClientHandler.instance().getClient().displayGuiScreen((GuiScreen)new WaypointChat(pos2));
                return null;
            }
            
            @Override
            public String getName() {
                return "Edit Waypoint";
            }
        });
    }
    
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }
    
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        return null;
    }
    
    public boolean isUsernameIndex(final String[] args, final int index) {
        return false;
    }
    
    public int compareTo(final ICommand o) {
        return 0;
    }
}
