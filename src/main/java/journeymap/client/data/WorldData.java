package journeymap.client.data;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.log.JMLogger;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.version.VersionCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.Display;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.util.*;

public class WorldData extends CacheLoader<Class, WorldData> {
    private static String DAYTIME;
    private static String SUNRISE;
    private static String SUNSET;
    private static String NIGHT;

    static {
        WorldData.DAYTIME = Constants.getString("jm.theme.labelsource.gametime.day");
        WorldData.SUNRISE = Constants.getString("jm.theme.labelsource.gametime.sunrise");
        WorldData.SUNSET = Constants.getString("jm.theme.labelsource.gametime.sunset");
        WorldData.NIGHT = Constants.getString("jm.theme.labelsource.gametime.night");
    }

    String name;
    int dimension;
    long time;
    boolean hardcore;
    boolean singlePlayer;
    Map<Feature, Boolean> features;
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
            if (!mc.isSingleplayer()) {
                try {
                    final NetHandlerPlayClient netHandler = mc.getConnection();
                    final GuiScreen netHandlerGui = (GuiScreen) ReflectionHelper.getPrivateValue((Class) NetHandlerPlayClient.class, (Object) netHandler, new String[]{"field_147307_j", "guiScreenServer"});
                    if (netHandlerGui instanceof GuiScreenRealmsProxy) {
                        final RealmsScreen realmsScreen = ((GuiScreenRealmsProxy) netHandlerGui).getProxy();
                        if (realmsScreen instanceof RealmsMainScreen) {
                            final RealmsMainScreen mainScreen = (RealmsMainScreen) realmsScreen;
                            final long selectedServerId = (long) ReflectionHelper.getPrivateValue((Class) RealmsMainScreen.class, (Object) mainScreen, new String[]{"selectedServerId"});
                            final List<RealmsServer> mcoServers = (List<RealmsServer>) ReflectionHelper.getPrivateValue((Class) RealmsMainScreen.class, (Object) mainScreen, new String[]{"mcoServers"});
                            for (final RealmsServer mcoServer : mcoServers) {
                                if (mcoServer.id == selectedServerId) {
                                    serverName = mcoServer.name;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    Journeymap.getLogger().error("Unable to get Realms server name: " + LogFormatter.toString(t));
                }
            }
            if (serverName != null) {
                return serverName;
            }
            mc = FMLClientHandler.instance().getClient();
            final ServerData serverData = mc.getCurrentServerData();
            if (serverData != null) {
                serverName = serverData.serverName;
                if (serverName != null) {
                    serverName = serverName.replaceAll("\\W+", "~").trim();
                    if (Strings.isNullOrEmpty(serverName.replaceAll("~", ""))) {
                        serverName = serverData.serverIP;
                    }
                    return serverName;
                }
            }
            return null;
        } catch (Throwable t2) {
            Journeymap.getLogger().error("Couldn't get service name: " + LogFormatter.toString(t2));
            return getLegacyServerName();
        }
    }

    public static String getLegacyServerName() {
        try {
            final NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if (netManager != null) {
                final SocketAddress socketAddress = netManager.getRemoteAddress();
                if (socketAddress != null && socketAddress instanceof InetSocketAddress) {
                    final InetSocketAddress inetAddr = (InetSocketAddress) socketAddress;
                    return inetAddr.getHostName();
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Couldn't get server name: " + LogFormatter.toString(t));
        }
        return "server";
    }

    public static String getWorldName(final Minecraft mc, final boolean useLegacyName) {
        String worldName = null;
        if (mc.isSingleplayer()) {
            if (!useLegacyName) {
                return mc.getIntegratedServer().getFolderName();
            }
            worldName = mc.getIntegratedServer().getWorldName();
        } else {
            worldName = mc.world.getWorldInfo().getWorldName();
            final String serverName = getServerName();
            if (serverName == null) {
                return "offline";
            }
            if (!"MpServer".equals(worldName)) {
                worldName = serverName + "_" + worldName;
            } else {
                worldName = serverName;
            }
        }
        if (useLegacyName) {
            worldName = getLegacyUrlEncodedWorldName(worldName);
        } else {
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
        } catch (UnsupportedEncodingException e) {
            return worldName;
        }
    }

    public static List<DimensionProvider> getDimensionProviders(final List<Integer> requiredDimensionList) {
        try {
            final HashSet<Integer> requiredDims = new HashSet<>(requiredDimensionList);
            final HashMap<Integer, DimensionProvider> dimProviders = new HashMap<>();
            final Level logLevel = Level.DEBUG;
            Journeymap.getLogger().log(logLevel, String.format("Required dimensions from waypoints: %s", requiredDimensionList));
            Integer[] dims = DimensionManager.getIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));
            dims = DimensionManager.getStaticDimensionIDs();
            Journeymap.getLogger().log(logLevel, String.format("DimensionManager has static dims: %s", Arrays.asList(dims)));
            requiredDims.addAll(Arrays.asList(dims));
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final WorldProvider playerProvider = mc.player.world.provider;
            final int dimId = mc.player.dimension;
            final DimensionProvider playerDimProvider = new WrappedProvider(playerProvider);
            dimProviders.put(dimId, playerDimProvider);
            requiredDims.remove(dimId);
            Journeymap.getLogger().log(logLevel, String.format("Using player's provider for dim %s: %s", dimId, getSafeDimensionName(playerDimProvider)));
            for (final int dim : requiredDims) {
                if (!dimProviders.containsKey(dim)) {
                    if (DimensionManager.getWorld(dim) != null) {
                        try {
                            final WorldProvider worldProvider = DimensionManager.getProvider(dim);
                            worldProvider.getDimensionType().getName();
                            final DimensionProvider dimProvider = new WrappedProvider(worldProvider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.getProvider(%s): %s", dim, getSafeDimensionName(dimProvider)));
                        } catch (Throwable t) {
                            JMLogger.logOnce(String.format("Couldn't DimensionManager.getProvider(%s) because of error: %s", dim, t), t);
                        }
                    } else {
                        try {
                            final WorldProvider provider = DimensionManager.createProviderFor(dim);
                            provider.getDimensionType().getName();
                            provider.setDimension(dim);
                            final DimensionProvider dimProvider = new WrappedProvider(provider);
                            dimProviders.put(dim, dimProvider);
                            Journeymap.getLogger().log(logLevel, String.format("DimensionManager.createProviderFor(%s): %s", dim, getSafeDimensionName(dimProvider)));
                        } catch (Throwable t2) {
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
            final ArrayList<DimensionProvider> providerList = new ArrayList<>(dimProviders.values());
            providerList.sort(Comparator.comparing(DimensionProvider::getDimension));
            return providerList;
        } catch (Throwable t3) {
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
        } catch (Exception e) {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            return Constants.getString("jm.common.dimension", mc.world.provider.getDimension());
        }
    }

    public static String getGameTime() {
        final long worldTime = FMLClientHandler.instance().getClient().world.getWorldTime() % 24000L;
        String label;
        if (worldTime < 12000L) {
            label = WorldData.DAYTIME;
        } else if (worldTime < 13800L) {
            label = WorldData.SUNSET;
        } else if (worldTime < 22200L) {
            label = WorldData.NIGHT;
        } else {
            label = WorldData.SUNRISE;
        }
        final long allSecs = worldTime / 20L;
        return String.format("%02d:%02d %s", (long) Math.floor(allSecs / 60L), (long) Math.ceil(allSecs % 60L), label);
    }

    public static boolean isDay(final long worldTime) {
        return worldTime % 24000L < 13800L;
    }

    public static boolean isNight(final long worldTime) {
        return worldTime % 24000L >= 13800L;
    }

    public WorldData load(final Class aClass) throws Exception {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final WorldInfo worldInfo = mc.world.getWorldInfo();
        final IntegratedServer server = mc.getIntegratedServer();
        final boolean multiplayer = server == null || server.getPublic();
        this.name = getWorldName(mc, false);
        this.dimension = mc.world.provider.getDimension();
        this.hardcore = worldInfo.isHardcoreModeEnabled();
        this.singlePlayer = !multiplayer;
        this.time = mc.world.getWorldTime() % 24000L;
        this.features = FeatureManager.getAllowedFeatures();
        this.mod_name = JourneymapClient.MOD_NAME;
        this.jm_version = Journeymap.JM_VERSION.toString();
        this.latest_journeymap_version = VersionCheck.getVersionAvailable();
        this.mc_version = Display.getTitle().split("\\s(?=\\d)")[1];
        this.browser_poll = Math.max(1000, Journeymap.getClient().getCoreProperties().browserPoll.get());
        return this;
    }

    public long getTTL() {
        return 1000L;
    }

    public interface DimensionProvider {
        int getDimension();

        String getName();
    }

    public static class WrappedProvider implements DimensionProvider {
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
            return this.worldProvider.getDimensionType().getName();
        }
    }

    static class DummyProvider implements DimensionProvider {
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
            return "Dimension " + this.dim;
        }
    }
}
