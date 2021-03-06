package journeymap.client.task.multi;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.io.FileHandler;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.client.io.nbt.RegionLoader;
import journeymap.client.log.ChatLog;
import journeymap.client.model.*;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class MapRegionTask extends BaseMapTask {
    private static final int MAX_RUNTIME = 30000;
    private static final Logger logger;
    public static MapType MAP_TYPE;
    private static volatile long lastTaskCompleted;

    static {
        logger = Journeymap.getLogger();
    }

    final PolygonOverlay regionOverlay;
    final RegionCoord rCoord;
    final Collection<ChunkPos> retainedCoords;

    private MapRegionTask(final ChunkRenderController renderController, final World world, final MapType mapType, final RegionCoord rCoord, final Collection<ChunkPos> chunkCoords, final Collection<ChunkPos> retainCoords) {
        super(renderController, world, mapType, chunkCoords, true, false, 5000);
        this.rCoord = rCoord;
        this.retainedCoords = retainCoords;
        this.regionOverlay = this.createOverlay();
    }

    public static BaseMapTask create(final ChunkRenderController renderController, final RegionCoord rCoord, final MapType mapType, final Minecraft minecraft) {
        final World world = minecraft.world;
        final List<ChunkPos> renderCoords = rCoord.getChunkCoordsInRegion();
        final List<ChunkPos> retainedCoords = new ArrayList<>(renderCoords.size());
        final HashMap<RegionCoord, Boolean> existingRegions = new HashMap<>();
        for (final ChunkPos coord : renderCoords) {
            for (final ChunkPos keepAliveOffset : MapRegionTask.keepAliveOffsets) {
                final ChunkPos keepAliveCoord = new ChunkPos(coord.x + keepAliveOffset.x, coord.z + keepAliveOffset.z);
                final RegionCoord neighborRCoord = RegionCoord.fromChunkPos(rCoord.worldDir, mapType, keepAliveCoord.x, keepAliveCoord.z);
                if (!existingRegions.containsKey(neighborRCoord)) {
                    existingRegions.put(neighborRCoord, neighborRCoord.exists());
                }
                if (!renderCoords.contains(keepAliveCoord) && existingRegions.get(neighborRCoord)) {
                    retainedCoords.add(keepAliveCoord);
                }
            }
        }
        return new MapRegionTask(renderController, world, mapType, rCoord, renderCoords, retainedCoords);
    }

    @Override
    public final void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        ClientAPI.INSTANCE.show(this.regionOverlay);
        final AnvilChunkLoader loader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc), DataFixesManager.createFixer());
        int missing = 0;
        for (final ChunkPos coord : this.retainedCoords) {
            final ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord, true);
            if (chunkMD != null && chunkMD.hasChunk()) {
                DataCache.INSTANCE.addChunkMD(chunkMD);
            }
        }
        for (final ChunkPos coord : this.chunkCoords) {
            final ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord, true);
            if (chunkMD != null && chunkMD.hasChunk()) {
                DataCache.INSTANCE.addChunkMD(chunkMD);
            } else {
                ++missing;
            }
        }
        if (this.chunkCoords.size() - missing > 0) {
            try {
                MapRegionTask.logger.info(String.format("Potential chunks to map in %s: %s of %s", this.rCoord, this.chunkCoords.size() - missing, this.chunkCoords.size()));
                super.performTask(mc, jm, jmWorldDir, threadLogging);
            } finally {
                this.regionOverlay.getShapeProperties().setFillColor(16777215).setFillOpacity(0.15f).setStrokeColor(16777215);
                final String label = String.format("%s\nRegion [%s,%s]", Constants.getString("jm.common.automap_region_complete"), this.rCoord.regionX, this.rCoord.regionZ);
                this.regionOverlay.setLabel(label);
                this.regionOverlay.flagForRerender();
            }
        } else {
            MapRegionTask.logger.info(String.format("Skipping empty region: %s", this.rCoord));
        }
    }

    protected PolygonOverlay createOverlay() {
        final String displayId = "AutoMap" + this.rCoord;
        final String groupName = "AutoMap";
        final String label = String.format("%s\nRegion [%s,%s]", Constants.getString("jm.common.automap_region_start"), this.rCoord.regionX, this.rCoord.regionZ);
        final ShapeProperties shapeProps = new ShapeProperties().setStrokeWidth(2.0f).setStrokeColor(255).setStrokeOpacity(0.7f).setFillColor(65280).setFillOpacity(0.2f);
        final TextProperties textProps = new TextProperties().setBackgroundColor(34).setBackgroundOpacity(0.5f).setColor(65280).setOpacity(1.0f).setFontShadow(true);
        final int x = this.rCoord.getMinChunkX() << 4;
        final int y = 70;
        final int z = this.rCoord.getMinChunkZ() << 4;
        final int maxX = (this.rCoord.getMaxChunkX() << 4) + 15;
        final int maxZ = (this.rCoord.getMaxChunkZ() << 4) + 15;
        final BlockPos sw = new BlockPos(x, y, maxZ);
        final BlockPos se = new BlockPos(maxX, y, maxZ);
        final BlockPos ne = new BlockPos(maxX, y, z);
        final BlockPos nw = new BlockPos(x, y, z);
        final MapPolygon polygon = new MapPolygon(sw, se, ne, nw);
        final PolygonOverlay regionOverlay = new PolygonOverlay("journeymap", displayId, this.rCoord.dimension, shapeProps, polygon);
        regionOverlay.setOverlayGroupName(groupName).setLabel(label).setTextProperties(textProps).setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap)).setActiveMapTypes(EnumSet.of(Context.MapType.Any));
        return regionOverlay;
    }

    @Override
    protected void complete(final int mappedChunks, final boolean cancelled, final boolean hadError) {
        MapRegionTask.lastTaskCompleted = System.currentTimeMillis();
        RegionImageCache.INSTANCE.flushToDiskAsync(true);
        DataCache.INSTANCE.stopChunkMDRetention();
        if (hadError || cancelled) {
            MapRegionTask.logger.warn("MapRegionTask cancelled %s hadError %s", cancelled, hadError);
        } else {
            MapRegionTask.logger.info(String.format("Actual chunks mapped in %s: %s ", this.rCoord, mappedChunks));
            this.regionOverlay.setTitle(Constants.getString("jm.common.automap_region_chunks", mappedChunks));
        }
        long usedPct = this.getMemoryUsage();
        if (usedPct >= 85L) {
            MapRegionTask.logger.warn(String.format("Memory usage at %2d%%, forcing garbage collection", usedPct));
            System.gc();
            usedPct = this.getMemoryUsage();
        }
        MapRegionTask.logger.info(String.format("Memory usage at %2d%%", usedPct));
    }

    private long getMemoryUsage() {
        final long max = Runtime.getRuntime().maxMemory();
        final long total = Runtime.getRuntime().totalMemory();
        final long free = Runtime.getRuntime().freeMemory();
        return (total - free) * 100L / max;
    }

    @Override
    public int getMaxRuntime() {
        return 30000;
    }

    public static class Manager implements ITaskManager {
        final int mapTaskDelay = 0;
        RegionLoader regionLoader;
        boolean enabled;

        @Override
        public Class<? extends ITask> getTaskClass() {
            return MapRegionTask.class;
        }

        @Override
        public boolean enableTask(final Minecraft minecraft, final Object params) {
            final EntityDTO player = DataCache.getPlayer();
            final boolean cavesAllowed = FeatureManager.isAllowed(Feature.MapCaves);
            final boolean underground = player.underground;
            if (underground && !cavesAllowed) {
                MapRegionTask.logger.info("Cave mapping not permitted.");
                return false;
            }
            if (!(this.enabled = (params != null))) {
                return false;
            }
            if (System.currentTimeMillis() - MapRegionTask.lastTaskCompleted < Journeymap.getClient().getCoreProperties().autoMapPoll.get()) {
                return false;
            }
            this.enabled = false;
            if (minecraft.isIntegratedServerRunning()) {
                try {
                    MapType mapType = MapRegionTask.MAP_TYPE;
                    if (mapType == null) {
                        mapType = Fullscreen.state().getMapType();
                    }
                    final Boolean mapAll = params != null && (boolean) params;
                    this.regionLoader = new RegionLoader(minecraft, mapType, mapAll);
                    if (this.regionLoader.getRegionsFound() == 0) {
                        this.disableTask(minecraft);
                    } else {
                        this.enabled = true;
                    }
                } catch (Throwable t) {
                    final String error = "Couldn't Auto-Map: " + t.getMessage();
                    ChatLog.announceError(error);
                    MapRegionTask.logger.error(error + ": " + LogFormatter.toString(t));
                }
            }
            return this.enabled;
        }

        @Override
        public boolean isEnabled(final Minecraft minecraft) {
            return this.enabled;
        }

        @Override
        public void disableTask(final Minecraft minecraft) {
            if (this.regionLoader != null) {
                if (this.regionLoader.isUnderground()) {
                    ChatLog.announceI18N("jm.common.automap_complete_underground", this.regionLoader.getVSlice());
                } else {
                    ChatLog.announceI18N("jm.common.automap_complete");
                }
            }
            this.enabled = false;
            if (this.regionLoader != null) {
                RegionImageCache.INSTANCE.flushToDisk(true);
                RegionImageCache.INSTANCE.clear();
                this.regionLoader.getRegions().clear();
                this.regionLoader = null;
            }
            ClientAPI.INSTANCE.removeAll("journeymap", DisplayType.Polygon);
        }

        @Override
        public BaseMapTask getTask(final Minecraft minecraft) {
            if (!this.enabled) {
                return null;
            }
            if (this.regionLoader.getRegions().isEmpty()) {
                this.disableTask(minecraft);
                return null;
            }
            final RegionCoord rCoord = this.regionLoader.getRegions().peek();
            final ChunkRenderController chunkRenderController = Journeymap.getClient().getChunkRenderController();
            return MapRegionTask.create(chunkRenderController, rCoord, this.regionLoader.getMapType(), minecraft);
        }

        @Override
        public void taskAccepted(final ITask task, final boolean accepted) {
            if (accepted) {
                this.regionLoader.getRegions().pop();
                final float total = 1.0f * this.regionLoader.getRegionsFound();
                final float remaining = total - this.regionLoader.getRegions().size();
                final String percent = new DecimalFormat("##.#").format(remaining * 100.0f / total) + "%";
                if (this.regionLoader.isUnderground()) {
                    ChatLog.announceI18N("jm.common.automap_status_underground", this.regionLoader.getVSlice(), percent);
                } else {
                    ChatLog.announceI18N("jm.common.automap_status", percent);
                }
            }
        }
    }
}
