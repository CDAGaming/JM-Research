package journeymap.client.api.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientPlugin;
import journeymap.common.api.util.PluginHelper;

public class ClientPluginHelper extends PluginHelper<ClientPlugin, IClientPlugin> {
    private static Supplier<ClientPluginHelper> lazyInit;

    static {
        ClientPluginHelper.lazyInit = Suppliers.memoize(ClientPluginHelper::new);
    }

    private ClientPluginHelper() {
        super(ClientPlugin.class, IClientPlugin.class);
    }

    public static ClientPluginHelper instance() {
        return ClientPluginHelper.lazyInit.get();
    }
}
