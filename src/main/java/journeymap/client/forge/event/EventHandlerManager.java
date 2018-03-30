package journeymap.client.forge.event;

import journeymap.client.cartography.color.*;
import net.minecraft.command.*;
import journeymap.client.command.*;
import net.minecraftforge.client.*;
import java.util.*;
import journeymap.common.*;
import net.minecraftforge.common.*;
import journeymap.common.log.*;

public class EventHandlerManager
{
    private static HashMap<Class<? extends EventHandler>, EventHandler> handlers;
    
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
        clientCommandInvoker.register((ICommand)new CmdChatPosition());
        clientCommandInvoker.register((ICommand)new CmdEditWaypoint());
        ClientCommandHandler.instance.func_71560_a((ICommand)clientCommandInvoker);
    }
    
    public static void unregisterAll() {
        final ArrayList<Class<? extends EventHandler>> list = new ArrayList<Class<? extends EventHandler>>(EventHandlerManager.handlers.keySet());
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
            MinecraftForge.EVENT_BUS.register((Object)handler);
            Journeymap.getLogger().debug("Handler registered: " + handlerClass.getName());
            EventHandlerManager.handlers.put(handler.getClass(), handler);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(handlerClass.getName() + " registration FAILED: " + LogFormatter.toString(t));
        }
    }
    
    public static void unregister(final Class<? extends EventHandler> handlerClass) {
        final EventHandler handler = EventHandlerManager.handlers.remove(handlerClass);
        if (handler != null) {
            try {
                MinecraftForge.EVENT_BUS.unregister((Object)handler);
                Journeymap.getLogger().debug("Handler unregistered: " + handlerClass.getName());
            }
            catch (Throwable t) {
                Journeymap.getLogger().error(handler + " unregistration FAILED: " + LogFormatter.toString(t));
            }
        }
    }
    
    static {
        EventHandlerManager.handlers = new HashMap<Class<? extends EventHandler>, EventHandler>();
    }
    
    public interface EventHandler
    {
    }
}
