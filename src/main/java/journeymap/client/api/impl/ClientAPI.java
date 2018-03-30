package journeymap.client.api.impl;

import com.google.common.base.Strings;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.ClientPluginHelper;
import journeymap.client.api.util.UIState;
import journeymap.client.io.FileHandler;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.task.multi.ApiImageTask;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public enum ClientAPI implements IClientAPI {
    INSTANCE;

    private final Logger LOGGER;
    private final List<OverlayDrawStep> lastDrawSteps;
    private HashMap<String, ClientPluginWrapper> plugins;
    private ClientEventManager clientEventManager;
    private boolean drawStepsUpdateNeeded;
    private Feature.Display lastUi;
    private Feature.MapType lastMapType;
    private int lastDimension;

    private ClientAPI() {
        this.LOGGER = Journeymap.getLogger();
        this.lastDrawSteps = new ArrayList<>();
        this.plugins = new HashMap<>();
        this.clientEventManager = new ClientEventManager(this.plugins.values());
        this.drawStepsUpdateNeeded = true;
        this.lastUi = null;
        this.lastMapType = null;
        this.lastDimension = Integer.MIN_VALUE;
        this.log("built with JourneyMap API 2.0-SNAPSHOT");
    }

    @Override
    public UIState getUIState(final Feature.Display ui) {
        switch (ui) {
            case Minimap: {
                return MiniMap.uiState();
            }
            case Fullscreen: {
                return Fullscreen.uiState();
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void subscribe(final String modId, final EnumSet<ClientEvent.Type> enumSet) {
        try {
            this.getPlugin(modId).subscribe(enumSet);
            this.clientEventManager.updateSubscribedTypes();
        } catch (Throwable t) {
            this.logError("Error subscribing: " + t, t);
        }
    }

    @Override
    public void show(final Displayable displayable) {
        try {
            if (this.playerAccepts(displayable)) {
                this.getPlugin(displayable.getModId()).show(displayable);
                this.drawStepsUpdateNeeded = true;
            }
        } catch (Throwable t) {
            this.logError("Error showing displayable: " + displayable, t);
        }
    }

    @Override
    public void remove(final Displayable displayable) {
        try {
            if (this.playerAccepts(displayable)) {
                this.getPlugin(displayable.getModId()).remove(displayable);
                this.drawStepsUpdateNeeded = true;
            }
        } catch (Throwable t) {
            this.logError("Error removing displayable: " + displayable, t);
        }
    }

    @Override
    public void removeAll(final String modId, final DisplayType displayType) {
        try {
            if (this.playerAccepts(modId, displayType)) {
                this.getPlugin(modId).removeAll(displayType);
                this.drawStepsUpdateNeeded = true;
            }
        } catch (Throwable t) {
            this.logError("Error removing all displayables: " + displayType, t);
        }
    }

    @Override
    public void removeAll(final String modId) {
        try {
            for (final DisplayType displayType : DisplayType.values()) {
                this.removeAll(modId, displayType);
                this.drawStepsUpdateNeeded = true;
            }
            this.getPlugin(modId).removeAll();
        } catch (Throwable t) {
            this.logError("Error removing all displayables for mod: " + modId, t);
        }
    }

    public void purge() {
        try {
            this.drawStepsUpdateNeeded = true;
            this.lastDrawSteps.clear();
            this.plugins.clear();
            this.clientEventManager.purge();
        } catch (Throwable t) {
            this.logError("Error purging: " + t, t);
        }
    }

    @Override
    public boolean exists(final Displayable displayable) {
        try {
            if (this.playerAccepts(displayable)) {
                return this.getPlugin(displayable.getModId()).exists(displayable);
            }
        } catch (Throwable t) {
            this.logError("Error checking exists: " + displayable, t);
        }
        return false;
    }

    @Override
    public boolean playerAccepts(final String modId, final DisplayType displayType) {
        return true;
    }

    @Override
    public void requestMapTile(final String modId, final int dimension, final Feature.MapType apiMapType, final ChunkPos startChunk, final ChunkPos endChunk, @Nullable final Integer chunkY, final int zoom, final boolean showGrid, final Consumer<BufferedImage> callback) {
        this.log("requestMapTile");
        boolean honorRequest = true;
        final File worldDir = FileHandler.getJMWorldDir(Minecraft.getMinecraft());
        if (!Objects.equals("jmitems", modId)) {
            honorRequest = false;
            this.logError("requestMapTile not supported");
        } else if (worldDir == null || !worldDir.exists() || !worldDir.isDirectory()) {
            honorRequest = false;
            this.logError("world directory not found: " + worldDir);
        }
        try {
            if (honorRequest) {
                Journeymap.getClient().queueOneOff(new ApiImageTask(modId, dimension, apiMapType, startChunk, endChunk, chunkY, zoom, showGrid, callback));
            } else {
                Minecraft.getMinecraft().addScheduledTask(() -> callback.accept(null));
            }
        } catch (Exception e) {
            callback.accept(null);
        }
    }

    private boolean playerAccepts(final Displayable displayable) {
        return this.playerAccepts(displayable.getModId(), displayable.getDisplayType());
    }

    public ClientEventManager getClientEventManager() {
        return this.clientEventManager;
    }

    public void getDrawSteps(final List<? super OverlayDrawStep> list, final UIState uiState) {
        if (uiState.ui != this.lastUi || uiState.dimension != this.lastDimension || uiState.mapType != this.lastMapType) {
            this.drawStepsUpdateNeeded = true;
            this.lastUi = uiState.ui;
            this.lastDimension = uiState.dimension;
            this.lastMapType = uiState.mapType;
        }
        if (this.drawStepsUpdateNeeded) {
            this.lastDrawSteps.clear();
            for (final ClientPluginWrapper pluginWrapper : this.plugins.values()) {
                pluginWrapper.getDrawSteps(this.lastDrawSteps, uiState);
            }
            this.lastDrawSteps.sort(Comparator.comparingInt(DrawStep::getDisplayOrder));
            this.drawStepsUpdateNeeded = false;
        }
        list.addAll(this.lastDrawSteps);
    }

    @Override
    public boolean isDisplayEnabled(final int dimension, final Feature.Display display) {
        return true;
    }

    @Override
    public boolean isMapTypeEnabled(final int dimension, final Feature.MapType mapType) {
        return true;
    }

    @Override
    public boolean isRadarEnabled(final int dimension, final Feature.Radar radar) {
        return true;
    }

    private ClientPluginWrapper getPlugin(final String modId) {
        if (Strings.isNullOrEmpty(modId)) {
            throw new IllegalArgumentException("Invalid modId: " + modId);
        }
        final IClientPlugin[] plugin = new IClientPlugin[1];
        return this.plugins.computeIfAbsent(modId, key -> {
            plugin[0] = ClientPluginHelper.instance().getPlugins().get(modId);
            if (plugin[0] == null) {
                if (modId.equals("journeymap")) {
                    plugin[0] = new IClientPlugin() {
                        @Override
                        public void initialize(final IClientAPI jmClientApi) {
                        }

                        @Override
                        public String getModId() {
                            return "journeymap";
                        }

                        @Override
                        public void onEvent(final ClientEvent event) {
                        }
                    };
                } else {
                    this.logError("No plugin found for modId: " + modId);
                }
            }
            if (plugin[0] != null) {
                return new ClientPluginWrapper(plugin[0]);
            } else {
                return null;
            }
        });
    }

    public boolean isDrawStepsUpdateNeeded() {
        return this.drawStepsUpdateNeeded;
    }

    void log(final String message) {
        this.LOGGER.info(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }

    private void logError(final String message) {
        this.LOGGER.error(String.format("[%s] %s", this.getClass().getSimpleName(), message));
    }

    void logError(final String message, final Throwable t) {
        this.LOGGER.error(String.format("[%s] %s", this.getClass().getSimpleName(), message), t);
    }

    public void flagOverlaysForRerender() {
        for (final OverlayDrawStep overlayDrawStep : this.lastDrawSteps) {
            overlayDrawStep.getOverlay().flagForRerender();
        }
    }
}
