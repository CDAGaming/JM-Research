package journeymap.client.data;

import net.minecraft.entity.*;
import journeymap.client.api.display.*;
import journeymap.client.render.draw.*;
import net.minecraft.block.state.*;
import journeymap.client.model.*;
import journeymap.common.*;
import journeymap.common.log.*;
import java.util.concurrent.*;
import journeymap.common.api.feature.*;
import com.google.common.collect.*;
import net.minecraft.util.math.*;
import journeymap.client.io.nbt.*;
import java.util.*;
import com.google.common.cache.*;

public enum DataCache
{
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
        this.managedCaches = new HashMap<Cache, String>();
        final AllData allData = new AllData();
        this.all = (LoadingCache<Long, Map>)this.getCacheBuilder().maximumSize(1L).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)allData);
        this.managedCaches.put((Cache)this.all, "AllData (web)");
        final PassiveMobsData passiveMobsData = new PassiveMobsData();
        this.passiveMobs = (LoadingCache<Class, Map<String, EntityDTO>>)this.getCacheBuilder().expireAfterWrite(passiveMobsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)passiveMobsData);
        this.managedCaches.put((Cache)this.passiveMobs, "PassiveMobs");
        final HostileMobsData hostileMobsData = new HostileMobsData();
        this.hostileMobs = (LoadingCache<Class, Map<String, EntityDTO>>)this.getCacheBuilder().expireAfterWrite(hostileMobsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)hostileMobsData);
        this.managedCaches.put((Cache)this.hostileMobs, "HostileMobs");
        final PlayerData playerData = new PlayerData();
        this.player = (LoadingCache<Class, EntityDTO>)this.getCacheBuilder().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)playerData);
        this.managedCaches.put((Cache)this.player, "Player");
        final PlayersData playersData = new PlayersData();
        this.players = (LoadingCache<Class, Map<String, EntityDTO>>)this.getCacheBuilder().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)playersData);
        this.managedCaches.put((Cache)this.players, "Players");
        final NpcsData npcsData = new NpcsData();
        this.npcs = (LoadingCache<Class, Map<String, EntityDTO>>)this.getCacheBuilder().expireAfterWrite(npcsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)npcsData);
        this.managedCaches.put((Cache)this.npcs, "Npcs");
        final VehiclesData vehiclesData = new VehiclesData();
        this.vehicles = (LoadingCache<Class, Map<String, EntityDTO>>)this.getCacheBuilder().expireAfterWrite(vehiclesData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)vehiclesData);
        this.managedCaches.put((Cache)this.vehicles, "Vehicles");
        final WorldData worldData = new WorldData();
        this.world = (LoadingCache<Class, WorldData>)this.getCacheBuilder().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)worldData);
        this.managedCaches.put((Cache)this.world, "World");
        final MessagesData messagesData = new MessagesData();
        this.messages = (LoadingCache<Class, Map<String, Object>>)this.getCacheBuilder().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader)messagesData);
        this.managedCaches.put((Cache)this.messages, "Messages (web)");
        this.entityDrawSteps = (LoadingCache<Entity, DrawEntityStep>)this.getCacheBuilder().weakKeys().build((CacheLoader)new DrawEntityStep.SimpleCacheLoader());
        this.managedCaches.put((Cache)this.entityDrawSteps, "DrawEntityStep");
        this.waypointDrawSteps = (LoadingCache<Waypoint, DrawWayPointStep>)this.getCacheBuilder().weakKeys().build((CacheLoader)new DrawWayPointStep.SimpleCacheLoader());
        this.managedCaches.put((Cache)this.waypointDrawSteps, "DrawWaypointStep");
        this.entityDTOs = (LoadingCache<Entity, EntityDTO>)this.getCacheBuilder().weakKeys().build((CacheLoader)new EntityDTO.SimpleCacheLoader());
        this.managedCaches.put((Cache)this.entityDTOs, "EntityDTO");
        this.regionImageSets = RegionImageCache.INSTANCE.initRegionImageSetsCache(this.getCacheBuilder());
        this.managedCaches.put((Cache)this.regionImageSets, "RegionImageSet");
        this.blockMetadata = (LoadingCache<IBlockState, BlockMD>)this.getCacheBuilder().weakKeys().build((CacheLoader)new BlockMD.CacheLoader());
        this.managedCaches.put((Cache)this.blockMetadata, "BlockMD");
        this.chunkMetadata = (Cache<ChunkPos, ChunkMD>)this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.chunkMetadata, "ChunkMD");
        this.regionCoords = (Cache<String, RegionCoord>)this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.regionCoords, "RegionCoord");
        this.mapViews = (Cache<String, MapView>)this.getCacheBuilder().build();
        this.managedCaches.put(this.mapViews, "MapType");
    }
    
    public static EntityDTO getPlayer() {
        return DataCache.INSTANCE.getPlayer(false);
    }
    
    private CacheBuilder<Object, Object> getCacheBuilder() {
        final CacheBuilder<Object, Object> builder = (CacheBuilder<Object, Object>)CacheBuilder.newBuilder();
        builder.concurrencyLevel(2);
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            builder.recordStats();
        }
        return builder;
    }
    
    public Map getAll(final long since) {
        synchronized (this.all) {
            try {
                return (Map)this.all.get((Object)since);
            }
            catch (ExecutionException e) {
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
                return (Map<String, EntityDTO>)this.passiveMobs.get((Object)PassiveMobsData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getPassiveMobs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public Map<String, EntityDTO> getVehicles(final boolean forceRefresh) {
        synchronized (this.vehicles) {
            try {
                if (forceRefresh) {
                    this.vehicles.invalidateAll();
                }
                return (Map<String, EntityDTO>)this.vehicles.get((Object)PassiveMobsData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getVehicles: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public Map<String, EntityDTO> getHostileMobs(final boolean forceRefresh) {
        synchronized (this.hostileMobs) {
            try {
                if (forceRefresh) {
                    this.hostileMobs.invalidateAll();
                }
                return (Map<String, EntityDTO>)this.hostileMobs.get((Object)HostileMobsData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getHostileMobs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public Map<String, EntityDTO> getPlayers(final boolean forceRefresh) {
        synchronized (this.players) {
            try {
                if (forceRefresh) {
                    this.players.invalidateAll();
                }
                return (Map<String, EntityDTO>)this.players.get((Object)PlayersData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getPlayers: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public EntityDTO getPlayer(final boolean forceRefresh) {
        synchronized (this.player) {
            try {
                if (forceRefresh) {
                    this.player.invalidateAll();
                }
                return (EntityDTO)this.player.get((Object)PlayerData.class);
            }
            catch (Exception e) {
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
                return (Map<String, EntityDTO>)this.npcs.get((Object)NpcsData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getNpcs: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public MapView getMapView(final Feature.MapType mapType, Integer vSlice, final int dimension) {
        vSlice = ((mapType != Feature.MapType.Underground) ? null : vSlice);
        MapView mapView = (MapView)this.mapViews.getIfPresent((Object)MapView.toCacheKey(mapType, vSlice, dimension));
        if (mapView == null) {
            mapView = new MapView(mapType, vSlice, dimension);
            this.mapViews.put((Object)mapView.toCacheKey(), (Object)mapView);
        }
        return mapView;
    }
    
    public Map<String, Object> getMessages(final boolean forceRefresh) {
        synchronized (this.messages) {
            try {
                if (forceRefresh) {
                    this.messages.invalidateAll();
                }
                return (Map<String, Object>)this.messages.get((Object)MessagesData.class);
            }
            catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getMessages: " + LogFormatter.toString(e));
                return (Map<String, Object>)Collections.EMPTY_MAP;
            }
        }
    }
    
    public WorldData getWorld(final boolean forceRefresh) {
        synchronized (this.world) {
            try {
                if (forceRefresh) {
                    this.world.invalidateAll();
                }
                return (WorldData)this.world.get((Object)WorldData.class);
            }
            catch (ExecutionException e) {
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
            return (DrawEntityStep)this.entityDrawSteps.getUnchecked((Object)entity);
        }
    }
    
    public EntityDTO getEntityDTO(final Entity entity) {
        synchronized (this.entityDTOs) {
            return (EntityDTO)this.entityDTOs.getUnchecked((Object)entity);
        }
    }
    
    public DrawWayPointStep getDrawWayPointStep(final Waypoint waypoint) {
        synchronized (this.waypointDrawSteps) {
            return (DrawWayPointStep)this.waypointDrawSteps.getUnchecked((Object)waypoint);
        }
    }
    
    public boolean hasBlockMD(final IBlockState aBlockState) {
        try {
            return this.blockMetadata.getIfPresent((Object)aBlockState) != null;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public BlockMD getBlockMD(final IBlockState blockState) {
        try {
            return (BlockMD)this.blockMetadata.get((Object)blockState);
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error in getBlockMD() for " + blockState + ": " + e);
            return BlockMD.AIRBLOCK;
        }
    }
    
    public int getBlockMDCount() {
        return this.blockMetadata.asMap().size();
    }
    
    public Set<BlockMD> getLoadedBlockMDs() {
        return (Set<BlockMD>)Sets.newHashSet((Iterable)this.blockMetadata.asMap().values());
    }
    
    public void resetBlockMetadata() {
        this.blockMetadata.invalidateAll();
    }
    
    public ChunkMD getChunkMD(final BlockPos blockPos) {
        return this.getChunkMD(new ChunkPos(blockPos.func_177958_n() >> 4, blockPos.func_177952_p() >> 4));
    }
    
    public ChunkMD getChunkMD(final ChunkPos coord) {
        synchronized (this.chunkMetadata) {
            ChunkMD chunkMD = null;
            try {
                chunkMD = (ChunkMD)this.chunkMetadata.getIfPresent((Object)coord);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    return chunkMD;
                }
                chunkMD = ChunkLoader.getChunkMdFromMemory(Journeymap.clientWorld(), coord.field_77276_a, coord.field_77275_b);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    this.chunkMetadata.put((Object)coord, (Object)chunkMD);
                    return chunkMD;
                }
                if (chunkMD != null) {
                    this.chunkMetadata.invalidate((Object)coord);
                }
                return null;
            }
            catch (Throwable e) {
                Journeymap.getLogger().warn("Unexpected error getting ChunkMD from cache: " + e);
                return null;
            }
        }
    }
    
    public void addChunkMD(final ChunkMD chunkMD) {
        synchronized (this.chunkMetadata) {
            this.chunkMetadata.put((Object)chunkMD.getCoord(), (Object)chunkMD);
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
                }
                catch (Exception e) {
                    Journeymap.getLogger().warn("Couldn't purge managed cache: " + cache);
                }
            }
        }
    }
    
    public String getDebugHtml() {
        final StringBuffer sb = new StringBuffer();
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            this.appendDebugHtml(sb, "Managed Caches", this.managedCaches);
        }
        else {
            sb.append("<b>Cache stat recording disabled.  Set config/journeymap.core.config 'recordCacheStats' to 1.</b>");
        }
        return sb.toString();
    }
    
    private void appendDebugHtml(final StringBuffer sb, final String name, final Map<Cache, String> cacheMap) {
        final ArrayList<Map.Entry<Cache, String>> list = new ArrayList<Map.Entry<Cache, String>>(cacheMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Cache, String>>() {
            @Override
            public int compare(final Map.Entry<Cache, String> o1, final Map.Entry<Cache, String> o2) {
                return o1.getValue().compareTo((String)o2.getValue());
            }
        });
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
