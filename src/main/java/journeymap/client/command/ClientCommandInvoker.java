package journeymap.client.command;

import org.apache.logging.log4j.util.*;
import net.minecraft.server.*;
import java.util.*;
import net.minecraft.util.text.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraft.command.*;
import net.minecraft.util.math.*;

public class ClientCommandInvoker implements ICommand
{
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
            if (!Strings.isEmpty((CharSequence)usage)) {
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
            }
            else {
                sender.sendMessage((ITextComponent)new TextComponentString(this.getUsage(sender)));
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
            throw new CommandException("Error in /jm: " + t, new Object[0]);
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
                return (List<String>)command.getTabCompletions(server, sender, subArgs, pos);
            }
        }
        catch (Throwable t) {
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
