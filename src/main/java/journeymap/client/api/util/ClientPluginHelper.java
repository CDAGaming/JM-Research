package journeymap.client.api.util;

import journeymap.common.api.util.*;
import journeymap.client.api.*;
import com.google.common.base.*;

public class ClientPluginHelper extends PluginHelper<ClientPlugin, IClientPlugin>
{
    private static Supplier<ClientPluginHelper> lazyInit;
    
    private ClientPluginHelper() {
        super(ClientPlugin.class, IClientPlugin.class);
    }
    
    public static ClientPluginHelper instance() {
        return (ClientPluginHelper)ClientPluginHelper.lazyInit.get();
    }
    
    static {
        ClientPluginHelper.lazyInit = (Supplier<ClientPluginHelper>)Suppliers.memoize(ClientPluginHelper::new);
    }
}
