package journeymap.client.command;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

public class ClientCommandInvoker implements ICommand {
    Map<String, ICommand> commandMap;

    public ClientCommandInvoker() {
        this.commandMap = new HashMap<String, ICommand>();
    }

    public ClientCommandInvoker register(final ICommand command) {
        this.commandMap.put(command.getName().toLowerCase(), command);
        return this;
    }

    public String getName() {
        return "jm";
    }

    public String getUsage(final ICommandSender sender) {
        final StringBuffer sb = new StringBuffer();
        for (final ICommand command : this.commandMap.values()) {
            final String usage = command.getUsage(sender);
            if (!Strings.isEmpty(usage)) {
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
