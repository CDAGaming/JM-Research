package journeymap.client.task.multi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapPlayerTask extends BaseMapTask {
    private static int MAX_STALE_MILLISECONDS;
    private static int MAX_BATCH_SIZE;
    private static DecimalFormat decFormat;
    private static volatile long lastTaskCompleted;
    private static long lastTaskTime;
    private static double lastTaskAvgChunkTime;
    private static Cache<String, String> tempDebugLines;

    static {
        MapPlayerTask.MAX_STALE_MILLISECONDS = 30000;
        MapPlayerTask.MAX_BATCH_SIZE = 32;
        MapPlayerTask.decFormat = new DecimalFormat("##.#");
        MapPlayerTask.tempDebugLines = CacheBuilder.newBuilder().maximumSize(20L).expireAfterWrite(1500L, TimeUnit.MILLISECONDS).build();
    }

    private final int maxRuntime;
    private int scheduledChunks;
    private long startNs;
    private long elapsedNs;

    private MapPlayerTask(final ChunkRenderController chunkRenderController, final World world, final MapType mapType, final Collection<ChunkPos> chunkCoords) {
        super(chunkRenderController, world, mapType, chunkCoords, false, true, 10000);
        this.maxRuntime = Journeymap.getClient().getCoreProperties().renderDelay.get() * 3000;
        this.scheduledChunks = 0;
    }

    public static void forceNearbyRemap() {
        synchronized (MapPlayerTask.class) {
            DataCache.INSTANCE.invalidateChunkMDCache();
        }
    }

    public static MapPlayerTaskBatch create(final ChunkRenderController chunkRenderController, final EntityDTO player) {
        final boolean surfaceAllowed = FeatureManager.isAllowed(Feature.MapSurface);
        final boolean cavesAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        if (!surfaceAllowed && !cavesAllowed) {
            return null;
        }
        final EntityLivingBase playerEntity = player.entityLivingRef.get();
        if (playerEntity == null) {
            return null;
        }
        final boolean underground = player.underground;
        MapType mapType;
        if (underground) {
            mapType = MapType.underground(player);
        } else {
            final long time = playerEntity.world.getWorldInfo().getWorldTime() % 24000L;
            mapType = ((time < 13800L) ? MapType.day(player) : MapType.night(player));
        }
        final List<ITask> tasks = new ArrayList<>(2);
        tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, mapType, new ArrayList<>()));
        if (underground) {
            if (surfaceAllowed && Journeymap.getClient().getCoreProperties().alwaysMapSurface.get()) {
                tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.day(player), new ArrayList<>()));
            }
        } else if (cavesAllowed && Journeymap.getClient().getCoreProperties().alwaysMapCaves.get()) {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.underground(player), new ArrayList<>()));
        }
        if (Journeymap.getClient().getCoreProperties().mapTopography.get()) {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.topo(player), new ArrayList<>()));
        }
        return new MapPlayerTaskBatch(tasks);
    }

    public static String[] getDebugStats() {
        try {
            final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            final boolean underground = DataCache.getPlayer().underground;
            final ArrayList<String> lines = new ArrayList<>(MapPlayerTask.tempDebugLines.asMap().values());
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
        } catch (Throwable t) {
            MapPlayerTask.logger.error(t);
            return new String[0];
        }
    }

    public static void addTempDebugMessage(final String key, final String message) {
        if (Minecraft.getMinecraft().gameSettings.showLagometer) {
            MapPlayerTask.tempDebugLines.put(key, message);
        }
    }

    public static void removeTempDebugMessage(final String key) {
        MapPlayerTask.tempDebugLines.invalidate(key);
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
        RenderSpec renderSpec;
        if (this.mapType.isUnderground()) {
            renderSpec = RenderSpec.getUndergroundSpec();
        } else if (this.mapType.isTopo()) {
            renderSpec = RenderSpec.getTopoSpec();
        } else {
            renderSpec = RenderSpec.getSurfaceSpec();
        }
        final long now = System.currentTimeMillis();
        final List<ChunkPos> renderArea = renderSpec.getRenderAreaCoords();
        final int maxBatchSize = renderArea.size() / 4;
        final ChunkMD[] chunkMD = new ChunkMD[1];
        final long n = 0;
        renderArea.removeIf(chunkPos -> {
            chunkMD[0] = DataCache.INSTANCE.getChunkMD(chunkPos);
            if (chunkMD[0] == null || !chunkMD[0].hasChunk() || n - chunkMD[0].getLastRendered(this.mapType) < 30000L) {
                return true;
            } else if (chunkMD[0].getDimension() != this.mapType.dimension) {
                return true;
            } else {
                chunkMD[0].resetBlockData(this.mapType);
                return false;
            }
        });
        if (renderArea.size() <= maxBatchSize) {
            this.chunkCoords.addAll(renderArea);
        } else {
            final List<ChunkPos> list = Arrays.asList((ChunkPos[]) renderArea.toArray(new ChunkPos[renderArea.size()]));
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

    public static class Manager implements ITaskManager {
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
            if (this.enabled && minecraft.player.addedToChunk && System.currentTimeMillis() - MapPlayerTask.lastTaskCompleted >= this.mapTaskDelay) {
                final ChunkRenderController chunkRenderController = Journeymap.getClient().getChunkRenderController();
                return MapPlayerTask.create(chunkRenderController, DataCache.getPlayer());
            }
            return null;
        }

        @Override
        public void taskAccepted(final ITask task, final boolean accepted) {
        }
    }

    public static class MapPlayerTaskBatch extends TaskBatch {
        public MapPlayerTaskBatch(final List<ITask> tasks) {
            super(tasks);
        }

        @Override
        public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
            if (mc.player == null) {
                return;
            }
            this.startNs = System.nanoTime();
            final List<ITask> tasks = new ArrayList<>(this.taskList);
            super.performTask(mc, jm, jmWorldDir, threadLogging);
            this.elapsedNs = System.nanoTime() - this.startNs;
            MapPlayerTask.lastTaskTime = TimeUnit.NANOSECONDS.toMillis(this.elapsedNs);
            MapPlayerTask.lastTaskCompleted = System.currentTimeMillis();
            int chunkCount = 0;
            for (final ITask task : tasks) {
                if (task instanceof MapPlayerTask) {
                    final MapPlayerTask mapPlayerTask = (MapPlayerTask) task;
                    chunkCount += mapPlayerTask.scheduledChunks;
                    if (mapPlayerTask.mapType.isUnderground()) {
                        RenderSpec.getUndergroundSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    } else if (mapPlayerTask.mapType.isTopo()) {
                        RenderSpec.getTopoSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    } else {
                        RenderSpec.getSurfaceSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                } else {
                    Journeymap.getLogger().warn("Unexpected task in batch: " + task);
                }
            }
            MapPlayerTask.lastTaskAvgChunkTime = this.elapsedNs / Math.max(1, chunkCount) / 1000000.0;
        }
    }
}
