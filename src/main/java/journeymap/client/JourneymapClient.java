package journeymap.client;

import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.util.ClientPluginHelper;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.cartography.color.ColorPalette;
import journeymap.client.data.DataCache;
import journeymap.client.forge.event.EventHandlerManager;
import journeymap.client.io.FileHandler;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.ChatLog;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.model.RegionImageCache;
import journeymap.client.network.WorldInfoHandler;
import journeymap.client.properties.*;
import journeymap.client.render.map.TileDrawStepCache;
import journeymap.client.service.WebServer;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.task.main.MainTaskController;
import journeymap.client.task.main.MappingMonitorTask;
import journeymap.client.task.multi.ITaskManager;
import journeymap.client.task.multi.TaskController;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.command.CommandJTP;
import journeymap.common.log.LogFormatter;
import journeymap.common.migrate.Migration;
import journeymap.common.network.PacketHandler;
import journeymap.common.version.VersionCheck;
import journeymap.server.JourneymapServer;
import modinfo.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class JourneymapClient implements CommonProxy {
    public static final String FULL_VERSION;
    public static final String MOD_NAME;

    static {
        FULL_VERSION = "1.12.2-" + Journeymap.JM_VERSION;
        MOD_NAME = "JourneyMap " + JourneymapClient.FULL_VERSION;
    }

    private final MainTaskController mainThreadTaskController;
    public volatile ASMDataTable asmDataTable;
    private boolean serverEnabled;
    private volatile CoreProperties coreProperties;
    private volatile FullMapProperties fullMapProperties;
    private volatile MiniMapProperties miniMapProperties1;
    private volatile MiniMapProperties miniMapProperties2;
    private volatile TopoProperties topoProperties;
    private volatile WebMapProperties webMapProperties;
    private volatile WaypointProperties waypointProperties;
    private volatile Boolean initialized;
    private volatile String currentWorldId;
    private Logger logger;
    private boolean threadLogging;
    private TaskController multithreadTaskController;
    private ChunkRenderController chunkRenderController;

    public JourneymapClient() {
        this.serverEnabled = false;
        this.initialized = false;
        this.currentWorldId = null;
        this.threadLogging = false;
        this.mainThreadTaskController = new MainTaskController();
    }

    public static GameType getGameType() {
        return FMLClientHandler.instance().getClient().playerController.getCurrentGameType();
    }

    public CoreProperties getCoreProperties() {
        return this.coreProperties;
    }

    public FullMapProperties getFullMapProperties() {
        return this.fullMapProperties;
    }

    public TopoProperties getTopoProperties() {
        return this.topoProperties;
    }

    public void disable() {
        this.initialized = false;
        EventHandlerManager.unregisterAll();
        this.stopMapping();
        ClientAPI.INSTANCE.purge();
        DataCache.INSTANCE.purge();
    }

    public MiniMapProperties getMiniMapProperties(final int which) {
        switch (which) {
            case 2: {
                this.miniMapProperties2.setActive(true);
                this.miniMapProperties1.setActive(false);
                return this.getMiniMapProperties2();
            }
            default: {
                this.miniMapProperties1.setActive(true);
                this.miniMapProperties2.setActive(false);
                return this.getMiniMapProperties1();
            }
        }
    }

    public int getActiveMinimapId() {
        if (this.miniMapProperties1.isActive()) {
            return 1;
        }
        return 2;
    }

    public MiniMapProperties getMiniMapProperties1() {
        return this.miniMapProperties1;
    }

    public MiniMapProperties getMiniMapProperties2() {
        return this.miniMapProperties2;
    }

    public WebMapProperties getWebMapProperties() {
        return this.webMapProperties;
    }

    public WaypointProperties getWaypointProperties() {
        return this.waypointProperties;
    }

    @Override
    public void preInitialize(final FMLPreInitializationEvent event) throws Throwable {
        try {
            this.asmDataTable = event.getAsmData();
            ClientPluginHelper.instance().preInitPlugins(this.asmDataTable);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void initialize(final FMLInitializationEvent event) throws Throwable {
        PacketHandler.init(Side.CLIENT);
        StatTimer timer;
        try {
            timer = StatTimer.getDisposable("elapsed").start();
            final boolean migrationOk = new Migration("journeymap.client.task.migrate").performTasks();
            (this.logger = JMLogger.init()).info("initialize ENTER");
            if (this.initialized) {
                this.logger.warn("Already initialized, aborting");
                return;
            }
            EntityRegistry.instance();
            this.loadConfigProperties();
            JMLogger.logProperties();
            EventHandlerManager.registerHandlers();
            this.threadLogging = false;
            ClientPluginHelper.instance().initPlugins(ClientAPI.INSTANCE);
            this.logger.info("initialize EXIT, " + ((timer == null) ? "" : timer.getLogReportString()));
        } catch (Throwable t) {
            if (this.logger == null) {
                this.logger = LogManager.getLogger("journeymap");
            }
            this.logger.error(LogFormatter.toString(t));
            throw t;
        }
    }

    @Override
    public void postInitialize(final FMLPostInitializationEvent event) {
        StatTimer timer = null;
        try {
            this.logger.debug("postInitialize ENTER");
            timer = StatTimer.getDisposable("elapsed").start();
            this.queueMainThreadTask(new MappingMonitorTask());
            ThemeLoader.initialize(true);
            WebServer.setEnabled(this.webMapProperties.enabled.get(), false);
            this.initialized = true;
            VersionCheck.getVersionAvailable();
            final ModInfo modInfo = new ModInfo("UA-28839029-5", "en_US", "journeymap", JourneymapClient.MOD_NAME, JourneymapClient.FULL_VERSION, false);
            modInfo.reportAppView();
        } catch (Throwable t) {
            if (this.logger == null) {
                this.logger = LogManager.getLogger("journeymap");
            }
            this.logger.error(LogFormatter.toString(t));
        } finally {
            this.logger.debug("postInitialize EXIT, " + ((timer == null) ? "" : timer.stopAndReport()));
        }
        JMLogger.setLevelFromProperties();
    }

    @Override
    public void serverStartingEvent(final FMLServerStartingEvent event) throws Throwable {
        event.registerServerCommand(new CommandJTP());
        if (!event.getServer().isDedicatedServer() && this.asmDataTable != null) {
            JourneymapServer.preInitialize(this.asmDataTable);
            JourneymapServer.initialize();
        }
    }

    @Override
    public boolean checkModLists(final Map<String, String> modList, final Side side) {
        return true;
    }

    @Override
    public boolean isUpdateCheckEnabled() {
        return this.getCoreProperties().checkUpdates.get();
    }

    public Boolean isInitialized() {
        return this.initialized;
    }

    public Boolean isMapping() {
        return this.initialized && this.multithreadTaskController != null && this.multithreadTaskController.isActive();
    }

    public Boolean isThreadLogging() {
        return this.threadLogging;
    }

    public WebServer getJmServer() {
        return WebServer.getInstance();
    }

    public void queueOneOff(final Runnable runnable) throws Exception {
        if (this.multithreadTaskController != null) {
            this.multithreadTaskController.queueOneOff(runnable);
        }
    }

    public void toggleTask(final Class<? extends ITaskManager> managerClass, final boolean enable, final Object params) {
        if (this.multithreadTaskController != null) {
            this.multithreadTaskController.toggleTask(managerClass, enable, params);
        }
    }

    public boolean isTaskManagerEnabled(final Class<? extends ITaskManager> managerClass) {
        return this.multithreadTaskController != null && this.multithreadTaskController.isTaskManagerEnabled(managerClass);
    }

    public boolean isMainThreadTaskActive() {
        return this.mainThreadTaskController != null && this.mainThreadTaskController.isActive();
    }

    public void startMapping() {
        synchronized (this) {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc == null || this.world() == null || !this.initialized || !this.coreProperties.mappingEnabled.get()) {
                return;
            }
            final File worldDir = FileHandler.getJMWorldDir(mc, this.currentWorldId);
            if (worldDir == null) {
                return;
            }
            if (!worldDir.exists()) {
                final boolean created = worldDir.mkdirs();
                if (!created) {
                    JMLogger.logOnce("CANNOT CREATE DATA DIRECTORY FOR WORLD: " + worldDir.getPath(), null);
                    return;
                }
            }
            this.reset();
            (this.multithreadTaskController = new TaskController()).enableTasks();
            final long totalMB = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
            final long freeMB = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
            final String memory = String.format("Memory: %sMB total, %sMB free", totalMB, freeMB);
            final int dimension = this.world().provider.getDimension();
            this.logger.info(String.format("Mapping started in %s%sDIM%s. %s ", FileHandler.getJMWorldDir(mc, this.currentWorldId), File.separator, dimension, memory));
            ClientAPI.INSTANCE.getClientEventManager().fireMappingEvent(true, dimension);
            UIManager.INSTANCE.getMiniMap().reset();
        }
    }

    public void stopMapping() {
        synchronized (this) {
            DataCache.INSTANCE.invalidateChunkMDCache();
            final Minecraft mc = FMLClientHandler.instance().getClient();
            if (this.isMapping() && mc != null) {
                this.logger.info(String.format("Mapping halted in %s%sDIM%s", FileHandler.getJMWorldDir(mc, this.currentWorldId), File.separator, this.world().provider.getDimension()));
                RegionImageCache.INSTANCE.flushToDiskAsync(true);
                final ColorPalette colorPalette = ColorPalette.getActiveColorPalette();
                if (colorPalette != null) {
                    colorPalette.writeToFile();
                }
            }
            if (this.multithreadTaskController != null) {
                this.multithreadTaskController.disableTasks();
                this.multithreadTaskController.clear();
                this.multithreadTaskController = null;
            }
            if (mc != null) {
                final int dimension = (this.world() != null) ? this.world().provider.getDimension() : 0;
                ClientAPI.INSTANCE.getClientEventManager().fireMappingEvent(false, dimension);
            }
        }
    }

    private void reset() {
        if (!FMLClientHandler.instance().getClient().isSingleplayer() && this.currentWorldId == null) {
            WorldInfoHandler.requestWorldID();
        }
        this.loadConfigProperties();
        DataCache.INSTANCE.purge();
        this.chunkRenderController = new ChunkRenderController();
        Fullscreen.state().requireRefresh();
        Fullscreen.state().follow.set(true);
        StatTimer.resetAll();
        TileDrawStepCache.clear();
        UIManager.INSTANCE.getMiniMap().reset();
        UIManager.INSTANCE.reset();
        WaypointStore.INSTANCE.reset();
    }

    public void queueMainThreadTask(final IMainThreadTask task) {
        this.mainThreadTaskController.addTask(task);
    }

    public void performMainThreadTasks() {
        this.mainThreadTaskController.performTasks();
    }

    public void performMultithreadTasks() {
        try {
            synchronized (this) {
                if (this.isMapping()) {
                    this.multithreadTaskController.performTasks();
                }
            }
        } catch (Throwable t) {
            final String error = "Error in JourneyMap.performMultithreadTasks(): " + t.getMessage();
            ChatLog.announceError(error);
            this.logger.error(LogFormatter.toString(t));
        }
    }

    public ChunkRenderController getChunkRenderController() {
        return this.chunkRenderController;
    }

    public void saveConfigProperties() {
        if (this.coreProperties != null) {
            this.coreProperties.save();
        }
        if (this.fullMapProperties != null) {
            this.fullMapProperties.save();
        }
        if (this.miniMapProperties1 != null) {
            this.miniMapProperties1.save();
        }
        if (this.miniMapProperties2 != null) {
            this.miniMapProperties2.save();
        }
        if (this.miniMapProperties2 != null) {
            this.miniMapProperties2.save();
        }
        if (this.topoProperties != null) {
            this.topoProperties.save();
        }
        if (this.webMapProperties != null) {
            this.webMapProperties.save();
        }
        if (this.waypointProperties != null) {
            this.waypointProperties.save();
        }
    }

    public void loadConfigProperties() {
        this.saveConfigProperties();
        this.coreProperties = new CoreProperties().load();
        this.fullMapProperties = new FullMapProperties().load();
        this.miniMapProperties1 = new MiniMapProperties(1).load();
        this.miniMapProperties2 = new MiniMapProperties(2).load();
        this.topoProperties = new TopoProperties().load();
        this.webMapProperties = new WebMapProperties().load();
        this.waypointProperties = new WaypointProperties().load();
    }

    @Override
    public void handleWorldIdMessage(final String worldId, final EntityPlayerMP playerEntity) {
        this.setCurrentWorldId(worldId);
    }

    public String getCurrentWorldId() {
        return this.currentWorldId;
    }

    public void setCurrentWorldId(final String worldId) {
        synchronized (this) {
            final Minecraft mc = FMLClientHandler.instance().getClient();
            final File currentWorldDirectory = FileHandler.getJMWorldDirForWorldId(mc, this.currentWorldId);
            final File newWorldDirectory = FileHandler.getJMWorldDir(mc, worldId);
            final boolean worldIdUnchanged = Constants.safeEqual(worldId, this.currentWorldId);
            final boolean directoryUnchanged = currentWorldDirectory != null && newWorldDirectory != null && currentWorldDirectory.getPath().equals(newWorldDirectory.getPath());
            if (worldIdUnchanged && directoryUnchanged && worldId != null) {
                Journeymap.getLogger().info("World UID hasn't changed: " + worldId);
                return;
            }
            final boolean wasMapping = this.isMapping();
            if (wasMapping) {
                this.stopMapping();
            }
            this.currentWorldId = worldId;
            Journeymap.getLogger().info("World UID is set to: " + worldId);
        }
    }

    public boolean isServerEnabled() {
        return this.serverEnabled;
    }

    public void setServerEnabled(final boolean serverEnabled) {
        this.serverEnabled = serverEnabled;
    }

    public WorldClient world() {
        return FMLClientHandler.instance().getClient().world;
    }

    public EntityPlayerSP player() {
        return FMLClientHandler.instance().getClient().player;
    }
}
