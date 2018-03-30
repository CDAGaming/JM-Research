package journeymap.client.forge.event;

import journeymap.client.cartography.color.ColorManager;
import journeymap.client.command.ClientCommandInvoker;
import journeymap.client.command.CmdChatPosition;
import journeymap.client.command.CmdEditWaypoint;
import journeymap.client.world.ChunkMonitor;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.HashMap;

public class EventHandlerManager {
    private static HashMap<Class<? extends EventHandler>, EventHandler> handlers;

    static {
        EventHandlerManager.handlers = new HashMap<>();
    }

    public static void registerHandlers() {
        register(KeyEventHandler.INSTANCE);
        register(new ChatEventHandler());
        register(new StateTickHandler());
        register(new WorldEventHandler());
        register(new WaypointBeaconHandler());
        register(new TextureAtlasHandler());
        register(new MiniMapOverlayHandler());
        ColorManager.INSTANCE.getDeclaringClass();
        final ClientCommandInvoker clientCommandInvoker = new ClientCommandInvoker();
        clientCommandInvoker.register(new CmdChatPosition());
        clientCommandInvoker.register(new CmdEditWaypoint());
        ClientCommandHandler.instance.registerCommand(clientCommandInvoker);
        register(ChunkMonitor.INSTANCE);
    }

    public static void unregisterAll() {
        final ArrayList<Class<? extends EventHandler>> list = new ArrayList<>(EventHandlerManager.handlers.keySet());
        for (final Class<? extends EventHandler> handlerClass : list) {
            unregister(handlerClass);
        }
    }

    private static void register(final EventHandler handler) {
        final Class<? extends EventHandler> handlerClass = handler.getClass();
        if (EventHandlerManager.handlers.containsKey(handlerClass)) {
            Journeymap.getLogger().warn("Handler already registered: " + handlerClass.getName());
            return;
        }
        try {
            MinecraftForge.EVENT_BUS.register(handler);
            Journeymap.getLogger().debug("Handler registered: " + handlerClass.getName());
            EventHandlerManager.handlers.put(handler.getClass(), handler);
        } catch (Throwable t) {
            Journeymap.getLogger().error(handlerClass.getName() + " registration FAILED: " + LogFormatter.toString(t));
        }
    }

    public static void unregister(final Class<? extends EventHandler> handlerClass) {
        final EventHandler handler = EventHandlerManager.handlers.remove(handlerClass);
        if (handler != null) {
            try {
                MinecraftForge.EVENT_BUS.unregister(handler);
                Journeymap.getLogger().debug("Handler unregistered: " + handlerClass.getName());
            } catch (Throwable t) {
                Journeymap.getLogger().error(handler + " unregistration FAILED: " + LogFormatter.toString(t));
            }
        }
    }

    public interface EventHandler {
    }
}
