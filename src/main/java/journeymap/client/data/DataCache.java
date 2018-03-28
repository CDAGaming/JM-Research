package journeymap.client.data;

import com.google.common.cache.*;
import com.google.common.collect.Sets;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.client.model.*;
import journeymap.client.render.draw.DrawEntityStep;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum DataCache {
    INSTANCE;

    final LoadingCache<Long, Map> all;
    final LoadingCache<Class, Map<String, EntityDTO>> animals;
    final LoadingCache<Class, Map<String, EntityDTO>> mobs;
    final LoadingCache<Class, Map<String, EntityDTO>> players;
    final LoadingCache<Class, Map<String, EntityDTO>> villagers;
    final LoadingCache<Class, Collection<Waypoint>> waypoints;
    final LoadingCache<Class, EntityDTO> player;
    final LoadingCache<Class, WorldData> world;
    final LoadingCache<RegionImageSet.Key, RegionImageSet> regionImageSets;
    final LoadingCache<Class, Map<String, Object>> messages;
    final LoadingCache<EntityLivingBase, DrawEntityStep> entityDrawSteps;
    final LoadingCache<Waypoint, DrawWayPointStep> waypointDrawSteps;
    final LoadingCache<EntityLivingBase, EntityDTO> entityDTOs;
    final Cache<String, RegionCoord> regionCoords;
    final Cache<String, MapType> mapTypes;
    final LoadingCache<IBlockState, BlockMD> blockMetadata;
    final Cache<ChunkPos, ChunkMD> chunkMetadata;
    final HashMap<Cache, String> managedCaches;
    private final int chunkCacheExpireSeconds = 30;
    private final int defaultConcurrencyLevel = 2;

    private DataCache() {
        this.managedCaches = new HashMap<Cache, String>();
        final AllData allData = new AllData();
        this.all = (LoadingCache<Long, Map>) this.getCacheBuilder().maximumSize(1L).expireAfterWrite(allData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) allData);
        this.managedCaches.put((Cache) this.all, "AllData (web)");
        final AnimalsData animalsData = new AnimalsData();
        this.animals = (LoadingCache<Class, Map<String, EntityDTO>>) this.getCacheBuilder().expireAfterWrite(animalsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) animalsData);
        this.managedCaches.put((Cache) this.animals, "Animals");
        final MobsData mobsData = new MobsData();
        this.mobs = (LoadingCache<Class, Map<String, EntityDTO>>) this.getCacheBuilder().expireAfterWrite(mobsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) mobsData);
        this.managedCaches.put((Cache) this.mobs, "Mobs");
        final PlayerData playerData = new PlayerData();
        this.player = (LoadingCache<Class, EntityDTO>) this.getCacheBuilder().expireAfterWrite(playerData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) playerData);
        this.managedCaches.put((Cache) this.player, "Player");
        final PlayersData playersData = new PlayersData();
        this.players = (LoadingCache<Class, Map<String, EntityDTO>>) this.getCacheBuilder().expireAfterWrite(playersData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) playersData);
        this.managedCaches.put((Cache) this.players, "Players");
        final VillagersData villagersData = new VillagersData();
        this.villagers = (LoadingCache<Class, Map<String, EntityDTO>>) this.getCacheBuilder().expireAfterWrite(villagersData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) villagersData);
        this.managedCaches.put((Cache) this.villagers, "Villagers");
        final WaypointsData waypointsData = new WaypointsData();
        this.waypoints = (LoadingCache<Class, Collection<Waypoint>>) this.getCacheBuilder().expireAfterWrite(waypointsData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) waypointsData);
        this.managedCaches.put((Cache) this.waypoints, "Waypoints");
        final WorldData worldData = new WorldData();
        this.world = (LoadingCache<Class, WorldData>) this.getCacheBuilder().expireAfterWrite(worldData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) worldData);
        this.managedCaches.put((Cache) this.world, "World");
        final MessagesData messagesData = new MessagesData();
        this.messages = (LoadingCache<Class, Map<String, Object>>) this.getCacheBuilder().expireAfterWrite(messagesData.getTTL(), TimeUnit.MILLISECONDS).build((CacheLoader) messagesData);
        this.managedCaches.put((Cache) this.messages, "Messages (web)");
        this.entityDrawSteps = (LoadingCache<EntityLivingBase, DrawEntityStep>) this.getCacheBuilder().weakKeys().build((CacheLoader) new DrawEntityStep.SimpleCacheLoader());
        this.managedCaches.put((Cache) this.entityDrawSteps, "DrawEntityStep");
        this.waypointDrawSteps = (LoadingCache<Waypoint, DrawWayPointStep>) this.getCacheBuilder().weakKeys().build((CacheLoader) new DrawWayPointStep.SimpleCacheLoader());
        this.managedCaches.put((Cache) this.waypointDrawSteps, "DrawWaypointStep");
        this.entityDTOs = (LoadingCache<EntityLivingBase, EntityDTO>) this.getCacheBuilder().weakKeys().build((CacheLoader) new EntityDTO.SimpleCacheLoader());
        this.managedCaches.put((Cache) this.entityDTOs, "EntityDTO");
        this.regionImageSets = RegionImageCache.INSTANCE.initRegionImageSetsCache(this.getCacheBuilder());
        this.managedCaches.put((Cache) this.regionImageSets, "RegionImageSet");
        this.blockMetadata = (LoadingCache<IBlockState, BlockMD>) this.getCacheBuilder().weakKeys().build((CacheLoader) new BlockMD.CacheLoader());
        this.managedCaches.put((Cache) this.blockMetadata, "BlockMD");
        this.chunkMetadata = this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.chunkMetadata, "ChunkMD");
        this.regionCoords = this.getCacheBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).build();
        this.managedCaches.put(this.regionCoords, "RegionCoord");
        this.mapTypes = this.getCacheBuilder().build();
        this.managedCaches.put(this.mapTypes, "MapType");
    }

    public static EntityDTO getPlayer() {
        return DataCache.INSTANCE.getPlayer(false);
    }

    private CacheBuilder<Object, Object> getCacheBuilder() {
        final CacheBuilder<Object, Object> builder = (CacheBuilder<Object, Object>) CacheBuilder.newBuilder();
        builder.concurrencyLevel(2);
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get()) {
            builder.recordStats();
        }
        return builder;
    }

    public Map getAll(final long since) {
        synchronized (this.all) {
            try {
                return (Map) this.all.get(since);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getAll: " + LogFormatter.toString(e));
                return Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getAnimals(final boolean forceRefresh) {
        synchronized (this.animals) {
            try {
                if (forceRefresh) {
                    this.animals.invalidateAll();
                }
                return (Map<String, EntityDTO>) this.animals.get(AnimalsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getAnimals: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public Map<String, EntityDTO> getMobs(final boolean forceRefresh) {
        synchronized (this.mobs) {
            try {
                if (forceRefresh) {
                    this.mobs.invalidateAll();
                }
                return (Map<String, EntityDTO>) this.mobs.get(MobsData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getMobs: " + LogFormatter.toString(e));
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
                return (Map<String, EntityDTO>) this.players.get(PlayersData.class);
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
                return (EntityDTO) this.player.get(PlayerData.class);
            } catch (Exception e) {
                Journeymap.getLogger().error("ExecutionException in getPlayer: " + LogFormatter.toString(e));
                return null;
            }
        }
    }

    public Map<String, EntityDTO> getVillagers(final boolean forceRefresh) {
        synchronized (this.villagers) {
            try {
                if (forceRefresh) {
                    this.villagers.invalidateAll();
                }
                return (Map<String, EntityDTO>) this.villagers.get(VillagersData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getVillagers: " + LogFormatter.toString(e));
                return (Map<String, EntityDTO>) Collections.EMPTY_MAP;
            }
        }
    }

    public MapType getMapType(final MapType.Name name, Integer vSlice, final int dimension) {
        vSlice = ((name != MapType.Name.underground) ? null : vSlice);
        MapType mapType = (MapType) this.mapTypes.getIfPresent((Object) MapType.toCacheKey(name, vSlice, dimension));
        if (mapType == null) {
            mapType = new MapType(name, vSlice, dimension);
            this.mapTypes.put(mapType.toCacheKey(), mapType);
        }
        return mapType;
    }

    public Collection<Waypoint> getWaypoints(final boolean forceRefresh) {
        synchronized (this.waypoints) {
            if (WaypointsData.isManagerEnabled()) {
                return WaypointStore.INSTANCE.getAll();
            }
            return (Collection<Waypoint>) Collections.EMPTY_LIST;
        }
    }

    public Map<String, Object> getMessages(final boolean forceRefresh) {
        synchronized (this.messages) {
            try {
                if (forceRefresh) {
                    this.messages.invalidateAll();
                }
                return (Map<String, Object>) this.messages.get(MessagesData.class);
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
                return (WorldData) this.world.get(WorldData.class);
            } catch (ExecutionException e) {
                Journeymap.getLogger().error("ExecutionException in getWorld: " + LogFormatter.toString(e));
                return new WorldData();
            }
        }
    }

    public void resetRadarCaches() {
        this.animals.invalidateAll();
        this.mobs.invalidateAll();
        this.players.invalidateAll();
        this.villagers.invalidateAll();
        this.entityDrawSteps.invalidateAll();
        this.entityDTOs.invalidateAll();
    }

    public DrawEntityStep getDrawEntityStep(final EntityLivingBase entity) {
        synchronized (this.entityDrawSteps) {
            return (DrawEntityStep) this.entityDrawSteps.getUnchecked(entity);
        }
    }

    public EntityDTO getEntityDTO(final EntityLivingBase entity) {
        synchronized (this.entityDTOs) {
            return (EntityDTO) this.entityDTOs.getUnchecked(entity);
        }
    }

    public DrawWayPointStep getDrawWayPointStep(final Waypoint waypoint) {
        synchronized (this.waypointDrawSteps) {
            return (DrawWayPointStep) this.waypointDrawSteps.getUnchecked(waypoint);
        }
    }

    public boolean hasBlockMD(final IBlockState aBlockState) {
        try {
            return this.blockMetadata.getIfPresent((Object) aBlockState) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public BlockMD getBlockMD(final IBlockState blockState) {
        try {
            return (BlockMD) this.blockMetadata.get(blockState);
        } catch (Exception e) {
            Journeymap.getLogger().error("Error in getBlockMD() for " + blockState + ": " + e);
            return BlockMD.AIRBLOCK;
        }
    }

    public int getBlockMDCount() {
        return this.blockMetadata.asMap().size();
    }

    public Set<BlockMD> getLoadedBlockMDs() {
        return (Set<BlockMD>) Sets.newHashSet((Iterable) this.blockMetadata.asMap().values());
    }

    public void resetBlockMetadata() {
        this.blockMetadata.invalidateAll();
    }

    public ChunkMD getChunkMD(final BlockPos blockPos) {
        return this.getChunkMD(new ChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4));
    }

    public ChunkMD getChunkMD(final ChunkPos coord) {
        synchronized (this.chunkMetadata) {
            ChunkMD chunkMD = null;
            try {
                chunkMD = (ChunkMD) this.chunkMetadata.getIfPresent((Object) coord);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    return chunkMD;
                }
                chunkMD = ChunkLoader.getChunkMdFromMemory((World) FMLClientHandler.instance().getClient().world, coord.x, coord.z);
                if (chunkMD != null && chunkMD.hasChunk()) {
                    this.chunkMetadata.put(coord, chunkMD);
                    return chunkMD;
                }
                if (chunkMD != null) {
                    this.chunkMetadata.invalidate((Object) coord);
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
        final ArrayList<Map.Entry<Cache, String>> list = new ArrayList<Map.Entry<Cache, String>>(cacheMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Cache, String>>() {
            @Override
            public int compare(final Map.Entry<Cache, String> o1, final Map.Entry<Cache, String> o2) {
                return o1.getValue().compareTo((String) o2.getValue());
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
