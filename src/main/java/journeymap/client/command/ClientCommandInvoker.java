package journeymap.client.command;

import net.minecraft.client.*;
import journeymap.common.*;
import com.mojang.authlib.*;
import journeymap.common.log.*;
import net.minecraft.client.entity.*;
import net.minecraft.server.integrated.*;
import net.minecraft.server.management.*;
import com.google.common.base.*;
import net.minecraft.server.*;
import java.util.*;
import net.minecraft.util.text.*;
import net.minecraft.command.*;
import net.minecraft.util.math.*;

public class ClientCommandInvoker implements ICommand
{
    Map<String, ICommand> commandMap;
    
    public ClientCommandInvoker() {
        this.commandMap = new HashMap<String, ICommand>();
    }
    
    public static boolean commandsAllowed(final Minecraft mc) {
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && mc.func_71401_C() != null) {
            final IntegratedServer mcServer = mc.func_71401_C();
            PlayerList configurationManager = null;
            GameProfile profile = null;
            try {
                profile = new GameProfile(player.func_110124_au(), player.func_70005_c_());
                configurationManager = mcServer.func_184103_al();
                return configurationManager.func_152596_g(profile);
            }
            catch (Exception e) {
                try {
                    if (profile != null && configurationManager != null) {
                        return mcServer.func_71264_H() && mcServer.field_71305_c[0].func_72912_H().func_76086_u() && mcServer.func_71214_G().equalsIgnoreCase(profile.getName());
                    }
                    Journeymap.getLogger().warn("Failed to check commandsAllowed both ways: " + LogFormatter.toString(e) + ", and profile or configManager were null.");
                    return true;
                }
                catch (Exception e2) {
                    Journeymap.getLogger().warn("Failed to check commandsAllowed. Both ways failed: " + LogFormatter.toString(e) + ", and " + LogFormatter.toString(e2));
                }
            }
        }
        return true;
    }
    
    public ClientCommandInvoker register(final ICommand command) {
        this.commandMap.put(command.func_71517_b().toLowerCase(), command);
        return this;
    }
    
    public String func_71517_b() {
        return "jm";
    }
    
    public String func_71518_a(final ICommandSender sender) {
        final StringBuffer sb = new StringBuffer();
        for (final ICommand command : this.commandMap.values()) {
            final String usage = command.func_71518_a(sender);
            if (!Strings.isNullOrEmpty(usage)) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("/jm ").append(usage);
            }
        }
        return sb.toString();
    }
    
    public List<String> func_71514_a() {
        return Collections.emptyList();
    }
    
    public void func_184881_a(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        try {
            if (args.length > 0) {
                final ICommand command = this.getSubCommand(args);
                if (command != null) {
                    final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    command.func_184881_a(server, sender, subArgs);
                }
            }
            else {
                sender.func_145747_a((ITextComponent)new TextComponentString(this.func_71518_a(sender)));
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
            throw new CommandException("Error in /jm: " + t, new Object[0]);
        }
    }
    
    public boolean func_184882_a(final MinecraftServer server, final ICommandSender sender) {
        return true;
    }
    
    public List<String> func_184883_a(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        try {
            final ICommand command = this.getSubCommand(args);
            if (command != null) {
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return (List<String>)command.func_184883_a(server, sender, subArgs, pos);
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
    
    public boolean func_82358_a(final String[] args, final int index) {
        return false;
    }
    
    public int compareTo(final ICommand o) {
        return 0;
    }
}
