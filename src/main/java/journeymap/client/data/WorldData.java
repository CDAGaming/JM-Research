package journeymap.client.data;

import com.google.common.cache.*;
import net.minecraftforge.fml.client.*;
import net.minecraft.client.network.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.client.gui.*;
import com.mojang.realmsclient.*;
import com.mojang.realmsclient.dto.*;
import journeymap.common.*;
import journeymap.common.log.*;
import com.google.common.base.*;
import net.minecraft.client.*;
import net.minecraft.realms.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.network.*;
import java.net.*;
import java.io.*;
import org.apache.logging.log4j.*;
import net.minecraftforge.common.*;
import journeymap.client.log.*;
import java.util.*;
import net.minecraft.client.entity.*;
import journeymap.client.*;
import journeymap.client.feature.*;
import journeymap.common.version.*;
import org.lwjgl.opengl.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.server.integrated.*;

public class WorldData extends CacheLoader<Class, WorldData>
{
    private static String DAYTIME;
    private static String SUNRISE;
    private static String SUNSET;
    private static String NIGHT;
    String name;
    int dimension;
    long time;
    boolean hardcore;
    boolean singlePlayer;
    String features;
    String jm_version;
    String latest_journeymap_version;
    String mc_version;
    String mod_name;
    String iconSetName;
    String[] iconSetNames;
    int browser_poll;
    
    public WorldData() {
        this.mod_name = JourneymapClient.MOD_NAME;
    }
    
    public static boolean isHardcoreAndMultiplayer() {
        final WorldData world = DataCache.INSTANCE.getWorld(false);
        return world.hardcore && !world.singlePlayer;
    }
    
    private static String getServerName() {
        try {
            String serverName = null;
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (!mc.func_71356_B()) {
                try {
                    final NetHandlerPlayClient netHandler = mc.func_147114_u();
                    final GuiScreen netHandlerGui = (GuiScreen)ReflectionHelper.getPrivateValue((Class)NetHandlerPlayClient.class, (Object)netHandler, new String[] { "field_147307_j", "guiScreenServer" });
                    if (netHandlerGui instanceof GuiScreenRealmsProxy) {
                        final RealmsScreen realmsScreen = ((GuiScreenRealmsProxy)netHandlerGui).func_154321_a();
                        if (realmsScreen instanceof RealmsMainScreen) {
                            final RealmsMainScreen mainScreen = (RealmsMainScreen)realmsScreen;
                            final long selectedServerId = (long)ReflectionHelper.getPrivateValue((Class)RealmsMainScreen.class, (Object)mainScreen, new String[] { "selectedServerId" });
                            final List<RealmsServer> mcoServers = (List<RealmsServer>)ReflectionHelper.getPrivateValue((Class)RealmsMainScreen.class, (Object)mainScreen, new String[] { "mcoServers" });
                            for (final RealmsServer mcoServer : mcoServers) {
                                if (mcoServer.id == selectedServerId) {
                                    serverName = mcoServer.name;
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (Throwable t) {
                    Journeymap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
                }
            }
            if (serverName != null) {
                return serverName;
            }
            mc = FMLClientHandler.instance().getClient();
            final ServerData serverData = mc.func_147104_D();
            if (serverData != null) {
                serverName = serverData.field_78847_a;
                if (serverName != null) {
                    serverName = serverName.replaceAll("\\W+", "~").trim();
                    if (Strings.isNullOrEmpty(serverName.replaceAll("~", ""))) {
                        serverName = serverData.field_78845_b;
                    }
                    return serverName;
                }
            }
            return null;
        }
        catch (Throwable t2) {
            Journeymap.getLogger().error("Couldn't get service name: " + LogFormatter.toString(t2));
            return getLegacyServerName();
        }
    }
    
    public static String getLegacyServerName() {
        try {
            final NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if (netManager != null) {
                final SocketAddress socketAddress = netManager.func_74430_c();
                if (socketAddress != null && socketAddress instanceof InetSocketAddress) {
                    final InetSocketAddress inetAddr = (InetSocketAddress)socketAddress;
                    return inetAddr.getHostName();
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Couldn't get server name: " + LogFormatter.toString(t));
        }
        return "server";
    }
    
    public static String getWorldName(final Minecraft mc, final boolean useLegacyName) {
        String worldName = null;
        if (mc.func_71356_B()) {
            if (!useLegacyName) {
                return mc.func_71401_C().func_71270_I();
            }
            worldName = mc.func_71401_C().func_71221_J();
        }
        else {
            worldName = Journeymap.clientWorld().func_72912_H().func_76065_j();
            final String serverName = getServerName();
            if (serverName == null) {
                return "offline";
            }
            if (!"MpServer".equals(worldName)) {
                worldName = serverName + "_" + worldName;
            }
            else {
                worldName = serverName;
            }
        }
        if (useLegacyName) {
            worldName = getLegacyUrlEncodedWorldName(worldName);
        }
        else {
            worldName = worldName.trim();
        }
        if (Strings.isNullOrEmpty(worldName.trim())) {
            worldName = "unnamed";
        }
        return worldName;
    }
    
    private static String getLegacyUrlEncodedWorldName(final String worldName) {
        try {
            return URLEncoder.encode(worldName, "UTF-8").replace("+", " ");
        }
        catch (UnsupportedEncodingException e) {
            return worldName;
        }
    }
    
    public static DimensionProvider getDimensionProvider(final int dimension) {
        for (final DimensionProvider provider : getDimensionProviders(Collections.singletonList(dimension))) {
            if (provider.getDimension() == dimension) {
                return provider;
            }
        }
        return new DummyProvider(dimension);
    }
    
    public static List<DimensionProvider> getDimensionProviders(final List<Integer> requiredDimensionList) {
        try {
            final HashSet<Integer> requiredDims = new HashSet<Integer>(requiredDimensionList);
            final HashMap<Integer, DimensionProvider> dimProviders = new HashMap<Integer, DimensionProvider>();
            final Level logLevel = Level.DEBUG;
            Journeymap.getLogger().log(logLevel, String.format("Required dimensions from waypoints: %s", requiredDimensionList));
            Integer[] dims = DimensionManager.getIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has dims: %s", Arrays.asList(dims)));
            requiredDims.addAll((Collection<?>)Arrays.asList(dims));
            dims = DimensionManager.getStaticDimensionIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has static dims: %s", Arrays.asList(dims)));
            requiredDims.addAll((Collection<?>)Arrays.asList(dims));
            final EntityPlayerSP player = Journeymap.clientPlayer();
            final WorldProvider playerProvider = player.func_130014_f_().field_73011_w;
            final int dimId = player.field_71093_bK;
            final DimensionProvider playerDimProvider = new WrappedProvider(playerProvider);
            dimProviders.put(dimId, playerDimProvider);
            requiredDims.remove(dimId);
            Journeymap.getLogger().log(logLevel, String.format("Using player's provider for dim %s: %s", dimId, getSafeDimensionName(playerDimProvider)));
            for (final int dim : requiredDims) {
                if (!dimProviders.containsKey(dim)) {
                    if (DimensionManager.getWorld(dim) != null) {
                        try {
                            final WorldProvider worldProvider = DimensionManager.getProvider(dim);
                            worldProvider.func_186058_p().func_186065_b();
                            final DimensionProvider dimProvider = new WrappedProvider(worldProvider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.getProvider(%s): %s", dim, getSafeDimensionName(dimProvider)));
                        }
                        catch (Throwable t) {
                            JMLogger.logOnce(String.format("Couldn't DimensionManager.getProvider(%s) because of error: %s", dim, t), t);
                        }
                    }
                    else {
                        try {
                            final WorldProvider provider = DimensionManager.createProviderFor(dim);
                            provider.func_186058_p().func_186065_b();
                            provider.setDimension(dim);
                            final DimensionProvider dimProvider = new WrappedProvider(provider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.createProviderFor(%s): %s", dim, getSafeDimensionName(dimProvider)));
                        }
                        catch (Throwable t2) {
                            JMLogger.logOnce(String.format("Couldn't DimensionManager.createProviderFor(%s) because of error: %s", dim, t2), t2);
                        }
                    }
                }
            }
            requiredDims.removeAll(dimProviders.keySet());
            for (final int dim : requiredDims) {
                if (!dimProviders.containsKey(dim)) {
                    dimProviders.put(dim, new DummyProvider(dim));
                    Journeymap.getLogger().warn(String.format("Used DummyProvider for required dim: %s", dim));
                }
            }
            final ArrayList<DimensionProvider> providerList = new ArrayList<DimensionProvider>(dimProviders.values());
            Collections.sort(providerList, new Comparator<DimensionProvider>() {
                @Override
                public int compare(final DimensionProvider o1, final DimensionProvider o2) {
                    return Integer.valueOf(o1.getDimension()).compareTo(Integer.valueOf(o2.getDimension()));
                }
            });
            return providerList;
        }
        catch (Throwable t3) {
            Journeymap.getLogger().error("Unexpected error in WorldData.getDimensionProviders(): ", t3);
            return Collections.emptyList();
        }
    }
    
    public static String getSafeDimensionName(final DimensionProvider dimensionProvider) {
        if (dimensionProvider == null || dimensionProvider.getName() == null) {
            return null;
        }
        try {
            return dimensionProvider.getName();
        }
        catch (Exception e) {
            return Constants.getString("jm.common.dimension", Journeymap.clientWorld().field_73011_w.getDimension());
        }
    }
    
    public WorldData load(final Class aClass) throws Exception {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final World world = Journeymap.clientWorld();
        final WorldInfo worldInfo = world.func_72912_H();
        final IntegratedServer server = mc.func_71401_C();
        final boolean multiplayer = server == null || server.func_71344_c();
        this.name = getWorldName(mc, false);
        this.dimension = world.field_73011_w.getDimension();
        this.hardcore = worldInfo.func_76093_s();
        this.singlePlayer = !multiplayer;
        this.time = world.func_72820_D() % 24000L;
        this.features = ClientFeatures.instance().getJson(this.dimension);
        this.mod_name = JourneymapClient.MOD_NAME;
        this.jm_version = Journeymap.JM_VERSION.toString();
        this.latest_journeymap_version = VersionCheck.getVersionAvailable();
        this.mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        this.browser_poll = Math.max(1000, Journeymap.getClient().getCoreProperties().browserPoll.get());
        return this;
    }
    
    public static String getGameTime() {
        final long worldTime = Journeymap.clientWorld().func_72820_D() % 24000L;
        String label;
        if (worldTime < 12000L) {
            label = WorldData.DAYTIME;
        }
        else if (worldTime < 13800L) {
            label = WorldData.SUNSET;
        }
        else if (worldTime < 22200L) {
            label = WorldData.NIGHT;
        }
        else {
            label = WorldData.SUNRISE;
        }
        final long allSecs = worldTime / 20L;
        return String.format("%02d:%02d %s", (long)Math.floor(allSecs / 60L), (long)Math.ceil(allSecs % 60L), label);
    }
    
    public static boolean isDay(final long worldTime) {
        return worldTime % 24000L < 13800L;
    }
    
    public static boolean isNight(final long worldTime) {
        return worldTime % 24000L >= 13800L;
    }
    
    public long getTTL() {
        return 1000L;
    }
    
    static {
        WorldData.DAYTIME = Constants.getString("jm.theme.labelsource.gametime.day");
        WorldData.SUNRISE = Constants.getString("jm.theme.labelsource.gametime.sunrise");
        WorldData.SUNSET = Constants.getString("jm.theme.labelsource.gametime.sunset");
        WorldData.NIGHT = Constants.getString("jm.theme.labelsource.gametime.night");
    }
    
    public static class WrappedProvider implements DimensionProvider
    {
        WorldProvider worldProvider;
        
        public WrappedProvider(final WorldProvider worldProvider) {
            this.worldProvider = worldProvider;
        }
        
        @Override
        public int getDimension() {
            return this.worldProvider.getDimension();
        }
        
        @Override
        public String getName() {
            return this.worldProvider.func_186058_p().func_186065_b();
        }
    }
    
    static class DummyProvider implements DimensionProvider
    {
        final int dim;
        
        DummyProvider(final int dim) {
            this.dim = dim;
        }
        
        @Override
        public int getDimension() {
            return this.dim;
        }
        
        @Override
        public String getName() {
            return Constants.getString("jm.waypoint.dimension", this.dim);
        }
    }
    
    public interface DimensionProvider
    {
        int getDimension();
        
        String getName();
    }
}
