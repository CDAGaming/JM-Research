package journeymap.client.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import journeymap.client.api.display.Waypoint;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.client.model.*;
import journeymap.client.render.draw.DrawEntityStep;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum DataCache {
    INSTANCE;

    final LoadingCache<Long, Map> all;
    final LoadingCache<Class, Map<String, EntityDTO>> passiveMobs;
    final LoadingCache<Class, Map<String, EntityDTO>> hostileMobs;
    final LoadingCache<Class, Map<String, EntityDTO>> players;
    final LoadingCache<Class, Map<String, EntityDTO>> npcs;
    final LoadingCache<Class, Map<String, EntityDTO>> vehicles;
    final LoadingCache<Class, EntityDTO> player;
    final LoadingCache<Class, WorldData> world;
    final LoadingCache<RegionImageSet.Key, RegionImageSet> regionImageSets;
    final LoadingCache<Class, Map<String, Object>> messages;
    final LoadingCache<Entity, DrawEntityStep> entityDrawSteps;
    final LoadingCache<Waypoint, DrawWayPointStep> waypointDrawSteps;
    final LoadingCache<Entity, EntityDTO> entityDTOs;
    final Cache<String, RegionCoord> regionCoords;
    final Cache<String, MapView> mapViews;
    final LoadingCache<IBlockState, BlockMD> blockMetadata;
    final Cache<ChunkPos, ChunkMD> chunkMetadata;
    final HashMap<Cache, String> managedCaches;
    private final int chunkCacheExpireSeconds = 30;
    private final int defaultConcurrencyLevel = 2;

    private DataCache() {
        this.managedCaches = new HashMap<>();
        final AllData allData = new AllData();
        this.all = this.getCacheBuilder().maximumSize(1L).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build(allData);
        this.managedCaches.put(this.all, "AllData (web)");
        final PassiveMobsData passiveMobsData = new PassiveMobsData();
        this.passiveMobs = this.getCacheBuilder().expireAfterWrite(passiveMobsData.getTTL(), TimeUnit.MILLISECONDS).build(passiveMobsData);
        this.managedCaches.put(this.passiveMobs, "PassiveMobs");
        final HostileMobsData hostileMobsData = new HostileMobsData();
        this.hostileMobs = this.getCacheBuilder().expireAfterWrite(hostileMobsData.getTTL(), TimeUnit.MILLISECONDS).build(hostileMobsData);
        this.managedCaches.put(this.hostileMobs, "HostileMobs");
        final PlayerData playerData = new PlayerData();
        this.player = this.getCacheBuilder().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build(playerData);
        this.managedCaches.put(this.player, "Player");
        final PlayersData playersData = new PlayersData();
        this.players = this.getCacheBuilder().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build(playersData);
        this.managedCaches.put(this.players, "Players");
        final NpcsData npcsData = new NpcsData();
        this.npcs = this.getCacheBuilder().expireAfterWrite(npcsData.getTTL(), TimeUnit.MILLISECONDS).build(npcsData);
        this.managedCaches.put(this.npcs, "Npcs");
        final VehiclesData vehiclesData = new VehiclesData();
        this.vehicles = this.getCacheBuilder().expireAfterWrite(vehiclesData.getTTL(), TimeUnit.MILLISECONDS).build(vehiclesData);
        this.managedCaches.put(this.vehicles, "Vehicles");
        final WorldData worldData = new WorldData();
        this.world = this.getCacheBuilder().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build(worldData);
        this.managedCaches.put(this.world, "World");
        final MessagesData messagesData = new MessagesData();
        this.messages = this.getCacheBuilder().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build(messagesData);
        this.managedCaches.put(this.messages, "Messages (web)");
        this.entityDrawSteps = this.getCacheBuilder().weakKeys().build(new DrawEntityStep.SimpleCacheLoader());
        this.managedCaches.put(this.entityDrawSteps, "DrawEntityStep");
        this.waypointDrawSteps = this.getCacheBuilder().weakKeys().build(new DrawWayPointStep.SimpleCacheLoader());
        this.managedCaches.put(this.waypointDrawSteps, "DrawWaypointStep");
        this.entityDTOs = this.getCacheBuilder().weakKeys().build(new EntityDTO.SimpleCacheLoader());
        this.managedCaches.put(this.entityDTOs, "EntityDTO");
        this.regionImageSets = RegionImageCache.INSTANCE.initRegionImageSetsCache(this.getCacheBuilder());
        this.managedCaches.put(this.regionImageSets, "RegionImageSet");
        this.blockMetadata = this.getCacheBuilder().weakKeys().build(new BlockMD.CacheLoader());
        this.managedCaches.put(this.blockMetadata, "BlockMD");
        this.chunkMetadata = this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.chunkMetadata, "ChunkMD");
        this.regionCoords = this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.regionCoords, "RegionCoord");
        this.mapViews = this.getCacheBuilder().build();
        this.managedCaches.put(this.mapViews, "MapType");
    }

    public static EntityDTO getPlayer() {
        return DataCache.INSTANCE.getPlayer(false);
    }

    private CacheBuilder<Object, Object> getCacheBuilder() {
        final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        builder.concurrencyLevel(2);
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            builder.recordStats();
        }
        return builder;
    }

    public Map getAll(final long since) {
        synchronized (this.all) {
            try {
                return this.all.get(since);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getAll: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getPassiveMobs(final boolean forceRefresh) {
        synchronized (this.passiveMobs) {
            try {
                if (forceRefresh) {
                    this.passiveMobs.invalidateAll();
                }
                return this.passiveMobs.get(PassiveMobsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getPassiveMobs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getVehicles(final boolean forceRefresh) {
        synchronized (this.vehicles) {
            try {
                if (forceRefresh) {
                    this.vehicles.invalidateAll();
                }
                return this.vehicles.get(PassiveMobsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getVehicles: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getHostileMobs(final boolean forceRefresh) {
        synchronized (this.hostileMobs) {
            try {
                if (forceRefresh) {
                    this.hostileMobs.invalidateAll();
                }
                return this.hostileMobs.get(HostileMobsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getHostileMobs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getPlayers(final boolean forceRefresh) {
        synchronized (this.players) {
            try {
                if (forceRefresh) {
                    this.players.invalidateAll();
                }
                return this.players.get(PlayersData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getPlayers: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public EntityDTO getPlayer(final boolean forceRefresh) {
        synchronized (this.player) {
            try {
                if (forceRefresh) {
                    this.player.invalidateAll();
                }
                return this.player.get(PlayerData.class);
            } catch (Exception e) {
                Journeymap.getLogger().error("ExecutionException in getPlayer: " + LogFormatter.toString(e));
                return null;
            }
        }
    }

    public Map<String, EntityDTO> getNpcs(final boolean forceRefresh) {
        synchronized (this.npcs) {
            try {
                if (forceRefresh) {
                    this.npcs.invalidateAll();
                }
                return this.npcs.get(NpcsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getNpcs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public MapView getMapView(final Feature.MapType mapType, Integer vSlice, final int dimension) {
        vSlice = ((mapType != Feature.MapType.Underground) ? null : vSlice);
        MapView mapView = this.mapViews.getIfPresent(MapView.toCacheKey(mapType, vSlice, dimension));
        if (mapView == null) {
            mapView = new MapView(mapType, vSlice, dimension);
            this.mapViews.put(mapView.toCacheKey(), mapView);
        }
        return mapView;
    }

    public Map<String, Object> getMessages(final boolean forceRefresh) {
        synchronized (this.messages) {
            try {
                if (forceRefresh) {
                    this.messages.invalidateAll();
                }
                return this.messages.get(MessagesData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getMessages: " + LogFormatter.toString(e));
                return (Map<String, Object>) Collections.EMPTY_MAP;
            }
        }
    }

    public WorldData getWorld(final boolean forceRefresh) {
        synchronized (this.world) {
            try {
                if (forceRefresh) {
                    this.world.invalidateAll();
                }
                return this.world.get(WorldData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getWorld: " + LogFormatter.toString(e));
                return new WorldData();
            }
        }
    }

    public void resetRadarCaches() {
        this.passiveMobs.invalidateAll();
        this.hostileMobs.invalidateAll();
        this.players.invalidateAll();
        this.npcs.invalidateAll();
        this.entityDrawSteps.invalidateAll();
        this.entityDTOs.invalidateAll();
    }

    public DrawEntityStep getDrawEntityStep(final Entity entity) {
        synchronized (this.entityDrawSteps) {
            return this.entityDrawSteps.getUnchecked(entity);
        }
    }

    public EntityDTO getEntityDTO(final Entity entity) {
        synchronized (this.entityDTOs) {
            return this.entityDTOs.getUnchecked(entity);
        }
    }

    public DrawWayPointStep getDrawWayPointStep(final Waypoint waypoint) {
        synchronized (this.waypointDrawSteps) {
            return this.waypointDrawSteps.getUnchecked(waypoint);
        }
    }

    public boolean hasBlockMD(final IBlockState aBlockState) {
        try {
            return this.blockMetadata.getIfPresent(aBlockState) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public BlockMD getBlockMD(final IBlockState blockState) {
        try {
            return this.blockMetadata.get(blockState);
        } catch (Exception e) {
            Journeymap.getLogger().error("Error in getBlockMD() for " + blockState + ": " + e);
            return BlockMD.AIRBLOCK;
        }
    }

    public int getBlockMDCount() {
        return this.blockMetadata.asMap().size();
    }

    public Set<BlockMD> getLoadedBlockMDs() {
        return Sets.newHashSet(this.blockMetadata.asMap().values());
    }

    public void resetBlockMetadata() {
        this.blockMetadata.invalidateAll();
    }

    public ChunkMD getChunkMD(final BlockPos blockPos) {
        return this.getChunkMD(new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4));
    }

    public ChunkMD getChunkMD(final ChunkPos coord) {
        synchronized (this.chunkMetadata) {
            ChunkMD chunkMD;
            try {
                chunkMD = this.chunkMetadata.getIfPresent(coord);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    return chunkMD;
                }
                chunkMD = ChunkLoader.getChunkMdFromMemory(Journeymap.clientWorld(), coord.x, coord.z);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    this.chunkMetadata.put(coord, chunkMD);
                    return chunkMD;
                }
                if (chunkMD != null) {
                    this.chunkMetadata.invalidate(coord);
                }
                return null;
            } catch (Throwable e) {
                Journeymap.getLogger().warn("Unexpected error getting ChunkMD from cache: " + e);
                return null;
            }
        }
    }

    public void addChunkMD(final ChunkMD chunkMD) {
        synchronized (this.chunkMetadata) {
            this.chunkMetadata.put(chunkMD.getCoord(), chunkMD);
        }
    }

    public void invalidateChunkMDCache() {
        this.chunkMetadata.invalidateAll();
    }

    public void stopChunkMDRetention() {
        for (final ChunkMD chunkMD : this.chunkMetadata.asMap().values()) {
            if (chunkMD != null) {
                chunkMD.stopChunkRetention();
            }
        }
    }

    public LoadingCache<RegionImageSet.Key, RegionImageSet> getRegionImageSets() {
        return this.regionImageSets;
    }

    public Cache<String, RegionCoord> getRegionCoords() {
        return this.regionCoords;
    }

    public void purge() {
        RegionImageCache.INSTANCE.flushToDisk(false);
        this.resetBlockMetadata();
        synchronized (this.managedCaches) {
            for (final Cache cache : this.managedCaches.keySet()) {
                try {
                    cache.invalidateAll();
                } catch (Exception e) {
                    Journeymap.getLogger().warn("Couldn't purge managed cache: " + cache);
                }
            }
        }
    }

    public String getDebugHtml() {
        final StringBuffer sb = new StringBuffer();
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            this.appendDebugHtml(sb, "Managed Caches", this.managedCaches);
        } else {
            sb.append("<b>Cache stat recording disabled.  Set config/journeymap.core.config 'recordCacheStats' to 1.</b>");
        }
        return sb.toString();
    }

    private void appendDebugHtml(final StringBuffer sb, final String name, final Map<Cache, String> cacheMap) {
        final ArrayList<Map.Entry<Cache, String>> list = new ArrayList<>(cacheMap.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getValue));
        sb.append("<b>").append(name).append(":</b>");
        sb.append("<pre>");
        for (final Map.Entry<Cache, String> entry : list) {
            sb.append(this.toString(entry.getValue(), entry.getKey()));
        }
        sb.append("</pre>");
    }

    private String toString(final String label, final Cache cache) {
        double avgLoadMillis = 0.0;
        final CacheStats cacheStats = cache.stats();
        if (cacheStats.totalLoadTime() > 0L && cacheStats.loadSuccessCount() > 0L) {
            avgLoadMillis = TimeUnit.NANOSECONDS.toMillis(cacheStats.totalLoadTime()) * 1.0 / cacheStats.loadSuccessCount();
        }
        return String.format("%s<b>%20s:</b> Size: %9s, Hits: %9s, Misses: %9s, Loads: %9s, Errors: %9s, Avg Load Time: %1.2fms", LogFormatter.LINEBREAK, label, cache.size(), cacheStats.hitCount(), cacheStats.missCount(), cacheStats.loadCount(), cacheStats.loadExceptionCount(), avgLoadMillis);
    }
}
