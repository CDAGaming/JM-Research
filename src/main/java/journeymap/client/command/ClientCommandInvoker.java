package journeymap.client.command;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.*;

public class ClientCommandInvoker implements ICommand {
    Map<String, ICommand> commandMap;

    public ClientCommandInvoker() {
        this.commandMap = new HashMap<>();
    }

    public static boolean commandsAllowed(final Minecraft mc) {
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && mc.getIntegratedServer() != null) {
            final IntegratedServer mcServer = mc.getIntegratedServer();
            PlayerList configurationManager = null;
            GameProfile profile = null;
            try {
                profile = new GameProfile(player.getUniqueID(), player.getName());
                configurationManager = mcServer.getPlayerList();
                return configurationManager.canSendCommands(profile);
            } catch (Exception e) {
                try {
                    if (profile != null && configurationManager != null) {
                        return mcServer.isSinglePlayer() && mcServer.worlds[0].getWorldInfo().areCommandsAllowed() && mcServer.getServerOwner().equalsIgnoreCase(profile.getName());
                    }
                    Journeymap.getLogger().warn("Failed to check commandsAllowed both ways: " + LogFormatter.toString(e) + ", and profile or configManager were null.");
                    return true;
                } catch (Exception e2) {
                    Journeymap.getLogger().warn("Failed to check commandsAllowed. Both ways failed: " + LogFormatter.toString(e) + ", and " + LogFormatter.toString(e2));
                }
            }
        }
        return true;
    }

    public ClientCommandInvoker register(final ICommand command) {
        this.commandMap.put(command.getName().toLowerCase(), command);
        return this;
    }

    public String getName() {
        return "jm";
    }

    public String getUsage(final ICommandSender sender) {
        final StringBuilder sb = new StringBuilder();
        for (final ICommand command : this.commandMap.values()) {
            final String usage = command.getUsage(sender);
            if (!Strings.isNullOrEmpty(usage)) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("/jm ").append(usage);
            }
        }
        return sb.toString();
    }

    public List<String> getAliases() {
        return Collections.emptyList();
    }

    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        try {
            if (args.length > 0) {
                final ICommand command = this.getSubCommand(args);
                if (command != null) {
                    final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    command.execute(server, sender, subArgs);
                }
            } else {
                sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
            throw new CommandException("Error in /jm: " + t);
        }
    }

    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }

    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        try {
            final ICommand command = this.getSubCommand(args);
            if (command != null) {
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return command.getTabCompletions(server, sender, subArgs, pos);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error in addTabCompletionOptions: " + LogFormatter.toPartialString(t));
        }
        return null;
    }

    public ICommand getSubCommand(final String[] args) {
        if (args.length > 0) {
            final ICommand command = this.commandMap.get(args[0].toLowerCase());
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    public boolean isUsernameIndex(final String[] args, final int index) {
        return false;
    }

    public int compareTo(final ICommand o) {
        return 0;
    }
}
