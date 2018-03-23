package journeymap.client.command;

import java.util.*;
import net.minecraft.server.*;
import com.google.common.base.*;
import journeymap.client.waypoint.*;
import org.lwjgl.input.*;
import journeymap.common.*;
import journeymap.client.task.main.*;
import journeymap.client.model.*;
import net.minecraft.client.*;
import journeymap.client.*;
import journeymap.client.ui.*;
import journeymap.client.log.*;
import journeymap.client.ui.component.*;
import net.minecraft.command.*;
import net.minecraft.util.math.*;

public class CmdEditWaypoint implements ICommand
{
    public String getName() {
        return "wpedit";
    }
    
    public String getUsage(final ICommandSender sender) {
        return null;
    }
    
    public List<String> getAliases() {
        return null;
    }
    
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final String text = Joiner.on(" ").skipNulls().join((Object[])args);
        final Waypoint waypoint = WaypointParser.parse(text);
        if (waypoint != null) {
            final boolean controlDown = Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
            Journeymap.getClient().queueMainThreadTask(new IMainThreadTask() {
                @Override
                public IMainThreadTask perform(final Minecraft mc, final JourneymapClient jm) {
                    if (controlDown) {
                        if (waypoint.isInPlayerDimension()) {
                            waypoint.setPersistent(false);
                            UIManager.INSTANCE.openFullscreenMap(waypoint);
                        }
                        else {
                            ChatLog.announceError("Location is not in your dimension");
                        }
                    }
                    else {
                        UIManager.INSTANCE.openWaypointEditor(waypoint, true, null);
                    }
                    return null;
                }
                
                @Override
                public String getName() {
                    return "Edit Waypoint";
                }
            });
        }
        else {
            ChatLog.announceError("Not a valid waypoint. Use: 'x:3, z:70', etc. : " + text);
        }
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
