package journeymap.client.command;

import com.google.common.base.Joiner;
import journeymap.client.JourneymapClient;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.ui.UIManager;
import journeymap.client.waypoint.WaypointParser;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CmdEditWaypoint implements ICommand {
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
        final String text = Joiner.on(" ").skipNulls().join((Object[]) args);
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
                        } else {
                            ChatLog.announceError("Location is not in your dimension");
                        }
                    } else {
                        UIManager.INSTANCE.openWaypointEditor(waypoint, true, null);
                    }
                    return null;
                }

                @Override
                public String getName() {
                    return "Edit Waypoint";
                }
            });
        } else {
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
