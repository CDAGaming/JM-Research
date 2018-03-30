package journeymap.server.api.util;

import journeymap.common.api.util.*;
import journeymap.server.api.*;
import javax.annotation.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
public class ServerPluginHelper extends PluginHelper<ServerPlugin, IServerPlugin>
{
    private static Supplier<ServerPluginHelper> lazyInit;
    
    private ServerPluginHelper() {
        super(ServerPlugin.class, IServerPlugin.class);
    }
    
    public static ServerPluginHelper instance() {
        return (ServerPluginHelper)ServerPluginHelper.lazyInit.get();
    }
    
    static {
        ServerPluginHelper.lazyInit = (Supplier<ServerPluginHelper>)Suppliers.memoize(ServerPluginHelper::new);
    }
}
