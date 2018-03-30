package journeymap.client.task.multi;

import java.text.*;
import journeymap.client.cartography.*;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import journeymap.common.*;
import journeymap.client.data.*;
import net.minecraft.entity.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.client.properties.*;
import net.minecraft.client.*;
import journeymap.client.*;
import java.io.*;
import journeymap.client.model.*;
import com.google.common.cache.*;
import java.util.concurrent.*;
import net.minecraft.client.entity.*;
import java.util.*;

public class MapPlayerTask extends BaseMapTask
{
    private static DecimalFormat decFormat;
    private static volatile long lastTaskCompleted;
    private static long lastTaskTime;
    private static double lastTaskAvgChunkTime;
    private static Cache<String, String> tempDebugLines;
    private final int maxRuntime;
    private int scheduledChunks;
    private long startNs;
    private long elapsedNs;
    
    private MapPlayerTask(final ChunkRenderController chunkRenderController, final World world, final MapView mapView, final Collection<ChunkPos> chunkCoords) {
        super(chunkRenderController, world, mapView, chunkCoords, false, true, 10000);
        this.maxRuntime = Journeymap.getClient().getCoreProperties().renderDelay.get() * 3000;
        this.scheduledChunks = 0;
    }
    
    public static void forceNearbyRemap() {
        synchronized (MapPlayerTask.class) {
            DataCache.INSTANCE.invalidateChunkMDCache();
        }
    }
    
    public static MapPlayerTaskBatch create(final ChunkRenderController chunkRenderController, final EntityDTO player) {
        final Entity playerEntity = player.entityRef.get();
        if (playerEntity == null) {
            return null;
        }
        final int dimension = player.dimension;
        final boolean underground = player.underground;
        final boolean undergroundAllowed = ClientFeatures.instance().isAllowed(Feature.MapType.Underground, dimension);
        final boolean surfaceAllowed = ClientFeatures.instance().isAllowed(Feature.MapType.Day, dimension) || ClientFeatures.instance().isAllowed(Feature.MapType.Night, dimension);
        final boolean topoAllowed = Journeymap.getClient().getCoreProperties().mapTopography.get() && ClientFeatures.instance().isAllowed(Feature.MapType.Topo, dimension);
        final List<ITask> tasks = new ArrayList<ITask>(2);
        if (underground && undergroundAllowed) {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.field_70170_p, MapView.underground(player), new ArrayList<ChunkPos>()));
            if (surfaceAllowed && Journeymap.getClient().getCoreProperties().alwaysMapSurface.get()) {
                tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.field_70170_p, MapView.day(player), new ArrayList<ChunkPos>()));
            }
        }
        else if (surfaceAllowed) {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.field_70170_p, MapView.day(player), new ArrayList<ChunkPos>()));
            if (undergroundAllowed && Journeymap.getClient().getCoreProperties().alwaysMapCaves.get()) {
                tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.field_70170_p, MapView.underground(player), new ArrayList<ChunkPos>()));
            }
        }
        if (topoAllowed && Journeymap.getClient().getCoreProperties().mapTopography.get()) {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.field_70170_p, MapView.topo(player), new ArrayList<ChunkPos>()));
        }
        if (tasks.isEmpty()) {
            return null;
        }
        return new MapPlayerTaskBatch(tasks);
    }
    
    public static String[] getDebugStats() {
        try {
            final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            final boolean underground = DataCache.getPlayer().underground;
            final ArrayList<String> lines = new ArrayList<String>(MapPlayerTask.tempDebugLines.asMap().values());
            if (underground || coreProperties.alwaysMapCaves.get()) {
                lines.add(RenderSpec.getUndergroundSpec().getDebugStats());
            }
            if (!underground || coreProperties.alwaysMapSurface.get()) {
                lines.add(RenderSpec.getSurfaceSpec().getDebugStats());
            }
            if (!underground && coreProperties.mapTopography.get()) {
                lines.add(RenderSpec.getTopoSpec().getDebugStats());
            }
            return lines.toArray(new String[lines.size()]);
        }
        catch (Throwable t) {
            MapPlayerTask.logger.error((Object)t);
            return new String[0];
        }
    }
    
    public static void addTempDebugMessage(final String key, final String message) {
        if (Minecraft.func_71410_x().field_71474_y.field_181657_aC) {
            MapPlayerTask.tempDebugLines.put((Object)key, (Object)message);
        }
    }
    
    public static void removeTempDebugMessage(final String key) {
        MapPlayerTask.tempDebugLines.invalidate((Object)key);
    }
    
    public static String getSimpleStats() {
        int primaryRenderSize = 0;
        int secondaryRenderSize = 0;
        int totalChunks = 0;
        if (DataCache.getPlayer().underground || Journeymap.getClient().getCoreProperties().alwaysMapCaves.get()) {
            final RenderSpec spec = RenderSpec.getUndergroundSpec();
            if (spec != null) {
                primaryRenderSize += spec.getPrimaryRenderSize();
                secondaryRenderSize += spec.getLastSecondaryRenderSize();
                totalChunks += spec.getLastTaskChunks();
            }
        }
        if (!DataCache.getPlayer().underground || Journeymap.getClient().getCoreProperties().alwaysMapSurface.get()) {
            final RenderSpec spec = RenderSpec.getSurfaceSpec();
            if (spec != null) {
                primaryRenderSize += spec.getPrimaryRenderSize();
                secondaryRenderSize += spec.getLastSecondaryRenderSize();
                totalChunks += spec.getLastTaskChunks();
            }
        }
        return Constants.getString("jm.common.renderstats", totalChunks, primaryRenderSize, secondaryRenderSize, MapPlayerTask.lastTaskTime, MapPlayerTask.decFormat.format(MapPlayerTask.lastTaskAvgChunkTime));
    }
    
    public static long getlastTaskCompleted() {
        return MapPlayerTask.lastTaskCompleted;
    }
    
    @Override
    public void initTask(final Minecraft minecraft, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        this.startNs = System.nanoTime();
        RenderSpec renderSpec = null;
        if (this.mapView.isUnderground()) {
            renderSpec = RenderSpec.getUndergroundSpec();
        }
        else if (this.mapView.isTopo()) {
            renderSpec = RenderSpec.getTopoSpec();
        }
        else {
            renderSpec = RenderSpec.getSurfaceSpec();
        }
        final long now = System.currentTimeMillis();
        final List<ChunkPos> renderArea = renderSpec.getRenderAreaCoords();
        final int maxBatchSize = renderArea.size() / 4;
        final ChunkMD chunkMD;
        final long n;
        renderArea.removeIf(ChunkPos -> {
            chunkMD = DataCache.INSTANCE.getChunkMD(ChunkPos);
            if (chunkMD == null || !chunkMD.hasChunk() || n - chunkMD.getLastRendered(this.mapView) < 30000L) {
                return true;
            }
            else if (chunkMD.getDimension() != this.mapView.dimension) {
                return true;
            }
            else {
                chunkMD.resetBlockData(this.mapView);
                return false;
            }
        });
        if (renderArea.size() <= maxBatchSize) {
            this.chunkCoords.addAll(renderArea);
        }
        else {
            final List<ChunkPos> list = Arrays.asList((ChunkPos[])renderArea.toArray((T[])new ChunkPos[renderArea.size()]));
            this.chunkCoords.addAll(list.subList(0, maxBatchSize));
        }
        this.scheduledChunks = this.chunkCoords.size();
    }
    
    @Override
    protected void complete(final int mappedChunks, final boolean cancelled, final boolean hadError) {
        this.elapsedNs = System.nanoTime() - this.startNs;
    }
    
    @Override
    public int getMaxRuntime() {
        return this.maxRuntime;
    }
    
    static {
        MapPlayerTask.decFormat = new DecimalFormat("##.#");
        MapPlayerTask.tempDebugLines = (Cache<String, String>)CacheBuilder.newBuilder().maximumSize(20L).expireAfterWrite(1500L, TimeUnit.MILLISECONDS).build();
    }
    
    public static class Manager implements ITaskManager
    {
        final int mapTaskDelay;
        boolean enabled;
        
        public Manager() {
            this.mapTaskDelay = Journeymap.getClient().getCoreProperties().renderDelay.get() * 1000;
        }
        
        @Override
        public Class<? extends BaseMapTask> getTaskClass() {
            return MapPlayerTask.class;
        }
        
        @Override
        public boolean enableTask(final Minecraft minecraft, final Object params) {
            return this.enabled = true;
        }
        
        @Override
        public boolean isEnabled(final Minecraft minecraft) {
            return this.enabled;
        }
        
        @Override
        public void disableTask(final Minecraft minecraft) {
            this.enabled = false;
        }
        
        @Override
        public ITask getTask(final Minecraft minecraft) {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player != null && this.enabled && player.field_70175_ag && System.currentTimeMillis() - MapPlayerTask.lastTaskCompleted >= this.mapTaskDelay) {
                final ChunkRenderController chunkRenderController = Journeymap.getClient().getChunkRenderController();
                return MapPlayerTask.create(chunkRenderController, DataCache.getPlayer());
            }
            return null;
        }
        
        @Override
        public void taskAccepted(final ITask task, final boolean accepted) {
        }
    }
    
    public static class MapPlayerTaskBatch extends TaskBatch
    {
        public MapPlayerTaskBatch(final List<ITask> tasks) {
            super(tasks);
        }
        
        @Override
        public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
            if (Journeymap.clientPlayer() == null) {
                return;
            }
            this.startNs = System.nanoTime();
            final List<ITask> tasks = new ArrayList<ITask>(this.taskList);
            super.performTask(mc, jm, jmWorldDir, threadLogging);
            this.elapsedNs = System.nanoTime() - this.startNs;
            MapPlayerTask.lastTaskTime = TimeUnit.NANOSECONDS.toMillis(this.elapsedNs);
            MapPlayerTask.lastTaskCompleted = System.currentTimeMillis();
            int chunkCount = 0;
            for (final ITask task : tasks) {
                if (task instanceof MapPlayerTask) {
                    final MapPlayerTask mapPlayerTask = (MapPlayerTask)task;
                    chunkCount += mapPlayerTask.scheduledChunks;
                    if (mapPlayerTask.mapView.isUnderground()) {
                        RenderSpec.getUndergroundSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                    else if (mapPlayerTask.mapView.isTopo()) {
                        RenderSpec.getTopoSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                    else {
                        RenderSpec.getSurfaceSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                }
                else {
                    Journeymap.getLogger().warn("Unexpected task in batch: " + task);
                }
            }
            MapPlayerTask.lastTaskAvgChunkTime = this.elapsedNs / Math.max(1, chunkCount) / 1000000.0;
        }
    }
}
