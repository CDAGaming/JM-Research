package journeymap.client.task.multi;

import org.apache.logging.log4j.*;
import journeymap.client.cartography.*;
import net.minecraft.world.*;
import net.minecraft.client.*;
import java.io.*;
import journeymap.client.api.impl.*;
import journeymap.client.io.*;
import net.minecraft.util.datafix.*;
import net.minecraft.world.chunk.storage.*;
import journeymap.client.data.*;
import journeymap.client.*;
import net.minecraft.util.math.*;
import journeymap.client.api.model.*;
import journeymap.common.api.feature.*;
import java.util.*;
import journeymap.client.model.*;
import journeymap.common.*;
import journeymap.client.io.nbt.*;
import journeymap.client.ui.fullscreen.*;
import journeymap.client.log.*;
import journeymap.common.log.*;
import journeymap.client.api.display.*;
import java.text.*;

public class MapRegionTask extends BaseMapTask
{
    private static final int MAX_RUNTIME = 30000;
    private static final Logger logger;
    private static volatile long lastTaskCompleted;
    public static MapView MAP_VIEW;
    final PolygonOverlay regionOverlay;
    final RegionCoord rCoord;
    final Collection<ChunkPos> retainedCoords;
    
    private MapRegionTask(final ChunkRenderController renderController, final World world, final MapView mapView, final RegionCoord rCoord, final Collection<ChunkPos> chunkCoords, final Collection<ChunkPos> retainCoords) {
        super(renderController, world, mapView, chunkCoords, true, false, 5000);
        this.rCoord = rCoord;
        this.retainedCoords = retainCoords;
        this.regionOverlay = this.createOverlay();
    }
    
    public static BaseMapTask create(final ChunkRenderController renderController, final RegionCoord rCoord, final MapView mapView, final Minecraft minecraft) {
        final World world = (World)minecraft.field_71441_e;
        final List<ChunkPos> renderCoords = rCoord.getChunkCoordsInRegion();
        final List<ChunkPos> retainedCoords = new ArrayList<ChunkPos>(renderCoords.size());
        final HashMap<RegionCoord, Boolean> existingRegions = new HashMap<RegionCoord, Boolean>();
        for (final ChunkPos coord : renderCoords) {
            for (final ChunkPos keepAliveOffset : MapRegionTask.keepAliveOffsets) {
                final ChunkPos keepAliveCoord = new ChunkPos(coord.field_77276_a + keepAliveOffset.field_77276_a, coord.field_77275_b + keepAliveOffset.field_77275_b);
                final RegionCoord neighborRCoord = RegionCoord.fromChunkPos(rCoord.worldDir, mapView, keepAliveCoord.field_77276_a, keepAliveCoord.field_77275_b);
                if (!existingRegions.containsKey(neighborRCoord)) {
                    existingRegions.put(neighborRCoord, neighborRCoord.exists());
                }
                if (!renderCoords.contains(keepAliveCoord) && existingRegions.get(neighborRCoord)) {
                    retainedCoords.add(keepAliveCoord);
                }
            }
        }
        return new MapRegionTask(renderController, world, mapView, rCoord, renderCoords, retainedCoords);
    }
    
    @Override
    public final void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        ClientAPI.INSTANCE.show(this.regionOverlay);
        final AnvilChunkLoader loader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc), DataFixesManager.func_188279_a());
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
            }
            else {
                ++missing;
            }
        }
        if (this.chunkCoords.size() - missing > 0) {
            try {
                MapRegionTask.logger.info(String.format("Potential chunks to map in %s: %s of %s", this.rCoord, this.chunkCoords.size() - missing, this.chunkCoords.size()));
                super.performTask(mc, jm, jmWorldDir, threadLogging);
            }
            finally {
                this.regionOverlay.getShapeProperties().setFillColor(16777215).setFillOpacity(0.15f).setStrokeColor(16777215);
                final String label = String.format("%s\nRegion [%s,%s]", Constants.getString("jm.common.automap_region_complete"), this.rCoord.regionX, this.rCoord.regionZ);
                this.regionOverlay.setLabel(label);
                this.regionOverlay.flagForRerender();
            }
        }
        else {
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
        final MapPolygon polygon = new MapPolygon(new BlockPos[] { sw, se, ne, nw });
        final PolygonOverlay regionOverlay = new PolygonOverlay("journeymap", displayId, this.rCoord.dimension, shapeProps, polygon);
        regionOverlay.setOverlayGroupName(groupName).setLabel(label).setTextProperties(textProps).setActiveUIs(EnumSet.of(Feature.Display.Fullscreen, Feature.Display.Webmap)).setActiveMapTypes(EnumSet.allOf(Feature.MapType.class));
        return regionOverlay;
    }
    
    @Override
    protected void complete(final int mappedChunks, final boolean cancelled, final boolean hadError) {
        MapRegionTask.lastTaskCompleted = System.currentTimeMillis();
        RegionImageCache.INSTANCE.flushToDiskAsync(true);
        DataCache.INSTANCE.stopChunkMDRetention();
        if (hadError || cancelled) {
            MapRegionTask.logger.warn("MapRegionTask cancelled %s hadError %s", (Object)cancelled, (Object)hadError);
        }
        else {
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
    
    static {
        logger = Journeymap.getLogger();
    }
    
    public static class Manager implements ITaskManager
    {
        final int mapTaskDelay = 0;
        RegionLoader regionLoader;
        boolean enabled;
        
        @Override
        public Class<? extends ITask> getTaskClass() {
            return MapRegionTask.class;
        }
        
        @Override
        public boolean enableTask(final Minecraft minecraft, final Object params) {
            if (!(this.enabled = (params != null))) {
                return false;
            }
            if (System.currentTimeMillis() - MapRegionTask.lastTaskCompleted < Journeymap.getClient().getCoreProperties().autoMapPoll.get()) {
                return false;
            }
            this.enabled = false;
            if (minecraft.func_71387_A()) {
                try {
                    MapView mapView = MapRegionTask.MAP_VIEW;
                    if (mapView == null) {
                        mapView = Fullscreen.state().getMapView();
                    }
                    if (mapView == null || mapView.isNone()) {
                        return false;
                    }
                    if (mapView.isAllowed()) {
                        final Boolean mapAll = (Boolean)params;
                        this.regionLoader = new RegionLoader(minecraft, mapView, mapAll);
                        if (this.regionLoader.getRegionsFound() == 0) {
                            this.disableTask(minecraft);
                        }
                        else {
                            this.enabled = true;
                        }
                    }
                    else {
                        final String error = "Not allowed to Auto-Map: " + mapView;
                        ChatLog.announceError(error);
                    }
                }
                catch (Throwable t) {
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
                }
                else {
                    ChatLog.announceI18N("jm.common.automap_complete", new Object[0]);
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
            final BaseMapTask baseMapTask = MapRegionTask.create(chunkRenderController, rCoord, this.regionLoader.getMapType(), minecraft);
            return baseMapTask;
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
                }
                else {
                    ChatLog.announceI18N("jm.common.automap_status", percent);
                }
            }
        }
    }
}
