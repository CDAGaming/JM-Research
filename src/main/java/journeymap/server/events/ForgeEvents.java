package journeymap.server.events;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import net.minecraftforge.event.entity.*;
import journeymap.server.api.impl.*;
import net.minecraft.entity.player.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraft.command.server.*;
import journeymap.common.log.*;
import net.minecraft.command.*;
import net.minecraft.server.*;
import net.minecraft.entity.*;
import net.minecraftforge.fml.common.network.*;

public enum ForgeEvents
{
    INSTANCE;
    
    private Logger logger;
    
    private ForgeEvents() {
        this.logger = Journeymap.getLogger();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoinWorldStart(final EntityJoinWorldEvent event) {
        final EntityPlayerMP player = this.getForgePlayer(event.getEntity());
        if (player != null) {
            ServerAPI.INSTANCE.pauseClientPackets(player);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(String.format("Player %s joining dimension %s...", player.getDisplayNameString(), player.field_71093_bK));
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoinWorldEnd(final EntityJoinWorldEvent event) {
        final EntityPlayerMP player = this.getForgePlayer(event.getEntity());
        if (player != null) {
            try {
                int delay = 500;
                for (final int dim : DimensionManager.getIDs()) {
                    final ServerAPI instance = ServerAPI.INSTANCE;
                    final EntityPlayerMP player2 = player;
                    final int dimension = dim;
                    delay += 100;
                    instance.sendDimensionPolicies(player2, dimension, delay);
                }
            }
            finally {
                ServerAPI.INSTANCE.resumeClientPackets(player);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommandEvent(final CommandEvent event) {
        final ICommand command = event.getCommand();
        try {
            EntityPlayerMP player = null;
            if (command instanceof CommandOp || command instanceof CommandDeOp) {
                final String username = event.getParameters()[0];
                final MinecraftServer server = event.getSender().func_184102_h();
                player = CommandBase.func_184888_a(server, event.getSender(), username);
            }
            else if (command instanceof CommandGameMode) {
                final String[] args = event.getParameters();
                if (args.length >= 2) {
                    final String username2 = event.getParameters()[1];
                    final MinecraftServer server2 = event.getSender().func_184102_h();
                    player = CommandBase.func_184888_a(server2, event.getSender(), username2);
                }
                else {
                    final Entity sender = event.getSender().func_174793_f();
                    if (sender instanceof EntityPlayerMP) {
                        player = (EntityPlayerMP)sender;
                    }
                }
            }
            if (player != null) {
                int delay = 500;
                for (final int dim : DimensionManager.getIDs()) {
                    final ServerAPI instance = ServerAPI.INSTANCE;
                    final EntityPlayerMP player2 = player;
                    final int dimension = dim;
                    delay += 100;
                    instance.sendDimensionPolicies(player2, dimension, delay);
                }
            }
        }
        catch (PlayerNotFoundException ex) {}
        catch (Exception e) {
            this.logger.error(String.format("Error handling CommandEvent %s: %s", command, LogFormatter.toPartialString(e)));
        }
    }
    
    private EntityPlayerMP getForgePlayer(final Entity entity) {
        if (entity instanceof EntityPlayerMP) {
            final EntityPlayerMP player = (EntityPlayerMP)entity;
            final boolean hasForge = (boolean)player.field_71135_a.func_147362_b().channel().attr(NetworkRegistry.FML_MARKER).get();
            if (hasForge) {
                return player;
            }
            Journeymap.getLogger().debug(String.format("Player %s joining dimension %s doesn't have Forge.", player.getDisplayNameString(), player.field_71093_bK));
        }
        return null;
    }
}
