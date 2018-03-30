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
    public String func_71517_b() {
        return "~";
    }
    
    public String func_71518_a(final ICommandSender sender) {
        return TextFormatting.AQUA + "~" + TextFormatting.RESET + " : Copy your location into Text";
    }
    
    public List<String> func_71514_a() {
        return null;
    }
    
    public void func_184881_a(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        String text;
        if (args.length > 1) {
            text = Joiner.on("").skipNulls().join((Object[])args);
        }
        else {
            final BlockPos pos = sender.func_180425_c();
            text = String.format("[x:%s, y:%s, z:%s]", pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
        }
        final String pos2 = text;
        Journeymap.getClient().queueMainThreadTask(new IMainThreadTask() {
            @Override
            public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
                FMLClientHandler.instance().getClient().func_147108_a((GuiScreen)new WaypointChat(pos2));
                return null;
            }
            
            @Override
            public String getName() {
                return "Edit Waypoint";
            }
        });
    }
    
    public boolean func_184882_a(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }
    
    public List<String> func_184883_a(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        return null;
    }
    
    public boolean func_82358_a(final String[] args, final int index) {
        return false;
    }
    
    public int compareTo(final ICommand o) {
        return 0;
    }
}
